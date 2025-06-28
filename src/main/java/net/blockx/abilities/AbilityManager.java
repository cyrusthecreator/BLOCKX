package net.blockx.abilities;

import net.blockx.items.CustomItemManager; // Corrected import
import net.blockx.items.CustomSwordType;   // Corrected import
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AbilityManager {

    private final JavaPlugin plugin;
    private final CustomItemManager customItemManager;
    private final Map<String, List<SwordAbility>> swordAbilitiesMap = new HashMap<>();

    public AbilityManager(JavaPlugin plugin, CustomItemManager customItemManager) {
        this.plugin = plugin;
        this.customItemManager = customItemManager;
        registerAllAbilities();
    }

    private void registerAllAbilities() {
        for (CustomSwordType type : CustomSwordType.values()) {
            List<SwordAbility> abilitiesForType = new ArrayList<>();
            abilitiesForType.add(new SimpleParticleAbility(type)); // All swords get simple particles

            switch (type) {
                case EBENE_SWORD:
                    abilitiesForType.add(new BonusDamageAbility(type, 2.0));
                    abilitiesForType.add(new SimpleBlockBreakAbility(type));
                    plugin.getLogger().info("Registered Ebene Sword with Bonus Damage and Simple Block Break.");
                    break;
                case GOLIATH_SWORD:
                    abilitiesForType.add(new BonusDamageAbility(type, 4.0));
                    plugin.getLogger().info("Registered Goliath Sword with Bonus Damage.");
                    break;
                case BONE_SWORD:
                    abilitiesForType.add(new BonusDamageAbility(type, 1.0, PotionEffectType.WITHER, 100, 0)); // Wither I for 5s
                    plugin.getLogger().info("Registered Bone Sword with Bonus Damage and Wither effect.");
                    break;
                default: // Should not happen if all enum types are covered, but good practice
                    abilitiesForType.add(new BonusDamageAbility(type, 0.5)); // A very minor default bonus
                    plugin.getLogger().info("Registered " + type.name() + " with default minor Bonus Damage.");
                    break;
            }
            swordAbilitiesMap.put(String.valueOf(type.getCustomModelData()), abilitiesForType);
        }
        plugin.getLogger().info("Completed registration of abilities for " + swordAbilitiesMap.size() + " custom sword types.");
    }

    public void handleAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof LivingEntity victim)) return;

        ItemStack weapon = attacker.getInventory().getItemInMainHand();
        if (weapon == null || !weapon.hasItemMeta()) return;

        String customItemId = customItemManager.getCustomItemId(weapon);
        if (customItemId == null) return;

        List<SwordAbility> abilities = swordAbilitiesMap.get(customItemId);
        if (abilities != null && !abilities.isEmpty()) {
            for (SwordAbility ability : abilities) {
                ability.onAttack(attacker, victim, weapon, event);
            }
        }
    }

    public void handleInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (itemInHand == null || !itemInHand.hasItemMeta()) return;

        String customItemId = customItemManager.getCustomItemId(itemInHand);
        if (customItemId == null) return;

        List<SwordAbility> abilities = swordAbilitiesMap.get(customItemId);
        if (abilities != null && !abilities.isEmpty()) {
            if (event.getAction().name().contains("RIGHT_CLICK")) { // Ensure it's a right click
                for (SwordAbility ability : abilities) {
                    ability.onInteract(player, itemInHand, event);
                }
            }
        }
    }

    // Placeholder for potential future player quit logic if abilities need cleanup
    // public void handlePlayerQuit(Player player) {
    //     // Example: Iterate through abilities if they store player-specific temporary data
    // }
}
