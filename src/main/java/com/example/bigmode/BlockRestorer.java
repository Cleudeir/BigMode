package com.example.bigmode;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

    private static final Map<BlockPos, BlockState> brokenBlocks = new HashMap<>();

    public static void addBrokenBlock(BlockPos pos, BlockState state) {
        brokenBlocks.put(pos, state);
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.level.isClientSide()) {
            ServerLevel serverWorld = (ServerLevel) event.level;
            long time = serverWorld.getDayTime();
            int day = (int) time / 24000;
            int timeDay = (int) time - (day * 24000);
            boolean isNightTime = timeDay  >= 13000 && timeDay <= 23000;

            if (!isNightTime && brokenBlocks.size() > 0 && time % (20 * 10) == 0) {
                Iterator<Map.Entry<BlockPos, BlockState>> iterator = brokenBlocks.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<BlockPos, BlockState> entry = iterator.next();
                    BlockPos pos = entry.getKey();

                    // Verify if there's a mob or player at the position
                    List<Entity> entities = serverWorld.getEntities(null, new AABB(pos));
                    boolean positionOccupied = entities.stream().anyMatch(entity -> entity instanceof LivingEntity);

                    if (positionOccupied) {
                        System.out.println("Position " + pos + " is still occupied, skipping restoration.");
                        continue;
                    } else {
                        BlockState blockState = entry.getValue();
                        System.out.println("Restoring block at position: " + pos + " to state: " + blockState);
                        serverWorld.setBlockAndUpdate(pos, blockState);
                        System.out.println("Entry removed from the map.");
                        iterator.remove();
                    }
                }
            }
        }
    }
}
