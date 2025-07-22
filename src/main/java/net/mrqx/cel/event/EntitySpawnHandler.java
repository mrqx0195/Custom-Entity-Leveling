package net.mrqx.cel.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.mrqx.cel.CustomEntityLeveling;
import net.mrqx.cel.config.Config;
import net.mrqx.cel.config.EntityLevelingConfig;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

@EventBusSubscriber
public class EntitySpawnHandler {
    @SubscribeEvent
    public static void onEntityJoinLevelEvent(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof LivingEntity livingEntity
                && !livingEntity.level().isClientSide
                && !event.loadedFromDisk()
                && !(livingEntity instanceof Player)) {
            AtomicBoolean leveled = new AtomicBoolean(false);
            Config.CONFIG.ifPresent(config -> config.levelingList.forEach(levelingConfig -> {
                if (leveled.get()) {
                    return;
                }
                if (!levelingConfig.isBlackList && levelingConfig.entityList.contains(livingEntity.getEncodeId())) {
                    if (trySetLevel(livingEntity, levelingConfig, false)) {
                        leveled.set(true);
                    }
                }
                if (levelingConfig.isBlackList && !levelingConfig.entityList.contains(livingEntity.getEncodeId())) {
                    if (trySetLevel(livingEntity, levelingConfig, false)) {
                        leveled.set(true);
                    }
                }
                if (leveled.get()) {
                    livingEntity.heal(livingEntity.getMaxHealth());
                    livingEntity.getPersistentData().putBoolean(CustomEntityLeveling.IS_LEVELED_KEY, true);
                }
            }));
        }
    }

    public static boolean trySetLevel(LivingEntity entity, EntityLevelingConfig levelingConfig, boolean ignoreChance) {
        Level level = entity.level();
        if (ignoreChance || level.random.nextDouble() < levelingConfig.spawnChance) {
            try {
                levelingConfig.attributeList.forEach(attributeConfig -> {
                    try {
                        AttributeInstance instance = Objects.requireNonNull(entity.getAttribute(attributeConfig.getAttribute()));
                        instance.addPermanentModifier(attributeConfig.getAttributeModifier());
                    } catch (Exception e) {
                        CustomEntityLeveling.LOGGER.error("Error when trying set attribute {} for entity: {}, level: {}", attributeConfig.attribute, entity, levelingConfig.id, e);
                    }
                });
                levelingConfig.effectList.forEach(effectConfig -> {
                    try {
                        entity.addEffect(effectConfig.getMobEffectInstance());
                    } catch (Exception e) {
                        CustomEntityLeveling.LOGGER.error("Error when trying set effect {} for entity: {}, level: {}", effectConfig.effect, entity, levelingConfig.id, e);
                    }
                });
                entity.getPersistentData().putString(CustomEntityLeveling.ENTITY_LEVEL_KEY, levelingConfig.id);
                entity.setCustomName(levelingConfig.getEntityName(entity));
                return true;
            } catch (Exception e) {
                CustomEntityLeveling.LOGGER.error("Error when trying set level {} for entity: {}", levelingConfig.id, entity, e);
                return false;
            }
        }
        return false;
    }
}
