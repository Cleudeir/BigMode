package com.example.bigmode;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = YourMod.MODID)
public class BlockRestorer {

    private static final List<BlockEntry> brokenBlocks = new ArrayList<>();
    private static int index = 0;
    private static ServerLevel world = null;

    public static void addBrokenBlock(BlockPos pos, BlockState state) {
        brokenBlocks.add(new BlockEntry(pos, state));
        if (world != null) {
            // BlockAnimationHandler.animateBlockBreak(world, pos, state);
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.level.isClientSide()) {
            ServerLevel serverWorld = (ServerLevel) event.level;
            world = serverWorld;
            long time = serverWorld.getDayTime();
            int day = (int) time / 24000;
            int timeDay = (int) time - (day * 24000);
            boolean isNightTime = timeDay >= 13000 && timeDay <= 23000;

            if (!isNightTime && !brokenBlocks.isEmpty() && time % 20 * 1 / 4 == 0) {
                BlockEntry item = brokenBlocks.get(index);
                BlockState state = item.getState();
                BlockPos pos = item.getPos();
                // Verify if there's a mob or player at the position
                List<Entity> entities = serverWorld.getEntities(null, new AABB(pos));
                boolean positionOccupied = entities.stream().anyMatch(entity -> entity instanceof LivingEntity);

                if (positionOccupied) {
                    System.out.println("Position " + pos + " is still occupied, skipping restoration.");
                    index++;
                } else {
                    System.out.println("Restoring block at position: " + pos + " to state: " + state);
                    serverWorld.setBlockAndUpdate(pos, state);
                    System.out.println("Entry removed from the list.");
                    brokenBlocks.remove(index);
                    index = 0;
                }
            }
        }
    }

    private static class BlockEntry {
        private final BlockPos pos;
        private final BlockState state;

        public BlockEntry(BlockPos pos, BlockState state) {
            this.pos = pos;
            this.state = state;
        }

        public BlockPos getPos() {
            return pos;
        }

        public BlockState getState() {
            return state;
        }
    }
}
