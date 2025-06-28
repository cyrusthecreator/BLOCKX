package net.blockx.items;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;

import java.util.function.Supplier;

public enum CustomSwordType {
    EBENE_SWORD("&bEbene Sword", 11001, Material.IRON_SWORD, () -> {
        ItemAttributes attributes = new ItemAttributes();
        attributes.addLore("&eA blade that moves as fast as the wind.");
        attributes.addLore("&7Moderate damage, very fast attack.");
        attributes.addAttribute(Attribute.GENERIC_ATTACK_DAMAGE, 6.0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);
        attributes.addAttribute(Attribute.GENERIC_ATTACK_SPEED, -1.8, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);
        return attributes;
    }),
    GOLIATH_SWORD("&6Goliath Sword", 11002, Material.IRON_SWORD, () -> {
        ItemAttributes attributes = new ItemAttributes();
        attributes.addLore("&eA colossal sword capable of great destruction.");
        attributes.addLore("&7Very high damage, very slow attack.");
        attributes.addAttribute(Attribute.GENERIC_ATTACK_DAMAGE, 15.0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);
        attributes.addAttribute(Attribute.GENERIC_ATTACK_SPEED, -3.2, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);
        return attributes;
    }),
    BONE_SWORD("&7Bone Sword", 11003, Material.IRON_SWORD, () -> {
        ItemAttributes attributes = new ItemAttributes();
        attributes.addLore("&eA sinister sword carved from ancient bones.");
        attributes.addLore("&7Lifesteal effect on hit (placeholder).");
        attributes.addAttribute(Attribute.GENERIC_ATTACK_DAMAGE, 5.0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);
        attributes.addAttribute(Attribute.GENERIC_ATTACK_SPEED, -2.4, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);
        return attributes;
    });

    private final String displayName;
    private final int customModelData;
    private final Material baseMaterial;
    private final Supplier<ItemAttributes> attributesSupplier;

    CustomSwordType(String displayName, int customModelData, Material baseMaterial, Supplier<ItemAttributes> attributesSupplier) {
        this.displayName = displayName;
        this.customModelData = customModelData;
        this.baseMaterial = baseMaterial;
        this.attributesSupplier = attributesSupplier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public Material getBaseMaterial() {
        return baseMaterial;
    }

    public ItemAttributes getItemAttributes() {
        return attributesSupplier.get();
    }

    public static CustomSwordType fromString(String name) {
        String commandFriendlyName = name.toLowerCase().replace("_", "").replace(" ", "");
        for (CustomSwordType type : values()) {
            if (type.name().equalsIgnoreCase(name) ||
                type.getDisplayName().replaceAll("(&[a-fk-or0-9])", "").toLowerCase().replace(" ", "").equals(commandFriendlyName) ||
                type.name().toLowerCase().replace("_", "").equals(commandFriendlyName)) {
                return type;
            }
        }
        return null;
    }
}
