
package com.example.bigmode;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

@Mod.EventBusSubscriber(modid = YourMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModCommands {

    private static boolean waveSpawned = false;
    private static ScheduledExecutorService scheduler;

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("startwave")
                .requires(source -> source.hasPermission(2)) // Requires a permission level of 2 (cheats enabled)
                .executes(ModCommands::executeStartWave));
    }

    @SuppressWarnings("resource")
    private static int executeStartWave(CommandContext<CommandSourceStack> context) {
        if (!waveSpawned) {
            CommandSourceStack source = context.getSource();
            ServerLevel serverWorld = source.getLevel();
            Collection<ServerPlayer> players = serverWorld.getPlayers((Predicate<? super ServerPlayer>) p -> true);
            scheduler = Executors.newScheduledThreadPool(1);
            for (ServerPlayer player : players) {
                scheduler.scheduleAtFixedRate(() -> {
                    MobSpawnHandler.spawnCommandedMobWave(player.serverLevel());
                }, 0, 1000, TimeUnit.MILLISECONDS);
            }
            scheduler.schedule(() -> {
                scheduler.shutdown();
            }, 545850, TimeUnit.MILLISECONDS);
            waveSpawned = true;
        }
        return 0;
    }
}
