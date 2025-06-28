package net.blockx.core;

import net.blockx.abilities.AbilityManager;
import net.blockx.abilities.BarbarianAxeAbility;
import net.blockx.commands.CommandHandler;
import net.blockx.heroes.HeroManager; // Import HeroManager
import net.blockx.items.CustomItemManager;
import net.blockx.listeners.HeroPickupListener; // Import HeroPickupListener
import net.blockx.listeners.PlayerEventListener;


import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
// import org.bukkit.block.Block; // No longer directly used here
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public final class Blockx extends JavaPlugin implements Listener {

    private CustomItemManager customItemManager;
    private BarbarianAxeAbility barbarianAxeAbility;
    private AbilityManager abilityManager;
    private HeroManager heroManager; // Added HeroManager instance
    // HeroPickupListener doesn't need to be a field if only instantiated and registered

    @Override
    public void onEnable() {
        getLogger().info("Blockx Plugin Enabled (core package)");

        // Initialize Managers
        this.customItemManager = new CustomItemManager(this);
        this.barbarianAxeAbility = new BarbarianAxeAbility(this, this.customItemManager); // Assuming this is still needed
        this.abilityManager = new AbilityManager(this, this.customItemManager);
        this.heroManager = new HeroManager(this, this.customItemManager); // Initialize HeroManager

        // Register Command Executor
        // CommandHandler constructor now expects Blockx instance, CustomItemManager, and HeroManager
        this.getCommand("xget").setExecutor(new CommandHandler(this, this.customItemManager, this.heroManager));

        // Register Event Listeners
        getServer().getPluginManager().registerEvents(this, this); // For Blockx's own @EventHandlers
        getServer().getPluginManager().registerEvents(new PlayerEventListener(this.abilityManager), this);
        getServer().getPluginManager().registerEvents(new HeroPickupListener(this, this.customItemManager), this); // Register HeroPickupListener

        createUltraCraftingTableRecipe(); // Assuming this is still relevant
        getLogger().info("Blockx Systems Initialized (core package).");
    }

    @Override
    public void onDisable() {
        getLogger().info("Blockx Plugin Disabled (core package)");
    }

    private ItemStack getUltraCraftingItem() {
        ItemStack ultraItem = new ItemStack(Material.STONE);
        ItemMeta meta = ultraItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("Ultra Crafting Table");
            meta.setCustomModelData(1001);
            NamespacedKey key = new NamespacedKey(this, "ultra_crafting_block");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "ultra_crafting_table");
            ultraItem.setItemMeta(meta);
        }
        return ultraItem;
    }

    private void createUltraCraftingTableRecipe() {
        ItemStack ultraCraftingTable = getUltraCraftingItem();
        if (ultraCraftingTable.getItemMeta() == null) return;

        NamespacedKey key = new NamespacedKey(this, "ultra_crafting_table_recipe");
        if (Bukkit.getRecipe(key) != null) {
            return;
        }

        ShapedRecipe recipe = new ShapedRecipe(key, ultraCraftingTable);
        recipe.shape("GGG", "GCG", "GGG");
        recipe.setIngredient('G', Material.GOLD_INGOT);
        recipe.setIngredient('C', Material.CRAFTING_TABLE);
        Bukkit.addRecipe(recipe);
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        ItemStack craftedItem = event.getRecipe().getResult();
        if (craftedItem == null || !craftedItem.hasItemMeta()) return;
        ItemMeta meta = craftedItem.getItemMeta();

        if ("Ultra Crafting Table".equals(meta.getDisplayName())) {
             NamespacedKey pdcKey = new NamespacedKey(this, "ultra_crafting_block");
             if (meta.getPersistentDataContainer().has(pdcKey, PersistentDataType.STRING)) {
                ItemStack newResult = craftedItem.clone();
                ItemMeta newMeta = newResult.getItemMeta();
                if (newMeta != null) {
                    newMeta.setLore(List.of("An ultra-powerful crafting table!"));
                    newResult.setItemMeta(newMeta);
                    event.getInventory().setResult(newResult);
                }
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack itemInHand = event.getItemInHand();
        if (itemInHand == null || !itemInHand.hasItemMeta()) return;
        ItemMeta meta = itemInHand.getItemMeta();

        NamespacedKey key = new NamespacedKey(this, "ultra_crafting_block");
        if (meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
            String tagValue = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
            if ("ultra_crafting_table".equals(tagValue)) {
                 getLogger().info("Ultra Crafting Table placed with custom texture.");
            }
        }
    }

    // Barbarian Axe specific interaction is still handled here.
    // General sword interactions (damage, right-click for other abilities) are handled by PlayerEventListener -> AbilityManager.
    @EventHandler
    public void onBarbarianAxeInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (barbarianAxeAbility != null && barbarianAxeAbility.isItemBarbarianAxe(itemInHand)) {
            // BarbarianAxeAbility's methods (startCharging, releaseCharge) internally check for right-click context if needed.
            // This event handler is specific to the Barbarian Axe item.
            if (event.getAction().name().contains("RIGHT_CLICK")) { // Ensure it's a right click
                if (barbarianAxeAbility.isCharging(player)) {
                    barbarianAxeAbility.releaseCharge(player);
                } else if (!barbarianAxeAbility.isOnCooldown(player)) { // Check cooldown before starting charge
                    barbarianAxeAbility.startCharging(player);
                }
                 // event.setCancelled(true); // Consider if cancelling the event is needed for the axe.
            }
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        if (barbarianAxeAbility != null) {
            barbarianAxeAbility.handlePlayerQuit(event.getPlayer());
        }
        // if (abilityManager != null && abilityManager instanceof SomePlayerQuitHandlerInterface) {
        //    ((SomePlayerQuitHandlerInterface)abilityManager).handlePlayerQuit(event.getPlayer());
        // }
        // For now, AbilityManager doesn't have specific onQuit logic needing to be called.
    }
}
