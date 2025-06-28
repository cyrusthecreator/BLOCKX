package net.blockx.items;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;
// No List import needed here if showHelp is self-contained or uses CustomSwordType

public class CustomItem {

    private final CustomItemManager itemManager;
    private final JavaPlugin plugin;

    public CustomItem(JavaPlugin plugin, CustomItemManager itemManager) {
        this.plugin = plugin;
        this.itemManager = itemManager;
    }

    public void handleItemCommand(String[] args, CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command to get items.");
            return;
        }

        String itemNameArg = args[0].toLowerCase();

        if ("help".equalsIgnoreCase(itemNameArg)) {
            showHelp(player);
            return;
        }
        if ("point".equalsIgnoreCase(itemNameArg)) {
            executePointEmote(player); // Assuming WHITE_ASH is a valid particle or will be fixed
            return;
        }

        // Try to parse as a CustomSwordType
        CustomSwordType swordType = CustomSwordType.fromString(itemNameArg);
        if (swordType != null) {
            itemManager.generateItem(player, swordType.getCustomModelData(), swordType.getDisplayName(), swordType.getItemAttributes(), swordType.getBaseMaterial());
            return;
        }

        // Handle other specific non-sword items
        switch (itemNameArg) {
            case "barbarian_axe":
                itemManager.generateItem(player, 12301, "&cBarbarian Axe", getBarbarianAxeAttributes(), Material.IRON_AXE);
                break;
            case "shield":
                itemManager.generateItem(player, 12305, "&eCustom Shield", getShieldAttributes(), Material.SHIELD);
                break;
            default:
                player.sendMessage(ChatColor.RED + "Unknown item: " + args[0] + ". Use '/xget help' for a list of items.");
                break;
        }
    }

    public void showHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "--- Blockx Custom Items ---");
        player.sendMessage(ChatColor.YELLOW + "Use " + ChatColor.AQUA + "/xget <item_name>" + ChatColor.YELLOW + " to get an item.");
        player.sendMessage(ChatColor.GRAY + "Available items:");
        // Dynamically list swords from CustomSwordType enum
        for (CustomSwordType type : CustomSwordType.values()) {
            // Making the command lowercase and removing underscores for user-friendliness
            player.sendMessage(ChatColor.WHITE + "- " + type.name().toLowerCase().replace("_", ""));
        }
        player.sendMessage(ChatColor.WHITE + "- barbarian_axe");
        player.sendMessage(ChatColor.WHITE + "- shield");
        player.sendMessage(ChatColor.WHITE + "- point (emote)");
    }

    private void executePointEmote(Player player) {
        Location eyeLocation = player.getEyeLocation();
        Location targetLocation = eyeLocation.clone().add(eyeLocation.getDirection().multiply(10));

        // Particle.WHITE_ASH will be checked in compilation fixes step.
        // For now, assuming it's valid or will be replaced.
        player.getWorld().spawnParticle(Particle.ASH, targetLocation, 20, 0.2, 0.2, 0.2, 0.05); // Changed WHITE_ASH to ASH as a common alternative
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        player.sendMessage(ChatColor.GREEN + "You pointed forward!");
    }

    // Attribute methods for non-sword items remain here
    private ItemAttributes getShieldAttributes() {
        ItemAttributes attributes = new ItemAttributes();
        attributes.addLore("&7A sturdy custom shield.");
        // Example: attributes.addAttribute(Attribute.GENERIC_ARMOR, 2, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.OFF_HAND);
        return attributes;
    }

    private ItemAttributes getBarbarianAxeAttributes() {
        ItemAttributes attributes = new ItemAttributes();
        attributes.addLore("&eA brutal axe forged for fierce warriors.");
        attributes.addLore("&7Bonus damage & block breaking ability.");
        attributes.addAttribute(Attribute.GENERIC_ATTACK_DAMAGE, 10.0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);
        attributes.addAttribute(Attribute.GENERIC_ATTACK_SPEED, -3.0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);
        return attributes;
    }

    // Sword attribute methods (getGoliathSwordAttributes, etc.) are removed
    // as this logic is now within CustomSwordType enum.
}
