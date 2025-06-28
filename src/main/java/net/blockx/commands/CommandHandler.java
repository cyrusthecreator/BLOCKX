package net.blockx.commands;

import net.blockx.core.Blockx; // Required for HeroManager and plugin instance
import net.blockx.heroes.HeroManager;
import net.blockx.items.CustomItem;
import net.blockx.items.CustomItemManager;
import net.blockx.items.CustomSwordType; // For parsing weapon names
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
// import org.bukkit.plugin.java.JavaPlugin; // No longer directly needed if Blockx instance is passed

public class CommandHandler implements CommandExecutor {

    private final Blockx plugin; // Store Blockx instance to access managers
    private final CustomItem customItemProvider;
    private final CustomItemManager customItemManager;
    private final HeroManager heroManager;

    public CommandHandler(Blockx plugin, CustomItemManager customItemManager, HeroManager heroManager) {
        this.plugin = plugin;
        this.customItemManager = customItemManager;
        this.heroManager = heroManager;
        // CustomItem now takes Blockx plugin instance and CustomItemManager
        this.customItemProvider = new CustomItem(plugin, customItemManager);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("xget")) {
            return false;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
            return true;
        }

        if (args.length == 0) {
            customItemProvider.showHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        if ("summon".equalsIgnoreCase(subCommand)) {
            if (args.length < 2) {
                player.sendMessage(ChatColor.RED + "Usage: /xget summon <hero_type>[:<weapon_name>]");
                return true;
            }
            // args[1] should be hero_type or hero_type:weapon_name
            handleSummonCommand(player, args);
            return true;
        } else {
            // Delegate to existing item handling or help
            customItemProvider.handleItemCommand(args, sender);
            return true;
        }
    }

    private void handleSummonCommand(Player player, String[] args) {
        String heroAndWeaponArg = args[1];
        String[] parts = heroAndWeaponArg.split(":", 2);
        String heroType = parts[0].toLowerCase();
        ItemStack weaponStack = null;

        if (parts.length > 1) {
            String weaponName = parts[1].toLowerCase();
            // Attempt to parse as a CustomSwordType first
            CustomSwordType swordType = CustomSwordType.fromString(weaponName);
            if (swordType != null) {
                // Generate item but don't give to player
                weaponStack = customItemManager.generateItem(null, swordType.getCustomModelData(), swordType.getDisplayName(), swordType.getItemAttributes(), swordType.getBaseMaterial());
            } else {
                // Handle other specific non-sword items that can be weapons
                // For now, let's assume barbarian_axe is a valid weapon.
                // This part needs to be aligned with what CustomItem.java can provide.
                // We might need a method in CustomItemManager or CustomItem to get an item by name without giving it to a player.
                // For simplicity, directly using generateItem with null player.
                if ("barbarian_axe".equals(weaponName)) {
                     weaponStack = customItemManager.generateItem(null, 12301, "&cBarbarian Axe", new net.blockx.items.ItemAttributes() /* placeholder, get real attributes */, Material.IRON_AXE);
                     // TODO: Fetch proper attributes for barbarian_axe if needed for the hero.
                     // For now, CustomItemManager.generateItem creates it with default if attributes are complex to get here.
                } else {
                    player.sendMessage(ChatColor.RED + "Unknown weapon: " + weaponName);
                    // Optional: proceed to summon hero without weapon, or return
                    // return;
                }
            }
        }

        // Basic validation for heroType, expand as necessary
        if ("zombie".equals(heroType.toLowerCase())) {
            plugin.getLogger().warning("Player " + player.getName() + " used deprecated /xget summon for ZOMBIE. Defaulting to RED side. Use /xsummon for side selection.");
            heroManager.spawnHero(player, heroType, net.blockx.heroes.HeroSide.RED, weaponStack);
        } else if ("villager".equals(heroType.toLowerCase())) {
            plugin.getLogger().warning("Player " + player.getName() + " used deprecated /xget summon for VILLAGER. 'Villager' type will be a BLUE Zombie. Use /xsummon for specific side and type control.");
            // HeroManager will handle making it a BLUE Zombie.
            heroManager.spawnHero(player, heroType, net.blockx.heroes.HeroSide.BLUE, weaponStack); // Pass BLUE explicitly here for clarity
        } else {
            player.sendMessage(ChatColor.RED + "Unknown hero type: " + heroType + ". Supported types: zombie, villager (summons a Blue Zombie).");
        }
    }
}
