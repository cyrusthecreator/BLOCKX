package net.blockx.items; // Updated package

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ItemAttributes {
    private final List<String> lore = new ArrayList<>();
    private final List<AttributeData> attributeDataList = new ArrayList<>();

    public void addLore(String line) {
        lore.add(line);
    }

    /**
     * Adds an attribute modifier to the item.
     * The specific constructor for AttributeModifier will be addressed in Step 3
     * to handle deprecation warnings for API 1.21.
     * For now, it uses the constructor that includes EquipmentSlot.
     *
     * @param attribute The Bukkit Attribute to modify.
     * @param value     The value of the modification.
     * @param operation The operation to apply.
     * @param slot      The EquipmentSlot this attribute is active in. Defaults to HAND if null.
     */
    public void addAttribute(Attribute attribute, double value, AttributeModifier.Operation operation, EquipmentSlot slot) {
        String modifierName = "blockx." + attribute.name().toLowerCase().replace("generic.", "") + "." + UUID.randomUUID().toString().substring(0, 4);
        // This constructor is deprecated in later versions but might be what's expected for 1.18-1.20.
        // For 1.20.5+, the slot is part of the ItemMeta#addAttributeModifier method with a different AttributeModifier constructor.
        // We will address this specifically in Step 3 based on 1.21 API.
        AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), modifierName, value, operation, (slot == null ? EquipmentSlot.HAND : slot) );
        attributeDataList.add(new AttributeData(attribute, modifier));
    }

    // Overload for defaulting to HAND slot
    public void addAttribute(Attribute attribute, double value, AttributeModifier.Operation operation) {
        addAttribute(attribute, value, operation, EquipmentSlot.HAND);
    }

    public List<String> getLore() {
        return new ArrayList<>(lore); // Return a copy
    }

    public void applyAttributes(ItemMeta meta) {
        if (meta == null) return;
        for (AttributeData data : attributeDataList) {
            // The ItemMeta#addAttributeModifier method itself doesn't take EquipmentSlot in older APIs.
            // The slot was part of the AttributeModifier constructor.
            // This will be reviewed in Step 3 for 1.21 compatibility.
            meta.addAttributeModifier(data.attribute, data.modifier);
        }
    }

    private static class AttributeData {
        Attribute attribute;
        AttributeModifier modifier;

        AttributeData(Attribute attribute, AttributeModifier modifier) {
            this.attribute = attribute;
            this.modifier = modifier;
        }
    }
}
