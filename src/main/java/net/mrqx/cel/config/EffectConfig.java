package net.mrqx.cel.config;

import net.darkhax.pricklemc.common.api.annotations.Value;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;

public class EffectConfig {
    @Value(comment = "Sets the ID for this effect.")
    public String effect = "minecraft:glowing";

    @Value(comment = "Sets the amplifier (strength) for this effect.")
    public int amplifier = 0;

    @Value(comment = "Sets the duration of this effect (in ticks; -1 means infinite duration).")
    public int duration = -1;

    @Value(comment = "Sets whether the particle effect of this status is visible.")
    public boolean visible = false;

    @Value(comment = "Set effect tooltip text for '/customElementLeveling list'. Supports translatable key (requires client resource pack). See: https://minecraft.wiki/w/Resource_pack#Language")
    public String tooltip = " - %s";

    public MutableComponent getTooltips() {
        MobEffectInstance mobeffectinstance = this.getMobEffectInstance();
        Holder<MobEffect> holder = this.getEffect();
        MutableComponent mutablecomponent = Component.translatable(holder.value().getDescriptionId());
        if (mobeffectinstance.getAmplifier() > 0) {
            mutablecomponent = Component.translatable(
                    "potion.withAmplifier", mutablecomponent, Component.translatable("potion.potency." + amplifier)
            );
        }

        if (!mobeffectinstance.endsWithin(20)) {
            mutablecomponent = Component.translatable(
                    "potion.withDuration", mutablecomponent, MobEffectUtil.formatDuration(mobeffectinstance, 1, 20)
            );
        }
        return Component.translatable(this.tooltip, mutablecomponent);
    }

    public Holder<MobEffect> getEffect() {
        if (BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.parse(this.effect)).isPresent()) {
            return BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.parse(this.effect)).get();
        } else {
            throw new NullPointerException(this.effect);
        }
    }

    public MobEffectInstance getMobEffectInstance() {
        return new MobEffectInstance(
                this.getEffect(),
                this.duration,
                this.amplifier,
                true,
                this.visible
        );
    }
}
