package com.example.bigmode;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;

public class BlockAnimationHandler {

    private static final int PARTICLES_PER_FRAME = 10;
    private static final int FRAMES_PER_ANIMATION = 20;

    public static void animateBlockBreak(ServerLevel world, BlockPos pos, BlockState state) {
        // Play sound at the beginning
        world.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 1.0f, 1.0f);

        for (int frame = 0; frame < FRAMES_PER_ANIMATION; frame++) {
            // Spawn particles
            world.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    PARTICLES_PER_FRAME, 0.5, 0.5, 0.5, 0.0);

            // Pause briefly to create animation frames
            try {
                Thread.sleep(10); // Adjust sleep time for desired animation speed
            } catch (InterruptedException e) {
                System.err.println("Animation interrupted: " + e.getMessage());
                return; // Stop the animation
            }
        }
    }
}