package net.blockx.ai;

import net.blockx.core.Blockx;
import org.bukkit.scheduler.BukkitTask;

public class HeroAIScheduler {

    private final Blockx plugin;
    private BukkitTask aiTask;
    private final long TASK_PERIOD_TICKS = 30L; // Run every 1.5 seconds (30 ticks)

    public HeroAIScheduler(Blockx plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (aiTask != null && !aiTask.isCancelled()) {
            plugin.getLogger().warning("HeroAIScheduler task is already running. Cannot start again.");
            return;
        }

        // The actual AI logic will be in this run method in the next step
        aiTask = plugin.getServer().getScheduler().runTaskTimer(plugin, this::runAILogic, 0L, TASK_PERIOD_TICKS);
        plugin.getLogger().info("HeroAIScheduler started with a period of " + TASK_PERIOD_TICKS + " ticks.");
    }

    public void stop() {
        if (aiTask != null) {
            if (!aiTask.isCancelled()) {
                aiTask.cancel();
                plugin.getLogger().info("HeroAIScheduler task cancelled successfully.");
            }
            aiTask = null;
        } else {
            plugin.getLogger().info("HeroAIScheduler task was not running or already stopped.");
        }
    }

    private void runAILogic() {
        // plugin.getLogger().info("[HeroAIScheduler] AI Tick - Scanning for targets...");

        java.util.List<org.bukkit.entity.LivingEntity> redHeroes = new java.util.ArrayList<>();
        java.util.List<org.bukkit.entity.LivingEntity> blueHeroes = new java.util.ArrayList<>();

        for (org.bukkit.World world : plugin.getServer().getWorlds()) {
            for (org.bukkit.entity.LivingEntity entity : world.getLivingEntities()) {
                if (!(entity instanceof org.bukkit.entity.Mob)) {
                    continue;
                }
                org.bukkit.persistence.PersistentDataContainer pdc = entity.getPersistentDataContainer();
                if (pdc.has(net.blockx.heroes.HeroManager.HERO_SIDE_KEY, org.bukkit.persistence.PersistentDataType.STRING)) {
                    String sideStr = pdc.get(net.blockx.heroes.HeroManager.HERO_SIDE_KEY, org.bukkit.persistence.PersistentDataType.STRING);
                    try {
                        net.blockx.heroes.HeroSide side = net.blockx.heroes.HeroSide.valueOf(sideStr.toUpperCase());
                        if (side == net.blockx.heroes.HeroSide.RED) {
                            redHeroes.add(entity);
                        } else if (side == net.blockx.heroes.HeroSide.BLUE) {
                            blueHeroes.add(entity);
                        }
                    } catch (IllegalArgumentException e) {
                        // Invalid side data, ignore this entity for AI processing
                    }
                }
            }
        }

        // Process Red Heroes targeting Blue Heroes
        processSideTargeting(redHeroes, blueHeroes, net.blockx.heroes.HeroSide.BLUE);

        // Process Blue Heroes targeting Red Heroes
        processSideTargeting(blueHeroes, redHeroes, net.blockx.heroes.HeroSide.RED);
    }

    private void processSideTargeting(java.util.List<org.bukkit.entity.LivingEntity> attackers,
                                      java.util.List<org.bukkit.entity.LivingEntity> potentialTargets,
                                      net.blockx.heroes.HeroSide targetSideEnum) {

        final double MAX_ENGAGEMENT_RADIUS = 20.0; // Max distance to look for targets
        final double MAX_ENGAGEMENT_RADIUS_SQUARED = MAX_ENGAGEMENT_RADIUS * MAX_ENGAGEMENT_RADIUS;

        for (org.bukkit.entity.LivingEntity attackerEntity : attackers) {
            org.bukkit.entity.Mob attackerMob = (org.bukkit.entity.Mob) attackerEntity; // Already checked instanceof Mob

            org.bukkit.entity.LivingEntity currentTarget = attackerMob.getTarget();
            boolean needsNewTarget = false;

            if (currentTarget == null || currentTarget.isDead()) {
                needsNewTarget = true;
            } else {
                // Check if current target is still a valid enemy hero
                org.bukkit.persistence.PersistentDataContainer targetPDC = currentTarget.getPersistentDataContainer();
                if (!targetPDC.has(net.blockx.heroes.HeroManager.HERO_SIDE_KEY, org.bukkit.persistence.PersistentDataType.STRING)) {
                    needsNewTarget = true; // Target is no longer a hero
                } else {
                    try {
                        net.blockx.heroes.HeroSide currentTargetSide = net.blockx.heroes.HeroSide.valueOf(targetPDC.get(net.blockx.heroes.HeroManager.HERO_SIDE_KEY, org.bukkit.persistence.PersistentDataType.STRING).toUpperCase());
                        if (currentTargetSide != targetSideEnum) {
                            needsNewTarget = true; // Target is no longer on the correct opposing side
                        }
                        // Check distance if needed, but mob AI usually handles this once target is set.
                        // if (attackerMob.getLocation().distanceSquared(currentTarget.getLocation()) > MAX_ENGAGEMENT_RADIUS_SQUARED * 1.5) { // *1.5 for some buffer
                        //     needsNewTarget = true; // Target is too far
                        // }
                    } catch (IllegalArgumentException e) {
                        needsNewTarget = true; // Invalid side data on target
                    }
                }
            }

            if (needsNewTarget) {
                org.bukkit.entity.LivingEntity bestTarget = null;
                double minDistanceSquared = MAX_ENGAGEMENT_RADIUS_SQUARED;

                for (org.bukkit.entity.LivingEntity potentialTarget : potentialTargets) {
                    if (potentialTarget.isDead() || potentialTarget.equals(attackerMob)) {
                        continue;
                    }
                    // Ensure potential target is still valid (e.g. correct side, still alive - already checked by isDead)
                    // Redundant side check here as potentialTargets list is already filtered, but good for safety if list source changes

                    double distanceSquared = attackerMob.getLocation().distanceSquared(potentialTarget.getLocation());
                    if (distanceSquared < minDistanceSquared) {
                        minDistanceSquared = distanceSquared;
                        bestTarget = potentialTarget;
                    }
                }

                if (bestTarget != null) {
                    attackerMob.setTarget(bestTarget);
                    plugin.getLogger().info("[HeroAIScheduler] Hero " + attackerMob.getName() + " is now targeting " + bestTarget.getName());
                } else {
                    // No target found, ensure current target is cleared if it was invalid
                    if (attackerMob.getTarget() != null) { // Only clear if it had one, to avoid unnecessary event spam
                         attackerMob.setTarget(null);
                         // plugin.getLogger().info("[HeroAIScheduler] Hero " + attackerMob.getName() + " cleared target (no valid enemies found).");
                    }
                }
            }
        }
    }
}
