package net.blockx.abilities; // Updated package

import net.blockx.items.CustomItemManager; // Updated import

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult; // Added for getTargetLocation improvement

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BarbarianAxeAbility {

    private final JavaPlugin plugin;
    private final CustomItemManager customItemManager;

    private final Map<UUID, Long> playerChargeStartTimes = new HashMap<>();
    private final Map<UUID, Long> playerCooldownEndTimes = new HashMap<>();
    private final Map<UUID, BukkitTask> playerParticleTasks = new HashMap<>();

    // These constants were the more "enhanced" versions from previous work.
    private static final long COOLDOWN_DURATION_MS = 10000;
    private static final int MAX_TARGET_DISTANCE = 7;
    private static final int BLOCKS_TO_BREAK_RADIUS = 1;
    private static final float EXPLOSION_POWER_ENTITY_EFFECT = 2.5f;

    public BarbarianAxeAbility(JavaPlugin plugin, CustomItemManager customItemManager) {
        this.plugin = plugin;
        this.customItemManager = customItemManager;
    }

    public boolean isItemBarbarianAxe(ItemStack itemStack) {
        if (this.customItemManager == null) {
            this.plugin.getLogger().warning("CustomItemManager is null in BarbarianAxeAbility.isItemBarbarianAxe");
            return false;
        }
        return this.customItemManager.isSpecificCustomItem(itemStack, "12301", Material.IRON_AXE);
    }

    public boolean isCharging(Player player) {
        return playerChargeStartTimes.containsKey(player.getUniqueId());
    }

    public boolean isOnCooldown(Player player) {
        if (playerCooldownEndTimes.getOrDefault(player.getUniqueId(), 0L) > System.currentTimeMillis()) {
            long timeLeftMillis = playerCooldownEndTimes.get(player.getUniqueId()) - System.currentTimeMillis();
            player.sendActionBar(ChatColor.RED + "Axe ability on cooldown: " + String.format("%.1fs", timeLeftMillis / 1000.0));
            return true;
        }
        return false;
    }

    public void startCharging(Player player) {
        playerChargeStartTimes.put(player.getUniqueId(), System.currentTimeMillis());
        player.sendActionBar(ChatColor.GREEN + "Barbarian Axe charging...");
        player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 1.2f);

        if (playerParticleTasks.containsKey(player.getUniqueId())) {
            playerParticleTasks.get(player.getUniqueId()).cancel();
        }

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !isCharging(player) || !isItemBarbarianAxe(player.getInventory().getItemInMainHand())) {
                    cancelCharge(player);
                    return;
                }
                Location particleLoc = player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(0.5));
                player.getWorld().spawnParticle(Particle.FLAME, particleLoc, 1, 0.05, 0.05, 0.05, 0.005);
                player.getWorld().spawnParticle(Particle.SMOKE, particleLoc, 1, 0.05, 0.05, 0.05, 0.005); // Changed SMOKE_NORMAL to SMOKE
                if (System.currentTimeMillis() - playerChargeStartTimes.getOrDefault(player.getUniqueId(), System.currentTimeMillis()) > 500) {
                    player.getWorld().spawnParticle(Particle.LAVA, player.getLocation().add(0,1,0), 1);
                }
            }
        }.runTaskTimer(this.plugin, 0L, 2L);

        playerParticleTasks.put(player.getUniqueId(), task);
    }

    private void cleanupChargingState(Player player) {
        playerChargeStartTimes.remove(player.getUniqueId());
        BukkitTask existingTask = playerParticleTasks.remove(player.getUniqueId());
        if (existingTask != null) {
            existingTask.cancel();
        }
    }

    public void releaseCharge(Player player) {
        if (!isCharging(player)) {
            return;
        }

        long chargeDuration = System.currentTimeMillis() - playerChargeStartTimes.getOrDefault(player.getUniqueId(), System.currentTimeMillis());
        cleanupChargingState(player);

        if (chargeDuration < 200) { // Minimum charge time
            player.sendMessage(ChatColor.YELLOW + "Axe charge released too soon!");
            player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.7f, 1.5f);
            return;
        }

        Location targetLocation = getTargetImpactLocation(player, MAX_TARGET_DISTANCE);
        if (targetLocation != null) {
            triggerAbility(player, targetLocation);
            setCooldown(player);
        } else {
            player.sendMessage(ChatColor.YELLOW + "No valid target found for the axe ability.");
            player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 1f, 1f);
        }
    }

    private void triggerAbility(Player player, Location impactLocation) {
        World world = impactLocation.getWorld();
        if (world == null) return;

        plugin.getLogger().info(player.getName() + " used Barbarian Axe ability at " + impactLocation.toVector());
        player.sendActionBar(ChatColor.GOLD + "RRAAGH!");

        world.playSound(impactLocation, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.7f);
        world.playSound(impactLocation, Sound.BLOCK_STONE_BREAK, 1.2f, 0.5f);

        world.spawnParticle(Particle.EXPLOSION_EMITTER, impactLocation.clone().add(0.5, 0.5, 0.5), 1); // Changed EXPLOSION_LARGE to EXPLOSION_EMITTER
        world.spawnParticle(Particle.LAVA, impactLocation.clone().add(0.5, 0.5, 0.5), 20, 0.8, 0.8, 0.8, 0.15);
        world.spawnParticle(Particle.LARGE_SMOKE, impactLocation.clone().add(0.5, 0.5, 0.5), 30, 1.2, 1.2, 1.2, 0.08); // Changed SMOKE_LARGE to LARGE_SMOKE
        world.spawnParticle(Particle.ENCHANTED_HIT, impactLocation.clone().add(0.5,0.5,0.5), 25, 1,1,1, 0.2); // Changed CRIT_MAGIC to ENCHANTED_HIT

        int brokenCount = 0;
        List<Block> toBreak = new ArrayList<>();
        for (int x = -BLOCKS_TO_BREAK_RADIUS; x <= BLOCKS_TO_BREAK_RADIUS; x++) {
            for (int y = -BLOCKS_TO_BREAK_RADIUS; y <= BLOCKS_TO_BREAK_RADIUS; y++) {
                for (int z = -BLOCKS_TO_BREAK_RADIUS; z <= BLOCKS_TO_BREAK_RADIUS; z++) {
                    if (impactLocation.distanceSquared(impactLocation.clone().add(x,y,z)) <= BLOCKS_TO_BREAK_RADIUS * BLOCKS_TO_BREAK_RADIUS + 0.5) {
                         Block currentBlock = impactLocation.clone().add(x, y, z).getBlock();
                         if (currentBlock.getType().isSolid() && currentBlock.getType() != Material.BEDROCK && currentBlock.getType() != Material.BARRIER && !currentBlock.isLiquid()) {
                            toBreak.add(currentBlock);
                        }
                    }
                }
            }
        }

        Collections.shuffle(toBreak);
        for (int i=0; i < Math.min(toBreak.size(), 10); i++) {
            Block block = toBreak.get(i);
            // Changed BLOCK_CRACK to BLOCK, which takes BlockData
            world.spawnParticle(Particle.BLOCK, block.getLocation().clone().add(0.5,0.5,0.5), 30, 0.4, 0.4, 0.4, block.getBlockData());
            block.breakNaturally(new ItemStack(Material.IRON_AXE));
            brokenCount++;
        }

        if (brokenCount > 0) {
             player.sendMessage(ChatColor.GOLD + "Smashed " + brokenCount + " blocks!");
        }
        world.createExplosion(impactLocation, EXPLOSION_POWER_ENTITY_EFFECT, false, false, player);
    }

    private void setCooldown(Player player) {
        playerCooldownEndTimes.put(player.getUniqueId(), System.currentTimeMillis() + COOLDOWN_DURATION_MS);
    }

    private Location getTargetImpactLocation(Player player, double maxDistance) {
        World world = player.getWorld();
        Location eyeLocation = player.getEyeLocation();
        RayTraceResult blockRayTrace = world.rayTraceBlocks(eyeLocation, eyeLocation.getDirection(), maxDistance, org.bukkit.FluidCollisionMode.NEVER, true);

        if (blockRayTrace != null && blockRayTrace.getHitBlock() != null) {
            return blockRayTrace.getHitBlock().getLocation().add(0.5, 0.5, 0.5);
        } else {
            Location airPoint = eyeLocation.clone().add(eyeLocation.getDirection().multiply(maxDistance));
            Location groundPoint = airPoint.clone();
            while (groundPoint.getY() >= world.getMinHeight() && !world.getBlockAt(groundPoint).getType().isSolid() && groundPoint.getY() > player.getLocation().getY() -10) {
                groundPoint.subtract(0, 1, 0);
            }
            if (world.getBlockAt(groundPoint).getType().isSolid()) {
                 return groundPoint.add(0,1,0);
            } else {
                return airPoint;
            }
        }
    }

    public void cancelCharge(Player player) {
        if (!isCharging(player)) return;
        cleanupChargingState(player);
        player.sendActionBar(ChatColor.YELLOW + "Axe charge cancelled.");
        player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.7f, 1.0f);
    }

    public void handlePlayerQuit(Player player) {
        cleanupChargingState(player);
    }
}
