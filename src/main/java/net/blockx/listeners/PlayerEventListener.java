package net.blockx.listeners;

import net.blockx.abilities.AbilityManager; // Corrected import
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
// Unused import: import org.bukkit.inventory.ItemStack;

public class PlayerEventListener implements Listener {

    private final AbilityManager abilityManager;

    public PlayerEventListener(AbilityManager abilityManager) {
        this.abilityManager = abilityManager;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) { // Renamed for clarity
        if (event.getDamager() instanceof Player) {
            // No need to cast here, AbilityManager.handleAttack will do it.
            abilityManager.handleAttack(event);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Player player = event.getPlayer(); // Player is obtained within AbilityManager if needed
        // No need to get item here, AbilityManager.handleInteract will do it.
        abilityManager.handleInteract(event);
    }
}
