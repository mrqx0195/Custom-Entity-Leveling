package net.mrqx.cel.config;

import net.darkhax.pricklemc.common.api.annotations.Value;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public class AttributeConfig {
    @Value(comment = "Sets the attribute to which this modifier applies.")
    public String attribute = "minecraft:generic.max_health";

    @Value(comment = "Sets the value of this attribute modifier.")
    public float value = 20.0F;

    @Value(comment = "Sets the operation type of this attribute modifier.")
    public String operation = AttributeModifier.Operation.ADD_VALUE.getSerializedName();

    @Value(comment = "Set attribute tooltip text for '/customElementLeveling list'. Supports translatable text components (requires client resource pack). See: https://minecraft.wiki/w/Text_component_format")
    public String tooltip = " - %s";

    public MutableComponent getTooltip() {
        return Component.translatable(this.tooltip, this.getAttributeModifierTooltips());
    }

    public Holder<Attribute> getAttribute() {
        if (BuiltInRegistries.ATTRIBUTE.getHolder(ResourceLocation.parse(this.attribute)).isPresent()) {
            return BuiltInRegistries.ATTRIBUTE.getHolder(ResourceLocation.parse(this.attribute)).get();
        } else {
            throw new NullPointerException(this.attribute);
        }
    }

    public AttributeModifier getAttributeModifier() {
        return new AttributeModifier(
                ResourceLocation.parse(this.attribute),
                this.value,
                AttributeConfig.fromStringToOperation(this.operation)
        );
    }

    public MutableComponent getAttributeModifierTooltips() {
        Attribute attribute1 = this.getAttribute().value();
        AttributeModifier attributeModifier = this.getAttributeModifier();
        double d0;
        double d1 = attributeModifier.amount();
        if (attributeModifier.operation() != AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                && attributeModifier.operation() != AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
            d0 = attributeModifier.amount();
        } else {
            d0 = attributeModifier.amount() * 100;
        }

        if (d1 >= 0.0) {
            return Component.translatable(
                            "attribute.modifier.plus." + attributeModifier.operation().id(),
                            ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(d0),
                            Component.translatable(attribute1.getDescriptionId())
                    )
                    .withStyle(ChatFormatting.BLUE);
        } else {
            d0 *= -1;
            return Component.translatable(
                            "attribute.modifier.take." + attributeModifier.operation().id(),
                            ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(d0),
                            Component.translatable(attribute1.getDescriptionId())
                    )
                    .withStyle(ChatFormatting.RED);
        }
    }

    public static AttributeModifier.Operation fromStringToOperation(String operation) {
        if (operation.equals(AttributeModifier.Operation.ADD_VALUE.getSerializedName())) {
            return AttributeModifier.Operation.ADD_VALUE;
        } else if (operation.equals(AttributeModifier.Operation.ADD_MULTIPLIED_BASE.getSerializedName())) {
            return AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
        } else if (operation.equals(AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL.getSerializedName())) {
            return AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
        }
        throw new IllegalArgumentException(operation);
    }
}
