package net.blockx.abilities;

import net.blockx.items.CustomSwordType; // Corrected import
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity; // Keep for interface consistency, though unused here
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent; // Keep for interface consistency
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

public class SimpleBlockBreakAbility implements SwordAbility {

    private final CustomSwordType associatedType;
    private final Set<Material> breakableBlocks = new HashSet<>();

    public SimpleBlockBreakAbility(CustomSwordType associatedType) {
        this.associatedType = associatedType;
        // Define default breakable blocks
        this.breakableBlocks.add(Material.COBWEB);
        this.breakableBlocks.add(Material.OAK_LEAVES);
        this.breakableBlocks.add(Material.SPRUCE_LEAVES);
        this.breakableBlocks.add(Material.BIRCH_LEAVES);
        this.breakableBlocks.add(Material.JUNGLE_LEAVES);
        this.breakableBlocks.add(Material.ACACIA_LEAVES);
        this.breakableBlocks.add(Material.DARK_OAK_LEAVES);
        this.breakableBlocks.add(Material.AZALEA_LEAVES);
        // this.breakableBlocks.add(Material.FLOWERING_AZALEA_LEAVES); // FLOWERING_AZALEA is the block
        this.breakableBlocks.add(Material.FLOWERING_AZALEA); // The actual block material
        this.breakableBlocks.add(Material.GLASS_PANE);
        this.breakableBlocks.add(Material.GLASS);
        this.breakableBlocks.add(Material.VINE);
        this.breakableBlocks.add(Material.GRASS_BLOCK); // Corrected from GRASS
        this.breakableBlocks.add(Material.TALL_GRASS); // This is the two-high grass plant
        this.breakableBlocks.add(Material.FERN);
        this.breakableBlocks.add(Material.LARGE_FERN);
    }

    // Optional constructor to specify breakable blocks
    public SimpleBlockBreakAbility(CustomSwordType associatedType, Set<Material> specificBreakableBlocks) {
        this.associatedType = associatedType;
        if (specificBreakableBlocks != null && !specificBreakableBlocks.isEmpty()) {
            this.breakableBlocks.addAll(specificBreakableBlocks);
        } else {
            // Fallback to defaults if null or empty set passed (redundant if default constructor called first by logic)
            this.breakableBlocks.add(Material.COBWEB);
            this.breakableBlocks.add(Material.OAK_LEAVES);
            this.breakableBlocks.add(Material.GRASS_BLOCK);
        }
    }

    @Override
    public void onAttack(Player attacker, LivingEntity victim, ItemStack sword, EntityDamageByEntityEvent event) {
        // No on-attack action
    }

    @Override
    public void onInteract(Player player, ItemStack sword, PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block clickedBlock = event.getClickedBlock();
            if (clickedBlock != null && breakableBlocks.contains(clickedBlock.getType())) {
                clickedBlock.breakNaturally(sword);
                event.setCancelled(true);
            }
        }
    }

    @Override
    public CustomSwordType getAssociatedSwordType() {
        return associatedType;
    }
}
