package com.example.bigmode;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = YourMod.MODID)
public class BlockRestorer {

    private static final List<BlockEntry> brokenBlocks = new ArrayList<>();
    private static int index = 0;
    private static ServerLevel serverWorld;

    public static void addBrokenBlock(BlockPos pos, BlockState state, ServerLevel currentWorld) {
        brokenBlocks.add(new BlockEntry(pos, state));
        serverWorld = currentWorld;
    }

    public static void restoreBlocks() {
        if (serverWorld == null || brokenBlocks.size() == 0) {
            return;
        }
        if (brokenBlocks.size() > 0) {
            BlockEntry item = brokenBlocks.get(index);
            BlockState state = item.getState();
            BlockPos pos = item.getPos();
            boolean entityExists = serverWorld.getEntities(null, state.getShape(serverWorld, pos).bounds().move(pos))
                    .size() > 0;
            System.out.println("brokenBlocks: " + brokenBlocks.size());
            if (state != null && state != Blocks.AIR.defaultBlockState() && serverWorld != null && !entityExists) {
                serverWorld.setBlockAndUpdate(pos, state);
                brokenBlocks.remove(index);
                index = 0;
            } else {
                if (index + 1 == brokenBlocks.size()) {
                    index = 0;
                } else {
                    index++;
                }
            }
        }
    }

    public static void animateBlockDestroyed() {
        if (serverWorld == null || brokenBlocks.size() == 0) {
            return;
        }
        if (brokenBlocks.size() > 0) {
            for (int i = 0; i < brokenBlocks.size(); i++) {
                BlockEntry item = brokenBlocks.get(i);
                BlockPos pos = item.getPos();
                BlockAnimationHandler.animateBlock(serverWorld, pos);
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
