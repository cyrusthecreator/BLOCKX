package net.blockx.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class HeroTargetListener implements Listener {

    private final JavaPlugin plugin;

    public HeroTargetListener(JavaPlugin plugin) {
        this.plugin = plugin;
        this.plugin.getLogger().info("[HeroTargetListener] CONSTRUCTOR CALLED - Listener instantiated.");
    }

    @EventHandler
    public void onHeroTarget(EntityTargetLivingEntityEvent event) {
        Entity attacker = event.getEntity();
        LivingEntity target = event.getTarget();

        String attackerName = attacker != null ? attacker.getName() : "N/A";
        String attackerType = attacker != null ? attacker.getType().name() : "N/A";
        String targetName = target != null ? target.getName() : "N/A";
        String targetType = target != null ? target.getType().name() : "N/A";

        plugin.getLogger().info(
            "[HeroTargetListener] onHeroTarget CALLED. Event: " + event.getEventName() +
            ", Attacker: " + attackerName + " (" + attackerType + ")" +
            ", Target: " + targetName + " (" + targetType + ")"
        );

        // Step 4: Re-introduce Hero Identification Logic for the attacker
        org.bukkit.persistence.PersistentDataContainer attackerPDC = attacker.getPersistentDataContainer();
        if (!attackerPDC.has(net.blockx.heroes.HeroManager.HERO_SIDE_KEY, org.bukkit.persistence.PersistentDataType.STRING)) {
            plugin.getLogger().info("[HeroTargetListener] Attacker " + attackerName + " is NOT a custom hero. Allowing default behavior.");
            return;
        }

        String attackerSideStr = attackerPDC.get(net.blockx.heroes.HeroManager.HERO_SIDE_KEY, org.bukkit.persistence.PersistentDataType.STRING);
        net.blockx.heroes.HeroSide attackerSide;
        try {
            attackerSide = net.blockx.heroes.HeroSide.valueOf(attackerSideStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().severe("[HeroTargetListener] Attacker " + attackerName + " has invalid side data: '" + attackerSideStr + "'. Cancelling event.");
            event.setCancelled(true);
            return;
        }
        plugin.getLogger().info("[HeroTargetListener] Attacker " + attackerName + " IS a custom hero. Side: " + attackerSide);

        // Now check the target
        org.bukkit.persistence.PersistentDataContainer targetPDC = target.getPersistentDataContainer();
        if (targetPDC.has(net.blockx.heroes.HeroManager.HERO_SIDE_KEY, org.bukkit.persistence.PersistentDataType.STRING)) {
            // Target is also a custom hero
            String targetSideStr = targetPDC.get(net.blockx.heroes.HeroManager.HERO_SIDE_KEY, org.bukkit.persistence.PersistentDataType.STRING);
            net.blockx.heroes.HeroSide targetSide;
            try {
                targetSide = net.blockx.heroes.HeroSide.valueOf(targetSideStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                plugin.getLogger().severe("[HeroTargetListener] Target " + targetName + " (custom hero) has invalid side data: '" + targetSideStr + "'. Cancelling event for attacker " + attackerName);
                event.setCancelled(true);
                return;
            }
            plugin.getLogger().info("[HeroTargetListener] Target " + targetName + " IS a custom hero. Side: " + targetSide);

            if (attackerSide == targetSide) {
                plugin.getLogger().info("[HeroTargetListener] Attacker and Target are on the SAME side (" + attackerSide + "). Cancelling event.");
                event.setCancelled(true);
            } else {
                plugin.getLogger().info("[HeroTargetListener] Attacker and Target are on OPPOSING sides (" + attackerSide + " vs " + targetSide + "). Allowing event.");
                // Event proceeds, heroes will fight
            }
        } else {
            // Target is NOT a custom hero
            plugin.getLogger().info("[HeroTargetListener] Target " + targetName + " is NOT a custom hero. Custom hero " + attackerName + " will NOT target it. Cancelling event.");
            event.setCancelled(true);
        }
    }
}
