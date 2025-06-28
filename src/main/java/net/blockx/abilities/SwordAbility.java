package net.blockx.abilities;

import net.blockx.items.CustomSwordType; // Corrected import path
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public interface SwordAbility {

    void onAttack(Player attacker, LivingEntity victim, ItemStack sword, EntityDamageByEntityEvent event);

    void onInteract(Player player, ItemStack sword, PlayerInteractEvent event);

    /**
     * Optional method to link an ability directly to a sword type.
     * Can be null if the ability is generic or if mapping is handled entirely by AbilityManager.
     * @return The associated CustomSwordType or null.
     */
    CustomSwordType getAssociatedSwordType();
}
