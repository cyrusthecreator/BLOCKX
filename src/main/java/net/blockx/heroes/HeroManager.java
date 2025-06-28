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

        EntityType actualEntityType = EntityType.ZOMBIE; // Default to Zombie
        String displayHeroType = heroTypeStr; // The type to display in messages/name

        // Determine actual EntityType and display name based on heroTypeStr
        String requestedTypeLower = heroTypeStr.toLowerCase();
        if ("villager".equals(requestedTypeLower)) {
            // If "villager" is requested, spawn a Zombie but identify it as "Villager" for naming and side BLUE.
            actualEntityType = EntityType.ZOMBIE;
            displayHeroType = "Villager"; // Keep "Villager" for naming conventions
            if (side != HeroSide.BLUE) {
                // Enforce that "villager" hero type is always BLUE side, even if another side is specified.
                // Or, we could allow Red Villager (Zombie). For now, let's suggest Blue.
                player.sendMessage(ChatColor.YELLOW + "Note: 'Villager' heroes are always on the BLUE side. Spawning as BLUE Zombie.");
                side = HeroSide.BLUE;
            }
        } else if (!"zombie".equals(requestedTypeLower)) {
            player.sendMessage(ChatColor.RED + "Unsupported hero type for spawning: " + heroTypeStr + ". Defaulting to Zombie.");
            // Fallthrough to use Zombie, displayHeroType will remain heroTypeStr or could be "Zombie"
            displayHeroType = "Zombie"; // Correct display type if defaulting
        }


        org.bukkit.entity.LivingEntity hero = (org.bukkit.entity.LivingEntity) spawnLocation.getWorld().spawnEntity(spawnLocation, actualEntityType);

        // Set a custom name based on side and the *display* hero type
        String heroName = side.name() + " " + displayHeroType.substring(0, 1).toUpperCase() + displayHeroType.substring(1);
        if (weapon != null && weapon.hasItemMeta()) {
            ItemMeta weaponMeta = weapon.getItemMeta();
            if (weaponMeta != null && weaponMeta.hasDisplayName()) {
                String cleanWeaponName = ChatColor.stripColor(weaponMeta.getDisplayName());
                heroName = side.name() + " " + cleanWeaponName + " " + displayHeroType.substring(0, 1).toUpperCase() + displayHeroType.substring(1);
            }
        }
        hero.setCustomName(side.getDisplayColor() + heroName);
        hero.setCustomNameVisible(true);

        hero.setCanPickupItems(true);

        if (weapon != null && hero.getEquipment() != null) {
            hero.getEquipment().setItemInMainHand(weapon);
            hero.getEquipment().setItemInMainHandDropChance(0.0f);
        }

        // Store hero side and *requested* type (or effective type) in persistent data
        // Storing `heroTypeStr.toLowerCase()` to reflect the command used,
        // or `displayHeroType.toLowerCase()` if we want to store the "effective" type.
        // Let's store the originally requested type for clarity if needed later.
        hero.getPersistentDataContainer().set(HERO_SIDE_KEY, PersistentDataType.STRING, side.name());
        hero.getPersistentDataContainer().set(HERO_TYPE_KEY, PersistentDataType.STRING, heroTypeStr.toLowerCase());


        player.sendMessage(side.getDisplayColor() + "A " + heroName + " has been summoned to the " + side.name() + " side!");
        plugin.getLogger().info("Spawned " + side.name() + " hero: " + heroName + " (ActualType: "+ actualEntityType.name() +", RequestedType: " + heroTypeStr + ") for player " + player.getName() + " with weapon: " + (weapon != null ? weapon.getType() : "none"));
    }
}
