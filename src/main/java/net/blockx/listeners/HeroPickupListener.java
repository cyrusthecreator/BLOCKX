package net.blockx.listeners;

import net.blockx.core.Blockx;
import net.blockx.heroes.HeroManager; // For the CUSTOM_HERO_TAG_KEY
import net.blockx.items.CustomItemManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class HeroPickupListener implements Listener {

    private final Blockx plugin;
    private final CustomItemManager customItemManager;

    public HeroPickupListener(Blockx plugin, CustomItemManager customItemManager) {
        this.plugin = plugin;
        this.customItemManager = customItemManager;
    }

    @EventHandler
    public void onHeroPickupItem(EntityPickupItemEvent event) {
        LivingEntity entity = event.getEntity();

        // Check if the entity is a Zombie (or could be any LivingEntity if heroes can be other types)
        if (!(entity instanceof Zombie)) {
            return; // Not a Zombie, so not one of our current heroes
        }

        PersistentDataContainer pdc = entity.getPersistentDataContainer();

        // Check if the entity is tagged as one of our custom heroes by checking for HERO_TYPE_KEY
        if (pdc.has(HeroManager.HERO_TYPE_KEY, PersistentDataType.STRING)) {
            String heroType = pdc.get(HeroManager.HERO_TYPE_KEY, PersistentDataType.STRING);
            // plugin.getLogger().info("Hero (" + heroType + ") attempting to pick up: " + event.getItem().getItemStack().getType());

            ItemStack itemStack = event.getItem().getItemStack();
            String customItemId = customItemManager.getCustomItemId(itemStack);

            if (customItemId != null) {
                // This is a custom item from our plugin. Allow pickup.
                // plugin.getLogger().info("Hero allowed to pick up custom item: " + itemStack.getType() + " (ID: " + customItemId + ")");
                // Event is allowed by default, so no action needed here to allow it.
            } else {
                // This is NOT a custom item from our plugin. Prevent pickup.
                // plugin.getLogger().info("Hero PREVENTED from picking up non-custom item: " + itemStack.getType());
                event.setCancelled(true);
            }
        }
        // If not tagged as CUSTOM_HERO_TAG_KEY, it's a regular mob, so we don't interfere with its pickup logic.
    }
}
