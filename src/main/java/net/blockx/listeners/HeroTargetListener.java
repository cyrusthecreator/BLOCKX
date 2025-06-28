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
        // CRITICAL FIX for NullPointerException: event.getTarget() can be null if the mob is clearing its target.
        if (target == null) {
            plugin.getLogger().info("[HeroTargetListener] Target is null (e.g., mob clearing target). Attacker " + attackerName + " is not targeting anything specific now. Event likely to be cancelled by mob's AI or further logic if needed.");
            // If a hero was targeting something that disappeared, and now has a null target,
            // it effectively isn't targeting a specific entity that our rules would apply to.
            // The event might still be useful for some AI logic (like looking for a new target),
            // but our hero-vs-hero or hero-vs-other rules don't apply to a null target.
            // We might let the event proceed if the mob is just clearing its target,
            // or cancel if a hero should always have a target or specific behavior when losing one.
            // For now, if target is null, let the event proceed as the mob is likely just resetting its AI state.
            // However, the vanilla setTarget(null) call itself triggers this event.
            // If the attacker is a hero, and target is null, it means it *was* targeting something.
            // We don't want it to target "nothing" in a way that bypasses our rules later.
            // It's safer to assume if a hero is involved in a target event, and the target becomes null,
            // this specific event doesn't need our intervention beyond noting it.
            // The mob's AI or our HeroAIScheduler will find a new valid target.
            // No cancellation needed here as there's no "target" to apply rules against.
            return;
        }

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
