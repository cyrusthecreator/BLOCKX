package net.blockx.items; // Updated package

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.ArrayList;
import java.util.List;

// ItemAttributes is now in the same package 'net.blockx.items'

public class CustomItemManager {

    private final JavaPlugin plugin;
    private final NamespacedKey customItemIdKey; // Cached key

    public CustomItemManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.customItemIdKey = new NamespacedKey(plugin, "custom_item_id"); // Initialize key here
    }

    public ItemStack generateItem(Player player, int customModelData, String itemName, ItemAttributes attributes, Material itemMaterial) {
        if (itemMaterial == null) {
            itemMaterial = Material.STICK;
        }

        ItemStack customItem = new ItemStack(itemMaterial);
        ItemMeta meta = customItem.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', itemName)); // Support color codes

            if (attributes != null) {
                List<String> loreLines = new ArrayList<>();
                for(String line : attributes.getLore()){ // Process lore with color codes
                    loreLines.add(ChatColor.translateAlternateColorCodes('&', line));
                }
                meta.setLore(loreLines);
                attributes.applyAttributes(meta);
            }

            if(customModelData > 0) { // Generally, CMDs are positive
                meta.setCustomModelData(customModelData);
            }

            meta.getPersistentDataContainer().set(customItemIdKey, PersistentDataType.STRING, String.valueOf(customModelData));

            customItem.setItemMeta(meta);

            if (player != null) {
                player.getInventory().addItem(customItem);
                player.sendMessage(ChatColor.GREEN + "You received: " + itemName);
            }
        } else {
            plugin.getLogger().severe("Could not get ItemMeta for " + itemMaterial + " when creating " + itemName);
        }
        return customItem;
    }

    public boolean isSpecificCustomItem(ItemStack itemStack, String expectedCustomModelDataStr, Material expectedMaterial) {
        if (itemStack == null || itemStack.getType() != expectedMaterial || !itemStack.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = itemStack.getItemMeta();

        if (!meta.hasCustomModelData()) {
            // If expecting CMD 0, this might need adjustment, but for now, custom items must have CMD > 0
            if (!"0".equals(expectedCustomModelDataStr)) return false;
            // If CMD 0 is expected and item has no CMD, this part is fine. But usually we expect positive CMD.
        }

        try {
            int expectedCmd = Integer.parseInt(expectedCustomModelDataStr);
            if (meta.getCustomModelData() != expectedCmd) {
                 // Special case: if expecting 0 and item has no custom model data, it's a match for this part
                if (!(expectedCmd == 0 && !meta.hasCustomModelData())) {
                    return false;
                }
            }
        } catch (NumberFormatException e) {
            plugin.getLogger().warning("Invalid expectedCustomModelDataStr in isSpecificCustomItem: " + expectedCustomModelDataStr);
            return false;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (!container.has(customItemIdKey, PersistentDataType.STRING)) {
            return false;
        }
        String actualItemIdStr = container.get(customItemIdKey, PersistentDataType.STRING);
        return expectedCustomModelDataStr.equals(actualItemIdStr);
    }

    public String getCustomItemId(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasItemMeta()) {
            return null;
        }
        ItemMeta meta = itemStack.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(customItemIdKey, PersistentDataType.STRING)) {
            return container.get(customItemIdKey, PersistentDataType.STRING);
        }
        return null;
    }
}
