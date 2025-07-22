package net.mrqx.cel.config;

import net.darkhax.pricklemc.common.api.annotations.Value;
import net.darkhax.pricklemc.common.api.config.ConfigManager;
import net.darkhax.pricklemc.common.api.util.CachedSupplier;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.mrqx.cel.CustomEntityLeveling;

import java.util.List;

public class Config {
    @Value(reference = "https://github.com/mrqx0195/Custom-Entity-Leveling/wiki", writeDefault = false)
    public List<EntityLevelingConfig> levelingList = List.of(new EntityLevelingConfig());

    @Value(comment = "Set prompt text for '/customElementLeveling list'. Supports translatable text components (requires client resource pack). See: https://minecraft.wiki/w/Text_component_format")
    public String listCommandText = "Available levels: %s";

    public MutableComponent getListComponent() {
        MutableComponent base = Component.literal("");
        this.levelingList.forEach(levelingConfig -> {
            if (levelingConfig.visible) {
                base.append(Component.literal("\n"));
                base.append(levelingConfig.getLevelComponent());
            }
        });
        return Component.translatable(this.listCommandText, base);
    }

    public static final CachedSupplier<Config> CONFIG = CachedSupplier.cache(() -> ConfigManager.load(CustomEntityLeveling.MODID, new Config()));
}
