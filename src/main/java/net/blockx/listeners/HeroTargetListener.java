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
        Entity entity = event.getEntity();
        LivingEntity target = event.getTarget();

        if (target == null) {
            return;
        }

        // Check if the targeting entity is a custom hero
        PersistentDataContainer entityPDC = entity.getPersistentDataContainer();
        if (!entityPDC.has(HeroManager.HERO_SIDE_KEY, PersistentDataType.STRING)) {
            return; // Not a custom hero, default behavior
        }

        String entitySideStr = entityPDC.get(HeroManager.HERO_SIDE_KEY, PersistentDataType.STRING);
        HeroSide entitySide = HeroSide.valueOf(entitySideStr);

        // Check if the target is another custom hero
        PersistentDataContainer targetPDC = target.getPersistentDataContainer();
        if (targetPDC.has(HeroManager.HERO_SIDE_KEY, PersistentDataType.STRING)) {
            String targetSideStr = targetPDC.get(HeroManager.HERO_SIDE_KEY, PersistentDataType.STRING);
            HeroSide targetSide = HeroSide.valueOf(targetSideStr);

            // If they are on the same side, cancel the targeting
            if (entitySide == targetSide) {
                event.setCancelled(true);
            }
            // If they are on opposing sides, allow targeting (default event behavior)
            // No explicit 'allow' needed, just don't cancel.
        } else {
            // If the target is NOT a custom hero (e.g., a player, regular mob, etc.),
            // our custom heroes should not target them.
            event.setCancelled(true);
        }
    }
}
