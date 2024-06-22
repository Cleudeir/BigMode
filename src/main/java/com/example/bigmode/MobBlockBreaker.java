package com.example.bigmode;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.Random;

public class MobBlockBreaker {

    public static void enableMobBlockBreaking(Mob mob, ServerLevel world, Player target) {
        mob.goalSelector.addGoal(1, new BreakBlocksGoal(mob, world, target));
    }

    static class BreakBlocksGoal extends Goal {
        private final Mob mob;
        private final ServerLevel world;
        private final Player target;
        private int breakCooldown = 0;
        private final Random random = new Random();

        public BreakBlocksGoal(Mob mob, ServerLevel world, Player target) {
            this.mob = mob;
            this.world = world;
            this.target = target;
        }

        @Override
        public boolean canUse() {
            // Only start breaking blocks if the mob is targeting the player
            return mob.getTarget() == target;
        }

        @Override
        public void start() {
            // Initial delay before starting to break blocks (simulate laziness)
            breakCooldown = 40 + random.nextInt(20); // Random delay between 40 to 60 ticks
        }

        @Override
        public void tick() {
            if (breakCooldown > 0) {
                breakCooldown--;
            } else {
                breakBlocks();
                // Add a random delay after each block break (simulate laziness)
                breakCooldown = 40 + random.nextInt(20); // Random delay between 40 to 60 ticks
            }
        }

        private void breakBlocks() {
            BlockPos mobPos = mob.blockPosition();
            BlockPos targetPos = target.blockPosition();

            int distanceX = targetPos.getX() - mobPos.getX();
            int distanceZ = targetPos.getZ() - mobPos.getZ();
            int maxDistance = 2; // Maximum distance mobs can break blocks (2 blocks in front)

            // Calculate the block position in front of the mob
            BlockPos breakPos = new BlockPos(
                    mobPos.getX() + Mth.clamp(distanceX, -maxDistance, maxDistance),
                    mobPos.getY(),
                    mobPos.getZ() + Mth.clamp(distanceZ, -maxDistance, maxDistance));

            BlockState state = world.getBlockState(breakPos);
            if (state.getBlock() != Blocks.AIR && state.getDestroySpeed(world, breakPos) >= 0) {
                world.destroyBlock(breakPos, true, mob); // Destroy the block
                world.levelEvent(2001, breakPos, Block.getId(state)); // Block break animation
            }
        }

        @Override
        public void stop() {
            // Clean up if necessary
        }
    }
}
