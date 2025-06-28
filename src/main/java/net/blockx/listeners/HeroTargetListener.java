package net.blockx.listeners;

import net.blockx.heroes.HeroManager;
import net.blockx.heroes.HeroSide;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class HeroTargetListener implements Listener {

    private final JavaPlugin plugin;

    public HeroTargetListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onHeroTarget(EntityTargetLivingEntityEvent event) {
        plugin.getLogger().info("[HeroTargetListener] Method onHeroTarget CALLED. Event: " + event.getEventName()); // BASIC ENTRY LOG

        Entity entity = event.getEntity();
        LivingEntity target = event.getTarget();

        if (target == null) {
            plugin.getLogger().info("[HeroTargetListener] Target is null. Exiting.");
            return;
        }

        plugin.getLogger().info("[HeroTargetListener] Processing: " + entity.getName() + " (" + entity.getType() + ") trying to target " + target.getName() + " (" + target.getType() + ")");

        // Check if the targeting entity is a custom hero
        PersistentDataContainer entityPDC = entity.getPersistentDataContainer();
        if (!entityPDC.has(HeroManager.HERO_SIDE_KEY, PersistentDataType.STRING)) {
            plugin.getLogger().info("[HeroTargetListener] Attacker " + entity.getName() + " is NOT a custom hero. Allowing default behavior.");
            return; // Not a custom hero, default behavior
        }

        String entitySideStr = entityPDC.get(HeroManager.HERO_SIDE_KEY, PersistentDataType.STRING);
        HeroSide entitySide;
        try {
            entitySide = HeroSide.valueOf(entitySideStr.toUpperCase()); // Use toUpperCase() for safety
        } catch (IllegalArgumentException e) {
            plugin.getLogger().severe("[HeroTargetListener] Attacker " + entity.getName() + " has invalid side data: '" + entitySideStr + "'. Cancelling event.");
            event.setCancelled(true); // Prevent unknown side from acting
            return;
        }
        plugin.getLogger().info("[HeroTargetListener] Attacker " + entity.getName() + " is on side: " + entitySide);

        // Check if the target is another custom hero
        PersistentDataContainer targetPDC = target.getPersistentDataContainer();
        if (targetPDC.has(HeroManager.HERO_SIDE_KEY, PersistentDataType.STRING)) {
            String targetSideStr = targetPDC.get(HeroManager.HERO_SIDE_KEY, PersistentDataType.STRING);
            HeroSide targetSide;
            try {
                targetSide = HeroSide.valueOf(targetSideStr.toUpperCase()); // Use toUpperCase() for safety
            } catch (IllegalArgumentException e) {
                plugin.getLogger().severe("[HeroTargetListener] Target " + target.getName() + " has invalid side data: '" + targetSideStr + "'. Cancelling event.");
                event.setCancelled(true); // Prevent attacking hero with unknown side
                return;
            }
            plugin.getLogger().info("[HeroTargetListener] Target " + target.getName() + " is also a hero, on side: " + targetSide);

            // If they are on the same side, cancel the targeting
            if (entitySide == targetSide) {
                plugin.getLogger().info("[HeroTargetListener] Attacker and Target are on the SAME side (" + entitySide + "). Cancelling event.");
                event.setCancelled(true);
            } else {
                plugin.getLogger().info("[HeroTargetListener] Attacker and Target are on OPPOSING sides (" + entitySide + " vs " + targetSide + "). Allowing event.");
                // If they are on opposing sides, allow targeting (default event behavior)
                // No explicit 'allow' needed, just don't cancel.
            }
        } else {
            plugin.getLogger().info("[HeroTargetListener] Target " + target.getName() + " is NOT a custom hero. Cancelling event.");
            // If the target is NOT a custom hero (e.g., a player, regular mob, etc.),
            // our custom heroes should not target them.
            event.setCancelled(true);
        }
    }
}
