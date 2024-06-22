
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

            // Initialize the scheduler
            scheduler = Executors.newScheduledThreadPool(1);

            // Schedule the task to spawn waves for each player every 5 seconds
            for (ServerPlayer player : players) {
                scheduler.scheduleAtFixedRate(() -> {
                    MobSpawnHandler.spawnCommandedMobWave(player.serverLevel());
                }, 0, 1000, TimeUnit.MILLISECONDS); // Every 1000 milliseconds (5 seconds)
            }

            scheduler.schedule(() -> {
                scheduler.shutdown();
            }, 545850, TimeUnit.MILLISECONDS);

            waveSpawned = true; // Set the flag to true after scheduling waves for all players
        }
        return 0;
    }
}



