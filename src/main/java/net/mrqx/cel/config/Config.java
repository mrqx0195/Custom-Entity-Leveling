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

    @Value(comment = "Set prompt text for '/customElementLeveling list'. Supports translatable key (requires client resource pack). See: https://minecraft.wiki/w/Resource_pack#Language")
    public String listCommandText = "Available levels: %s";

    @Value(comment = "Set prompt text for successfully using '/customElementLeveling reload'. Supports translatable key (requires client resource pack). See: https://minecraft.wiki/w/Resource_pack#Language")
    public String reloadSuccessfulText = "Custom Entity Leveling reload successfully!";

    @Value(comment = "Set prompt text for using failed '/customElementLeveling reload'. Supports translatable key (requires client resource pack). See: https://minecraft.wiki/w/Resource_pack#Language")
    public String reloadFailedText = "Error while reloading! View latest.log for more info.";

    @Value(comment = "Set prompt text for successfully using '/customElementLeveling setLevel' on single target. Supports translatable key (requires client resource pack). See: https://minecraft.wiki/w/Resource_pack#Language")
    public String setLevelSingleSuccessfulText = "Successfully set level for %s.";

    @Value(comment = "Set prompt text for successfully using '/customElementLeveling setLevel' on multiple targets. Supports translatable key (requires client resource pack). See: https://minecraft.wiki/w/Resource_pack#Language")
    public String setLevelMultipleSuccessfulText = "Successfully set level for %s entities.";

    @Value(comment = "Set prompt text for level not found when using '/customElementLeveling setLevel'. Supports translatable key (requires client resource pack). See: https://minecraft.wiki/w/Resource_pack#Language")
    public String setLevelNotFoundText = "Cannot find level: %s!";

    @Value(comment = "Set prompt text for using failed '/customElementLeveling setLevel'. Supports translatable key (requires client resource pack). See: https://minecraft.wiki/w/Resource_pack#Language")
    public String setLevelFailedText = "Error while setting level! View latest.log for more info.";

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

    public static Config getInstance() {
        return CONFIG_MANAGER.get().get();
    }

    public static final CachedSupplier<ConfigManager<Config>> CONFIG_MANAGER = CachedSupplier.cache(() -> ConfigManager.init(CustomEntityLeveling.MODID, new Config()));
}
