package com.example.bigmode;

import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;

public class BlockAnimationHandler {

    private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    public static void scheduleBlockBreak(ServerLevel world, BlockPos pos, BlockState state) {
        if (!executor.isShutdown()) {
            try {
                executor.scheduleAtFixedRate(() -> animateBlockBreak(world, pos, state), 0, 1, TimeUnit.SECONDS);
            } catch (RejectedExecutionException e) {
                System.err.println("Task rejected: " + e.getMessage());
            }
        } else {
            System.err.println("Executor is already shut down, cannot schedule new tasks.");
        }
    }

    private static void animateBlockBreak(ServerLevel world, BlockPos pos, BlockState state) {
        try {
            // Example: Trigger a particle effect at the block position
            world.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    10, 0.5, 0.5, 0.5, 0.0);
            // Example: Play a sound effect at the block position
            world.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 1.0f, 1.0f);

            // Simulate block break (for demonstration purposes)
            world.setBlockAndUpdate(pos, state);
        } catch (RejectedExecutionException e) {
            System.err.println("Task rejected: " + e.getMessage());
        }
    }

    public static void shutdownExecutor() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    System.err.println("Executor did not terminate");
                }
            }
        } catch (InterruptedException ie) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
