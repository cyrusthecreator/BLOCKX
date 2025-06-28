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
    private final CustomItemManager customItemManager;

    public static final NamespacedKey HERO_SIDE_KEY = new NamespacedKey("blockx", "hero_side");
    public static final NamespacedKey HERO_TYPE_KEY = new NamespacedKey("blockx", "hero_type");


    public HeroManager(Blockx plugin, CustomItemManager customItemManager) {
        this.plugin = plugin;
        this.customItemManager = customItemManager;
    }

    // Updated spawnHero method to include HeroSide
    public void spawnHero(Player player, String heroTypeStr, HeroSide side, ItemStack weapon) {
        Location spawnLocation = player.getLocation();
        EntityType entityType;

        // Determine EntityType based on heroTypeStr
        switch (heroTypeStr.toLowerCase()) {
            case "zombie":
                entityType = EntityType.ZOMBIE;
                break;
            case "villager":
                entityType = EntityType.VILLAGER;
                break;
            default:
                player.sendMessage(ChatColor.RED + "Unsupported hero type for spawning: " + heroTypeStr);
                return;
        }

        org.bukkit.entity.LivingEntity hero = (org.bukkit.entity.LivingEntity) spawnLocation.getWorld().spawnEntity(spawnLocation, entityType);

        // Set a custom name based on side and type
        String heroName = side.name() + " " + heroTypeStr.substring(0, 1).toUpperCase() + heroTypeStr.substring(1);
        if (weapon != null && weapon.hasItemMeta()) {
            ItemMeta weaponMeta = weapon.getItemMeta();
            if (weaponMeta != null && weaponMeta.hasDisplayName()) {
                String cleanWeaponName = ChatColor.stripColor(weaponMeta.getDisplayName());
                heroName = side.name() + " " + cleanWeaponName + " " + heroTypeStr.substring(0, 1).toUpperCase() + heroTypeStr.substring(1);
            }
        }
        // Display color will be handled in a dedicated step, for now, use side's color
        hero.setCustomName(side.getDisplayColor() + heroName);
        hero.setCustomNameVisible(true);

        hero.setCanPickupItems(true); // Zombies and Skeletons can pick up items. Villagers cannot by default.
                                    // For Villagers, this won't make them pick up items but doesn't hurt.
                                    // Equipment must be set directly.

        if (weapon != null && hero.getEquipment() != null) {
            hero.getEquipment().setItemInMainHand(weapon);
            hero.getEquipment().setItemInMainHandDropChance(0.0f); // Don't drop the main weapon
        } else if (weapon != null && entityType == EntityType.VILLAGER) {
            // Villagers don't have equipment slots in the same way zombies do for holding weapons.
            // This means they can't naturally "hold" and use a sword or axe.
            // We can still give them the item, but their AI won't use it to attack.
            // For true villager combat, more complex solutions like custom AI or invisible entities holding weapons might be needed.
            // For now, we'll acknowledge this limitation.
            plugin.getLogger().warning("Attempted to give a weapon to a Villager hero. Villagers cannot naturally use weapons like swords or axes.");
        }


        // Store hero side and type in persistent data
        hero.getPersistentDataContainer().set(HERO_SIDE_KEY, PersistentDataType.STRING, side.name());
        hero.getPersistentDataContainer().set(HERO_TYPE_KEY, PersistentDataType.STRING, heroTypeStr.toLowerCase());


        player.sendMessage(side.getDisplayColor() + "A " + heroName + " has been summoned to the " + side.name() + " side!");
        plugin.getLogger().info("Spawned " + side.name() + " hero: " + heroName + " ("+ entityType.name() +") for player " + player.getName() + " with weapon: " + (weapon != null ? weapon.getType() : "none"));
    }
}
