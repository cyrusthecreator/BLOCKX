package net.blockx.commands;

import net.blockx.core.Blockx;
import net.blockx.heroes.HeroManager;
import net.blockx.heroes.HeroSide; // Will be created in a later step
import net.blockx.items.CustomItemManager;
import net.blockx.items.CustomSwordType;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class XSummonCommandHandler implements CommandExecutor {

    private final Blockx plugin;
    private final CustomItemManager customItemManager;
    private final HeroManager heroManager;

    public XSummonCommandHandler(Blockx plugin, CustomItemManager customItemManager, HeroManager heroManager) {
        this.plugin = plugin;
        this.customItemManager = customItemManager;
        this.heroManager = heroManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Usage: /xsummon <hero_type>:<side>[:<weapon_name>]");
            return true;
        }

        // Argument format: <hero_type>:<side>[:<weapon_name>]
        // Example: zombie:red:sword or villager:blue
        String[] parts = args[0].split(":", 3);

        if (parts.length < 2) {
            player.sendMessage(ChatColor.RED + "Invalid format. Usage: /xsummon <hero_type>:<side>[:<weapon_name>]");
            return true;
        }

        String heroTypeStr = parts[0].toLowerCase();
        String sideStr = parts[1].toUpperCase();
        String weaponName = null;
        if (parts.length > 2) {
            weaponName = parts[2].toLowerCase();
        }

        HeroSide side;
        try {
            side = HeroSide.valueOf(sideStr);
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Invalid side: " + sideStr + ". Use RED or BLUE.");
            return true;
        }

        // Validate heroType (simple validation for now)
        if (!"zombie".equals(heroTypeStr) && !"villager".equals(heroTypeStr)) {
            player.sendMessage(ChatColor.RED + "Unknown hero type: " + heroTypeStr + ". Supported types: zombie, villager.");
            return true;
        }

        ItemStack weaponStack = null;
        if (weaponName != null && !weaponName.isEmpty()) {
            CustomSwordType swordType = CustomSwordType.fromString(weaponName);
            if (swordType != null) {
                weaponStack = customItemManager.generateItem(null, swordType.getCustomModelData(), swordType.getDisplayName(), swordType.getItemAttributes(), swordType.getBaseMaterial());
            } else if ("barbarian_axe".equals(weaponName)) { // Example for other weapon types
                // This needs to align with how CustomItemManager provides items.
                // Assuming a method or direct creation logic here.
                 weaponStack = customItemManager.generateItem(null, 12301, "&cBarbarian Axe", new net.blockx.items.ItemAttributes(), Material.IRON_AXE);
            } else {
                player.sendMessage(ChatColor.YELLOW + "Unknown weapon: " + weaponName + ". Hero will be summoned without a specific weapon.");
                // Proceed to summon without weapon, or handle as an error if preferred.
            }
        }

        // Pass heroTypeStr instead of a specific EntityType for now. HeroManager will handle it.
        heroManager.spawnHero(player, heroTypeStr, side, weaponStack); // Will update spawnHero later

        return true;
    }
}
