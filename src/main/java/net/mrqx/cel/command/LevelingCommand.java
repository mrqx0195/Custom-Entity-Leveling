package net.mrqx.cel.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.mrqx.cel.CustomEntityLeveling;
import net.mrqx.cel.config.Config;
import net.mrqx.cel.event.EntitySpawnHandler;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.concurrent.atomic.AtomicBoolean;

@EventBusSubscriber
public class LevelingCommand {
    @SubscribeEvent
    public static void onRegisterCommandsEvent(RegisterCommandsEvent event) {
        register(event);
    }

    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> commandDispatcher = event.getDispatcher();
        commandDispatcher.register(Commands.literal("customEntityLeveling")
                .then(Commands.literal("setLevel")
                        .requires(source -> source.hasPermission(4))
                        .then(Commands.argument("level", StringArgumentType.word())
                                .then(Commands.argument("entities", EntityArgument.entities())
                                        .executes(context -> {
                                            AtomicBoolean flag = new AtomicBoolean(false);
                                            AtomicBoolean exceptionFlag = new AtomicBoolean(false);
                                            Config.CONFIG.ifPresent(config -> config.levelingList.stream()
                                                    .filter(levelingConfig -> levelingConfig.id.equals(StringArgumentType.getString(context, "level")))
                                                    .forEach(levelingConfig -> {
                                                        try {
                                                            EntityArgument.getEntities(context, "entities").forEach(entity -> {
                                                                if (entity instanceof LivingEntity livingEntity
                                                                        && !livingEntity.level().isClientSide
                                                                        && !(livingEntity instanceof Player)
                                                                        && !livingEntity.getPersistentData().getBoolean(CustomEntityLeveling.IS_LEVELED_KEY)) {
                                                                    if (EntitySpawnHandler.trySetLevel(livingEntity, levelingConfig, true)) {
                                                                        livingEntity.heal(livingEntity.getMaxHealth());
                                                                        livingEntity.getPersistentData().putBoolean(CustomEntityLeveling.IS_LEVELED_KEY, true);
                                                                    }
                                                                }
                                                            });
                                                            flag.set(true);
                                                        } catch (Exception e) {
                                                            CustomEntityLeveling.LOGGER.error("Error when trying set level {}", levelingConfig.id, e);
                                                            exceptionFlag.set(true);
                                                        }
                                                    }));
                                            return (!exceptionFlag.get()) && flag.get() ? 0 : 1;
                                        })
                                )
                        )
                )
                .then(Commands.literal("list")
                        .executes(context -> {
                            Config.CONFIG.ifPresent(config -> context.getSource().sendSystemMessage(config.getListComponent()));
                            return 0;
                        })
                )
        );
    }
}
