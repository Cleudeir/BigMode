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
import java.util.function.Predicate; // Import Predicate

@Mod.EventBusSubscriber(modid = YourMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModCommands {

    private static boolean waveSpawned = false;

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("startwave")
                .requires(source -> source.hasPermission(2)) // Requires a permission level of 2 (cheats enabled)
                .executes(ModCommands::executeStartWave));
    }

    private static int executeStartWave(CommandContext<CommandSourceStack> context) {
        if (!waveSpawned) {
            CommandSourceStack source = context.getSource();
            ServerLevel serverWorld = source.getLevel();
            Collection<ServerPlayer> players = serverWorld.getPlayers((Predicate<ServerPlayer>) p -> true); // Provide a Predicate that always returns true

            for (ServerPlayer player : players) {
                MobSpawnHandler.spawnCommandedMobWave(player.serverLevel()); 
            }

            waveSpawned = true; // Set the flag to true after spawning waves for all players
        }

        return 0;
    }
}
