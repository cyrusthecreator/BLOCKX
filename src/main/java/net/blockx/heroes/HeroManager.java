package net.blockx.heroes;

import net.blockx.core.Blockx;
import net.blockx.items.CustomItemManager; // We might need this later for more complex logic
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class HeroManager {

    private final Blockx plugin;
    private final CustomItemManager customItemManager; // Keep for future use if needed

    public static final NamespacedKey CUSTOM_HERO_TAG_KEY = new NamespacedKey("blockx", "custom_hero_type");


    public HeroManager(Blockx plugin, CustomItemManager customItemManager) {
        this.plugin = plugin;
        this.customItemManager = customItemManager;
    }

    public void spawnHero(Player player, String heroType, ItemStack weapon) {
        Location spawnLocation = player.getLocation();

        // For now, we only support "zombie" as a base type.
        // We can expand this later with a factory or strategy pattern for different hero types.
        if (!"zombie".equalsIgnoreCase(heroType)) {
            player.sendMessage(ChatColor.RED + "Unsupported hero type for spawning: " + heroType);
            return;
        }

        Zombie hero = (Zombie) spawnLocation.getWorld().spawnEntity(spawnLocation, EntityType.ZOMBIE);

        // Set a custom name (optional, but good for identification)
        String heroName = "Hero " + heroType.substring(0, 1).toUpperCase() + heroType.substring(1); // e.g., "Hero Zombie"
        if (weapon != null && weapon.hasItemMeta()) {
            ItemMeta weaponMeta = weapon.getItemMeta();
            if (weaponMeta != null && weaponMeta.hasDisplayName()) {
                // Try to make a more specific name, e.g., "Barbarian Axe Wielding Zombie"
                // We need to strip color codes from the display name for a cleaner custom name.
                String cleanWeaponName = ChatColor.stripColor(weaponMeta.getDisplayName());
                heroName = cleanWeaponName + " " + heroType.substring(0, 1).toUpperCase() + heroType.substring(1);
            }
        }
        hero.setCustomName(ChatColor.GOLD + heroName); // Example styling
        hero.setCustomNameVisible(true);

        // Allow the hero to pick up items
        hero.setCanPickupItems(true);

        // Equip weapon if provided
        if (weapon != null) {
            hero.getEquipment().setItemInMainHand(weapon);
            hero.getEquipment().setItemInMainHandDropChance(0.0f); // Don't drop the main weapon
        }

        // Tag the hero for identification by other systems (e.g., HeroPickupListener)
        // The value could be more specific, like "zombie:barbarian_axe_wielder" if needed
        hero.getPersistentDataContainer().set(CUSTOM_HERO_TAG_KEY, PersistentDataType.STRING, heroType);

        player.sendMessage(ChatColor.GREEN + "A " + heroName + " has been summoned!");
        plugin.getLogger().info("Spawned hero: " + heroName + " for player " + player.getName() + " with weapon: " + (weapon != null ? weapon.getType() : "none"));
    }
}
