package com.example.bigmode;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Iterator;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = YourMod.MODID)
public class MobBlockBreaker {

    private static final Queue<BlockBreakTask> blockBreakTasks = new LinkedList<>();
    private static Mob currentMob;
    private static ServerLevel currentWorld;
    private static Player currentTarget;

    /**
     * Enable block-breaking behavior for a mob.
     *
     * @param mob    The mob to enable block-breaking for.
     * @param world  The world the mob is in.
     * @param target The player target.
     */
    public static void enableMobBlockBreaking(Mob mob, ServerLevel world, Player target) {
        currentMob = mob;
        currentWorld = world;
        currentTarget = target;
    }

    /**
     * Schedule block breaks if conditions are met.
     *
     * @param mobPos    The position of the mob.
     * @param targetPos The position of the target player.
     */

     private static final int MAX_DISTANCE_SQ = 5;
     private static void scheduleBlockBreak(BlockPos mobPos, BlockPos targetPos) {
        int distanceSq = (int) mobPos.distSqr(targetPos);
        if (distanceSq > MAX_DISTANCE_SQ) {
        // Check if the mob is too close to the player
        int distanceX = targetPos.getX() - mobPos.getX();
        int distanceZ = targetPos.getZ() - mobPos.getZ();
        int distanceY = targetPos.getY() - mobPos.getY();
        int maxDistance = 5;
        
        BlockPos breakPos = new BlockPos(
            mobPos.getX() + Mth.clamp(distanceX, -maxDistance, maxDistance),
            mobPos.getY() + Mth.clamp(distanceY, -maxDistance, maxDistance),
            mobPos.getZ() + Mth.clamp(distanceZ, -maxDistance, maxDistance));
            
            BlockState state = currentWorld.getBlockState(breakPos);
            if (state.getBlock() != Blocks.AIR && state.getDestroySpeed(currentWorld, breakPos) >= 0) {
                // Schedule the block break task with a delay
                blockBreakTasks.add(() -> {
                    // Perform the block break actions
                    currentWorld.destroyBlock(breakPos, true, currentMob);
                    currentWorld.levelEvent(2001, breakPos, Block.getId(state));
                    currentWorld.playSound(null, breakPos, SoundEvents.STONE_BREAK, currentMob.getSoundSource(), 1.0F,
                            1.0F);
                    currentWorld.gameEvent(currentMob, GameEvent.BLOCK_DESTROY, breakPos);
                });

                BlockRestorer.addBrokenBlock(breakPos, state); // Store the broken block for restoration
            }
        }
    }

   
    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.level.isClientSide()) {
            ServerLevel serverWorld = (ServerLevel) event.level;
            long time = serverWorld.getDayTime();
            int day = (int) time / 24000;
            int timeDay = (int) time - (day * 24000);
            boolean isNightTime = timeDay  >= 13000 && timeDay <= 23000;
            
            int percentDay = (int) time % 24000;          
            int hours = ((int) percentDay / 1000 + 6) % 24; // Adjust for Minecraft's day starting at 6:00 AM
            int minutes = (int) ((percentDay % 1000) / 16.6667); // Approximately convert ticks to minutes
            System.out.println("Current Minecraft day: " + day);
            System.out.printf("Current Minecraft time: %02d:%02d%n", hours, minutes);
            System.out.println("isNightTime: " + isNightTime);
            

            if (currentMob != null && currentTarget != null && currentWorld != null) {
                BlockPos mobPos = currentMob.blockPosition();
                BlockPos targetPos = currentTarget.blockPosition();

                // Call scheduleBlockBreak if it's night time and the mob can use the goal
                if (isNightTime && currentMob.getTarget() == currentTarget) {
                    scheduleBlockBreak(mobPos, targetPos);
                }
            }

            // Process block break tasks
            Iterator<BlockBreakTask> taskIterator = blockBreakTasks.iterator();
            while (taskIterator.hasNext()) {
                BlockBreakTask task = taskIterator.next();
                task.execute(); // Execute the stored block break task
                taskIterator.remove();
            }
        }
    }

    /**
     * Handles block restoration before explosion.
     */
    @SubscribeEvent
    public static void onExplosionStart(ExplosionEvent.Detonate event) {
        if (event.getExplosion().getExploder() instanceof Creeper) {
            ServerLevel world = (ServerLevel) event.getLevel();
            List<BlockPos> affectedBlocks = event.getAffectedBlocks();
            for (BlockPos pos : affectedBlocks) {
                BlockState state = world.getBlockState(pos);
                if (state.getBlock() != Blocks.AIR) {
                    BlockRestorer.addBrokenBlock(pos, state);
                }
            }
        }
    }

    /**
     * Functional interface representing a block break task.
     */
    @FunctionalInterface
    private interface BlockBreakTask {
        void execute();
    }
}
