package net.blockx.abilities;

import net.blockx.items.CustomSwordType; // Corrected import
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class BonusDamageAbility implements SwordAbility {

    private final CustomSwordType associatedType;
    private final double extraDamage;
    private final PotionEffectType effectOnHit;
    private final int effectDurationTicks;
    private final int effectAmplifier;

    public BonusDamageAbility(CustomSwordType associatedType, double extraDamage) {
        this(associatedType, extraDamage, null, 0, 0);
    }

    public BonusDamageAbility(CustomSwordType associatedType, double extraDamage, PotionEffectType effectOnHit, int effectDurationTicks, int effectAmplifier) {
        this.associatedType = associatedType;
        this.extraDamage = extraDamage;
        this.effectOnHit = effectOnHit;
        this.effectDurationTicks = effectDurationTicks;
        this.effectAmplifier = effectAmplifier;
    }

    @Override
    public void onAttack(Player attacker, LivingEntity victim, ItemStack sword, EntityDamageByEntityEvent event) {
        event.setDamage(event.getDamage() + extraDamage);

        if (effectOnHit != null && effectDurationTicks > 0) {
            victim.addPotionEffect(new PotionEffect(effectOnHit, effectDurationTicks, effectAmplifier));
        }
    }

    @Override
    public void onInteract(Player player, ItemStack sword, PlayerInteractEvent event) {
        // No right-click interaction for this specific ability
    }

    @Override
    public CustomSwordType getAssociatedSwordType() {
        return associatedType;
    }
}
