package net.mrqx.cel.config;

import net.darkhax.pricklemc.common.api.annotations.RangedDouble;
import net.darkhax.pricklemc.common.api.annotations.Value;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public class EntityLevelingConfig {
    @Value(comment = "Sets the ID for this level.")
    public String id = "example_level";

    @Value(comment = "Sets the custom name for this level. Supports translatable key (requires client resource pack). See: https://minecraft.wiki/w/Resource_pack#Language")
    public String levelName = "Example Level";

    @Value(comment = "Sets the custom name for entities of this level. Supports translatable key (requires client resource pack). See: https://minecraft.wiki/w/Resource_pack#Language.\nParameter: Name of this entity.")
    public String entityName = "Example Level %s";

    @Value(comment = "Sets whether entityList is in blacklist mode (default is whitelist).")
    public boolean isBlackList = false;

    @Value(comment = "Whitelist mode: Entities to which this level configuration should be applied.\nBlacklist mode: Entities to which this level configuration should NOT be applied.")
    public List<String> entityList = new ArrayList<>();

    @Value(comment = "Probability for entities to spawn at this level when the configuration should be applied.")
    @RangedDouble(min = 0.0, max = 1.0)
    public double spawnChance = 0.0;

    @Value(comment = "Loot drops when entities of this level are killed (uses loot tables. See: https://minecraft.wiki/w/Loot_table).")
    public String lootTable = "minecraft:chests/simple_dungeon";

    @Value(comment = "Attribute modifiers to be applied to entities of this level.")
    public List<AttributeConfig> attributeList = List.of(new AttributeConfig());

    @Value(comment = "Effects to be applied to entities of this level.")
    public List<EffectConfig> effectList = List.of(new EffectConfig());

    @Value(comment = "Sets whether this level is visible for '/customElementLeveling list'.")
    public boolean visible = true;

    @Value(comment = "Set prompt text for '/customElementLeveling list'. Supports translatable key (requires client resource pack). See: https://minecraft.wiki/w/Resource_pack#Language\nParameter: levelName.")
    public String listCommandText = " - %s";

    @Value(comment = "Set entity text for '/customElementLeveling list'. Supports translatable key (requires client resource pack). See: https://minecraft.wiki/w/Resource_pack#Language\nParameter: levelName.")
    public String listCommandEntityText = "\n - %s";

    @Value(comment = "Set tooltip text for '/customElementLeveling list'. Supports translatable key (requires client resource pack). See: https://minecraft.wiki/w/Resource_pack#Language")
    public String listCommandTooltips = "§eAn example entity level.\n\n§3Is Blacklist mode: %1$s\n§7Entity List: %2$s\n§aAttribute List: %3$s\n§dEffect List: %4$s\n\n§bSpawn Chance: %5$s";

    @Value(comment = "Sets whether to notify the killer when killing an entity of this level.")
    public boolean noticeKiller = true;

    @Value(comment = "Sets the notification content sent to the killer when killing an entity of this level. Supports translatable key (requires client resource pack). See: https://minecraft.wiki/w/Resource_pack#Language\nParameter: entity`s name, levelName.")
    public String noticeKillerText = "Congratulations! You killed a %1$s at level %2$s!";

    @Value(comment = "Sets whether to notify all players when killing an entity of this level.")
    public boolean noticeAllPlayer = true;

    @Value(comment = "Sets the notification content sent to all players when killing an entity of this level. Supports translatable key (requires client resource pack). See: https://minecraft.wiki/w/Resource_pack#Language\nParameter: player`s name, entity`s name, levelName.")
    public String noticeAllPlayerText = "Congratulations! Player %1$s killed a %2$s at level %3$s!";

    public MutableComponent getLevelName() {
        return Component.translatable(this.levelName);
    }

    public MutableComponent getEntityName(Entity entity) {
        return Component.translatable(this.entityName, entity.getType().getDescription());
    }

    public MutableComponent getLevelComponent() {
        return Component.translatable(this.listCommandText, this.getLevelName())
                .withStyle(style -> style.withHoverEvent(
                        new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                Component.translatable(listCommandTooltips,
                                        this.isBlackList ? Component.translatable("gui.yes") : Component.translatable("gui.no"),
                                        this.getEntityListComponent(),
                                        this.getAttributeListComponent(),
                                        this.getEffectListComponent(),
                                        this.getSpawnChanceComponent()
                                )
                        )
                ));
    }

    public MutableComponent getEntityListComponent() {
        MutableComponent base = Component.literal("");
        this.getEntityTypes().stream().map(EntityType::getDescription).forEach(component -> base.append(Component.translatable(this.listCommandEntityText, component)));
        return base;
    }

    public MutableComponent getAttributeListComponent() {
        MutableComponent base = Component.literal("");
        this.attributeList.forEach(attributeConfig -> {
            base.append(Component.literal("\n"));
            base.append(attributeConfig.getTooltip());
        });
        return base;
    }

    public MutableComponent getEffectListComponent() {
        MutableComponent base = Component.literal("");
        this.effectList.forEach(effectConfig -> {
            base.append(Component.literal("\n"));
            base.append(effectConfig.getTooltips());
        });
        return base;
    }

    public MutableComponent getSpawnChanceComponent() {
        return Component.translatable("loading.progress", this.spawnChance * 100);
    }

    public List<EntityType<?>> getEntityTypes() {
        List<EntityType<?>> list = new ArrayList<>();
        this.entityList.forEach(s -> {
            if (BuiltInRegistries.ENTITY_TYPE.getHolder(ResourceLocation.parse(s)).isPresent()) {
                list.add(BuiltInRegistries.ENTITY_TYPE.getHolder(ResourceLocation.parse(s)).get().value());
            }
        });
        return list;
    }
}
