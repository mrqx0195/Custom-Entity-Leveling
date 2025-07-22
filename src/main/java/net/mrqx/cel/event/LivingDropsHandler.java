package net.mrqx.cel.event;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.mrqx.cel.CustomEntityLeveling;
import net.mrqx.cel.config.Config;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

import java.util.Objects;

@EventBusSubscriber
public class LivingDropsHandler {
    @SubscribeEvent
    public static void onLivingDropsEvent(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.getLastDamageSource() != null && entity.getPersistentData().contains(CustomEntityLeveling.ENTITY_LEVEL_KEY, Tag.TAG_STRING)) {
            String id = entity.getPersistentData().getString(CustomEntityLeveling.ENTITY_LEVEL_KEY);
            Config.CONFIG.ifPresent(config -> config.levelingList.forEach(levelingConfig -> {
                try {
                    if (levelingConfig.id.equals(id)) {
                        LootTable lootTable = Objects.requireNonNull(entity.getServer())
                                .reloadableRegistries()
                                .getLootTable(ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.parse(levelingConfig.lootTable)));
                        dropFromLootTable(lootTable, entity.getLastDamageSource(), entity);
                    }
                } catch (Exception e) {
                    CustomEntityLeveling.LOGGER.error("Error when drop for entity: {}, level: {}", levelingConfig.id, entity, e);
                }
            }));
        }
    }

    public static void dropFromLootTable(LootTable lootTable, DamageSource damageSource, LivingEntity entity) {
        LootParams.Builder builder = new LootParams.Builder((ServerLevel) entity.level())
                .withParameter(LootContextParams.THIS_ENTITY, entity)
                .withParameter(LootContextParams.ORIGIN, entity.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, damageSource)
                .withOptionalParameter(LootContextParams.ATTACKING_ENTITY, damageSource.getEntity())
                .withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, damageSource.getDirectEntity());

        if (entity.lastHurtByPlayer != null) {
            builder = builder
                    .withParameter(LootContextParams.LAST_DAMAGE_PLAYER, entity.lastHurtByPlayer)
                    .withLuck(entity.lastHurtByPlayer.getLuck());
        }

        LootParams lootparams = builder.create(LootContextParamSets.ENTITY);
        lootTable.getRandomItems(lootparams, entity.getLootTableSeed(), entity::spawnAtLocation);
    }
}
