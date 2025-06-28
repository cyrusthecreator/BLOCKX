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

        // heroTypeStr will now always be "zombie" from the command handlers.
        // We no longer need to differentiate between "zombie" and "villager" here for EntityType.
        EntityType entityTypeToSpawn = EntityType.ZOMBIE;
        String baseHeroName = "Zombie"; // The base name for the hero type

        // Validate heroTypeStr from command - should always be "zombie" but good to be safe.
        if (!"zombie".equalsIgnoreCase(heroTypeStr)) {
            plugin.getLogger().warning("HeroManager received an unexpected heroTypeStr: '" + heroTypeStr + "'. Defaulting to Zombie.");
            // heroTypeStr = "zombie"; // Ensure it is set for PDC if it was something else
        }

        org.bukkit.entity.LivingEntity hero = (org.bukkit.entity.LivingEntity) spawnLocation.getWorld().spawnEntity(spawnLocation, entityTypeToSpawn);

        // Set a custom name based on side and the base hero name ("Zombie")
        String heroName = side.name() + " " + baseHeroName;
        if (weapon != null && weapon.hasItemMeta()) {
            ItemMeta weaponMeta = weapon.getItemMeta();
            if (weaponMeta != null && weaponMeta.hasDisplayName()) {
                String cleanWeaponName = ChatColor.stripColor(weaponMeta.getDisplayName());
                // Example: "RED Sword Zombie", "BLUE Axe Zombie"
                heroName = side.name() + " " + cleanWeaponName + " " + baseHeroName;
            }
        }
        hero.setCustomName(side.getDisplayColor() + heroName);
        hero.setCustomNameVisible(true);

        hero.setCanPickupItems(true);

        if (weapon != null && hero.getEquipment() != null) {
            hero.getEquipment().setItemInMainHand(weapon);
            hero.getEquipment().setItemInMainHandDropChance(0.0f);
        }

        // Store hero side and the effective type ("zombie") in persistent data
        hero.getPersistentDataContainer().set(HERO_SIDE_KEY, PersistentDataType.STRING, side.name());
        hero.getPersistentDataContainer().set(HERO_TYPE_KEY, PersistentDataType.STRING, "zombie"); // Always "zombie" now

        player.sendMessage(side.getDisplayColor() + "A " + heroName + " has been summoned to the " + side.name() + " side!");
        plugin.getLogger().info("Spawned " + side.name() + " hero: " + heroName + " (EntityType: "+ entityTypeToSpawn.name() + ") for player " + player.getName() + " with weapon: " + (weapon != null ? weapon.getType() : "none"));

        // Force target nearby opposing hero
        if (hero instanceof org.bukkit.entity.Mob) {
            org.bukkit.entity.Mob mob = (org.bukkit.entity.Mob) hero;
            final double TARGET_RADIUS = 15.0; // Radius to scan for enemies
            java.util.List<org.bukkit.entity.Entity> nearbyEntities = mob.getNearbyEntities(TARGET_RADIUS, TARGET_RADIUS, TARGET_RADIUS);

            for (org.bukkit.entity.Entity nearbyEntity : nearbyEntities) {
                if (nearbyEntity instanceof org.bukkit.entity.LivingEntity && nearbyEntity.getUniqueId() != mob.getUniqueId()) {
                    org.bukkit.entity.LivingEntity potentialTarget = (org.bukkit.entity.LivingEntity) nearbyEntity;
                    org.bukkit.persistence.PersistentDataContainer targetPDC = potentialTarget.getPersistentDataContainer();

                    if (targetPDC.has(HERO_SIDE_KEY, PersistentDataType.STRING)) {
                        String targetSideStr = targetPDC.get(HERO_SIDE_KEY, PersistentDataType.STRING);
                        HeroSide targetSide = null;
                        try {
                            targetSide = HeroSide.valueOf(targetSideStr.toUpperCase());
                        } catch (IllegalArgumentException e) {
                            // Invalid side on potential target, skip
                            continue;
                        }

                        if (targetSide != side) { // Opposing sides
                            mob.setTarget(potentialTarget);
                            plugin.getLogger().info("HeroManager: Forcing newly spawned " + side.name() + " hero " + heroName +
                                                    " to target nearby opposing " + targetSide.name() + " hero " + potentialTarget.getName());
                            break; // Target the first one found
                        }
                    }
                }
            }
        }
    }
}
