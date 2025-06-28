package net.blockx.blocks; // Updated package

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
// import org.bukkit.block.BlockState; // Unused
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList; // Added for lore processing
import java.util.List;

// BlockAttributes is now in the same package 'net.blockx.blocks'

public class CustomBlockManager {

    private final JavaPlugin plugin;
    private final NamespacedKey customBlockPDCKey; // Key for PDC on block items

    public CustomBlockManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.customBlockPDCKey = new NamespacedKey(plugin, "custom_block_identity"); // More specific key name
    }

    /**
     * Places a custom block. Currently, this method only sets the block type.
     * Actual custom block behavior and appearance rely on resource packs
     * and event handling (e.g., checking item's PDC in BlockPlaceEvent).
     */
    public void placeBlock(Player player, Location location, int blockId, String blockName, BlockAttributes attributes, Material blockMaterial) {
        if (blockMaterial == null) {
            blockMaterial = Material.STONE;
        }
        Block block = location.getBlock();
        block.setType(blockMaterial);

        // Custom data for blocks (if needed beyond item identification) would typically
        // be stored in a TileEntity's PersistentDataContainer if the block has one,
        // or managed externally (e.g., map of Location -> CustomBlockData).
        // The current UltraCraftingTable logic in Blockx.java relies on the item's PDC.

        // player.sendMessage(ChatColor.GREEN + "You placed a custom block: " + blockName); // This message is better handled by the event listener in Blockx.java
    }

    /**
     * Generates an ItemStack representing the custom block.
     * This item has CustomModelData and a PDC tag for identification.
     */
    public ItemStack generateBlockItem(int customModelData, String blockName, Material blockMaterial, List<String> lore) {
        if (blockMaterial == null) {
            blockMaterial = Material.STONE;
        }

        ItemStack blockItem = new ItemStack(blockMaterial);
        ItemMeta meta = blockItem.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', blockName));
            if (customModelData > 0) {
                meta.setCustomModelData(customModelData);
            }

            PersistentDataContainer itemPDC = meta.getPersistentDataContainer();
            // Using the 'ultra_crafting_block' key for consistency with Blockx.java's BlockPlaceEvent check for the Ultra Crafting Table
            // If this manager handles other blocks, this key might need to be more generic or passed as a parameter.
            // For now, assuming it's for items like the Ultra Crafting Table.
            NamespacedKey itemSpecificKey = new NamespacedKey(plugin, "ultra_crafting_block"); // This matches the check in Blockx.java
            if (blockIdMatchesUltraCrafting(customModelData)) { // Example: Check if this ID is for ultra_crafting_table
                 itemPDC.set(itemSpecificKey, PersistentDataType.STRING, "ultra_crafting_table");
            } else {
                 itemPDC.set(this.customBlockPDCKey, PersistentDataType.STRING, String.valueOf(customModelData));
            }


            if (lore != null && !lore.isEmpty()) {
                List<String> coloredLore = new ArrayList<>();
                for (String line : lore) {
                    coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
                }
                meta.setLore(coloredLore);
            }
            blockItem.setItemMeta(meta);
        }
        return blockItem;
    }

    // Helper to check if the blockId corresponds to the Ultra Crafting Table
    // This is a bit of a workaround; ideally, item identity would be more robust.
    private boolean blockIdMatchesUltraCrafting(int blockId) {
        return blockId == 1001; // Assuming 1001 is the CMD for Ultra Crafting Table item
    }
}
