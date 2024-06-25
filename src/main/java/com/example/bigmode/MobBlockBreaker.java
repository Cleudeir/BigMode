package com.example.bigmode;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = YourMod.MODID)
public class MobBlockBreaker {
    private static Mob currentMob;
    private static ServerLevel currentWorld;
    private static Player currentPlayer;
    private static int currentTicks;

    public static void enableMobBlockBreaking(Mob mob, ServerLevel world, Player target, int ticks) {
        currentMob = mob;
        currentWorld = world;
        currentPlayer = target;
        currentTicks = ticks;
        scheduleBlockBreak();
    }

    private static void scheduleBlockBreak() {
        if (currentMob == null || currentWorld == null || currentPlayer == null) {
            return;
        }
        // position player
        int playerX = (int) currentPlayer.getX();
        int playerY = (int) currentPlayer.getY();
        int playerZ = (int) currentPlayer.getZ();
        // position mob
        int mobX = (int) currentMob.getX();
        int mobY = (int) currentMob.getY();
        int mobZ = (int) currentMob.getZ();
        // difference
        int xDiff = playerX - mobX;
        int yDiff = playerY - mobY;
        int zDiff = playerZ - mobZ;
        // block break
        int blockX = mobX;
        int blockY = mobY;
        int blockZ = mobZ;

        if (Math.abs(xDiff) > Math.abs(yDiff) && Math.abs(xDiff) > Math.abs(zDiff)) {
            blockX += xDiff > 0 ? 1 : -1;
        } else if (Math.abs(yDiff) > Math.abs(xDiff) && Math.abs(yDiff) > Math.abs(zDiff)) {
            blockY += yDiff > 0 ? 1 : -1;
        } else if (Math.abs(zDiff) > Math.abs(xDiff) && Math.abs(zDiff) > Math.abs(yDiff)) {
            blockZ += zDiff > 0 ? 1 : -1;
        }

        System.out.println("player: X:" + playerX + "Y:" + playerY + "Z:" + playerZ);
        System.out.println("mob: X:" + mobX + "Y:" + mobY + "Z:" + mobZ);
        System.out.println("blockX: X:" + blockX + "Y:" + blockY + "Z:" + blockZ);
        blockBreak(blockX, blockY, blockZ);
    }

    private static void blockBreak(int blockX, int blockY, int blockZ) {
        BlockPos blockPos = new BlockPos(blockX, blockY, blockZ);
        BlockState state = currentWorld.getBlockState(blockPos);
        if (state.getBlock() != Blocks.AIR && state.getDestroySpeed(currentWorld, blockPos) >= 0) {
            if (currentTicks < 10) {
                currentWorld.levelEvent(2001, blockPos, Block.getId(state));
            } else {
                currentWorld.destroyBlock(blockPos, true, currentMob);
                currentWorld.playSound(null, blockPos, SoundEvents.STONE_BREAK, currentMob.getSoundSource(), 1.0F,
                        1.0F);
                BlockRestorer.addBrokenBlock(blockPos, state, currentWorld);
                MobSpawnHandler.resetTicks();
            }
        }
    }

    @SubscribeEvent
    public static void onExplosionStart(ExplosionEvent.Detonate event) {
        if (currentMob == null || currentWorld == null || currentPlayer == null) {
            return;
        }
        if (event.getExplosion().getExploder() instanceof Creeper) {
            List<BlockPos> affectedBlocks = event.getAffectedBlocks();
            for (BlockPos pos : affectedBlocks) {
                BlockState state = currentWorld.getBlockState(pos);
                if (state.getBlock() != Blocks.AIR) {
                    BlockRestorer.addBrokenBlock(pos, state, currentWorld);
                }
            }
        }
    }

}
