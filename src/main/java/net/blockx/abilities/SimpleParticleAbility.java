package net.blockx.abilities;

import net.blockx.items.CustomSwordType; // Corrected import
import org.bukkit.Particle; // Corrected import
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class SimpleParticleAbility implements SwordAbility {

    private final CustomSwordType associatedType;

    public SimpleParticleAbility(CustomSwordType associatedType) {
        this.associatedType = associatedType;
    }

    @Override
    public void onAttack(Player attacker, LivingEntity victim, ItemStack sword, EntityDamageByEntityEvent event) {
        victim.getWorld().spawnParticle(Particle.ENCHANTED_HIT, victim.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0.1);
    }

    @Override
    public void onInteract(Player player, ItemStack sword, PlayerInteractEvent event) {
        if (event.getAction().name().contains("RIGHT_CLICK")) {
            player.getWorld().spawnParticle(Particle.ENCHANT, player.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.2);
        }
    }

    @Override
    public CustomSwordType getAssociatedSwordType() {
        return associatedType;
    }
}
