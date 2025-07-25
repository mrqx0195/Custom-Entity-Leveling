package net.mrqx.cel.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.mrqx.cel.CustomEntityLeveling;
import net.mrqx.cel.config.Config;
import net.mrqx.cel.event.EntitySpawnHandler;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@EventBusSubscriber
public class LevelingCommand {
    @SubscribeEvent
    public static void onRegisterCommandsEvent(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register(Commands.literal("customEntityLeveling")
                .then(Commands.literal("setLevel")
                        .requires(source -> source.hasPermission(4))
                        .then(Commands.argument("level", StringArgumentType.word())
                                .suggests((context, suggestionsBuilder) -> {
                                    Config config = Config.getInstance();
                                    config.levelingList.forEach(levelingConfig -> suggestionsBuilder.suggest(levelingConfig.id, levelingConfig.getLevelName()));
                                    return suggestionsBuilder.buildFuture();
                                })
                                .then(Commands.argument("entities", EntityArgument.entities())
                                        .executes(context -> {
                                            AtomicBoolean flag = new AtomicBoolean(false);
                                            AtomicBoolean exceptionFlag = new AtomicBoolean(false);
                                            AtomicInteger count = new AtomicInteger(0);
                                            Config config = Config.getInstance();
                                            Collection<Entity> entities = EntityArgument.getEntities(context, "entities").stream().map(e -> (Entity) e).collect(Collectors.toSet());
                                            Collection<Entity> removedEntities = new ArrayList<>();
                                            String id = StringArgumentType.getString(context, "level");
                                            config.levelingList.stream()
                                                    .filter(levelingConfig -> levelingConfig.id.equals(id))
                                                    .forEach(levelingConfig -> {
                                                        try {
                                                            for (Entity entity : entities) {
                                                                if (entity instanceof LivingEntity livingEntity
                                                                        && !(livingEntity instanceof Player)
                                                                        && !livingEntity.level().isClientSide
                                                                        && !livingEntity.getPersistentData().getBoolean(CustomEntityLeveling.IS_LEVELED_KEY)
                                                                        && EntitySpawnHandler.trySetLevel(livingEntity, levelingConfig, true)) {
                                                                    livingEntity.heal(livingEntity.getMaxHealth());
                                                                    livingEntity.getPersistentData().putBoolean(CustomEntityLeveling.IS_LEVELED_KEY, true);
                                                                    count.addAndGet(1);
                                                                    CustomEntityLeveling.LOGGER.debug("{}", livingEntity);
                                                                } else {
                                                                    removedEntities.add(entity);
                                                                }
                                                                CustomEntityLeveling.LOGGER.debug("{}", entity);
                                                            }
                                                            entities.removeAll(removedEntities);
                                                            flag.set(true);
                                                        } catch (Exception e) {
                                                            CustomEntityLeveling.LOGGER.error("Error when trying set level {}", levelingConfig.id, e);
                                                            exceptionFlag.set(true);
                                                        }
                                                    });
                                            if (exceptionFlag.get()) {
                                                context.getSource().sendFailure(Component.translatable(Config.getInstance().setLevelFailedText));
                                                return 1;
                                            }
                                            if (!flag.get()) {
                                                context.getSource().sendFailure(Component.translatable(Config.getInstance().setLevelNotFoundText, id));
                                                return 1;
                                            }
                                            if (count.get() == 0) {
                                                context.getSource().sendFailure(Component.translatable("argument.entity.notfound.entity"));
                                                return 1;
                                            } else if (count.get() == 1) {
                                                context.getSource().sendSuccess(() -> Component.translatable(Config.getInstance().setLevelSingleSuccessfulText, entities.iterator().next().getDisplayName()), true);
                                                return 0;
                                            } else {
                                                context.getSource().sendSuccess(() -> Component.translatable(Config.getInstance().setLevelMultipleSuccessfulText, entities.size()), true);
                                                return 0;
                                            }
                                        })
                                )
                        )
                )
                .then(Commands.literal("reload")
                        .requires(source -> source.hasPermission(4))
                        .executes(context -> {
                            try {
                                Config.CONFIG_MANAGER.get().load();
                                context.getSource().sendSuccess(() -> Component.translatable(Config.getInstance().reloadSuccessfulText), true);
                                return 0;
                            } catch (Exception e) {
                                CustomEntityLeveling.LOGGER.error("Error while reloading", e);
                                context.getSource().sendFailure(Component.translatable(Config.getInstance().reloadFailedText));
                                return 1;
                            }
                        })
                )
                .then(Commands.literal("list")
                        .executes(context -> {
                            Config config = Config.getInstance();
                            context.getSource().sendSuccess(config::getListComponent, true);
                            return 0;
                        })
                )
        );
    }
}
