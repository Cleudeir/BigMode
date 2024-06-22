package com.example.bigmode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = YourMod.MODID)
public class MobBlockBreaker {

    private static final int RESTORATION_DELAY_TICKS = 10; // 20 seconds in ticks
    private static final Map<BlockPos, BlockState> brokenBlocks = new HashMap<>();
    private static boolean restorationScheduled = false;

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
            breakCooldown = 40 + random.nextInt(20); // Random delay between 40 to 60 ticks
        }

        @Override
        public void tick() {
            if (breakCooldown > 0) {
                breakCooldown--;
            } else {
                breakBlocks();
                breakCooldown = 40 + random.nextInt(20); // Reset cooldown
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

                // Store the broken block for restoration
                brokenBlocks.put(breakPos, state);

                // Schedule restoration if not already scheduled
                if (!restorationScheduled) {
                    restorationScheduled = true;
                }
            }
        }

        @Override
        public void stop() {
            // Clean up if necessary
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.level.isClientSide()) {
            ServerLevel serverWorld = (ServerLevel) event.level;
            long time = serverWorld.getGameTime();
            // Check conditions to schedule restoration
            if (restorationScheduled && time % RESTORATION_DELAY_TICKS == 0) {
                restoreBrokenBlocks(serverWorld, brokenBlocks);
                restorationScheduled = false;
            }
        }
    }

    @SubscribeEvent
    public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
        // Check if the explosion source is a creeper (optional)
        if (event.getExplosion().getExploder() instanceof Creeper) { // Adjusted check

            List<BlockPos> affectedBlocks = event.getAffectedBlocks();
            ServerLevel world = (ServerLevel) event.getLevel(); // Assuming the event provides access to the world

            for (BlockPos breakPos : affectedBlocks) {
                // Get the block state at the affected position
                BlockState state = world.getBlockState(breakPos);

                // Ensure the block is not already air and can be broken
                if (state.getBlock() != Blocks.AIR && state.getDestroySpeed(world, breakPos) >= 0) {
                    brokenBlocks.put(breakPos, state); // Store block position and state
                }
            }
        }
    }

    private static void restoreBrokenBlocks(ServerLevel world, Map<BlockPos, BlockState> blocksToRestore) {
        // Restore broken blocks logic
        for (Map.Entry<BlockPos, BlockState> entry : blocksToRestore.entrySet()) {
            BlockPos pos = entry.getKey();
            BlockState state = entry.getValue();
            if (world.getBlockState(pos).getBlock() == Blocks.AIR) {
                world.setBlockAndUpdate(pos, state); // Restore the block
            }
        }
        // Remove the restored blocks from the main map
        brokenBlocks.entrySet().removeAll(blocksToRestore.entrySet());
    }

}
