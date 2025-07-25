package net.mrqx.cel;

import com.mojang.logging.LogUtils;
import net.mrqx.cel.config.Config;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

@Mod(CustomEntityLeveling.MODID)
@EventBusSubscriber
public class CustomEntityLeveling {
    public static final String MODID = "custom_entity_leveling";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final String ENTITY_LEVEL_KEY = "customEntityLeveling.entityLevel";
    public static final String IS_LEVELED_KEY = "customEntityLeveling.isLeveled";

    public CustomEntityLeveling() {
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        Config.CONFIG_MANAGER.get().load();
    }
}
