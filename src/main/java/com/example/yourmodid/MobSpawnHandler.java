package com.example.yourmodid;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Mod.EventBusSubscriber(modid = YourMod.MODID)
public class MobSpawnHandler {
    private static final int DAYS_INTERVAL = 7;
    private static int lastTriggeredDay = -1;
    private static final List<LivingEntity> currentWaveMobs = new ArrayList<>();
    private static int currentWave = 0;
    private static final Map<EntityType<?>, Integer> mobCounts = new HashMap<>();
    private static int mobsRemainingInWave = 0;

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.level.isClientSide()) {
            ServerLevel serverWorld = (ServerLevel) event.level;
            long time = serverWorld.getDayTime();
            int dayDuration = 24000;
            int days = (int) (time / dayDuration);

            // Check if it is night time (between 13000 and 23000 ticks)
            int timeOfDay = (int) (time % dayDuration);
            boolean isNightTime = timeOfDay >= 13000 && timeOfDay <= 23000;

            if (days % DAYS_INTERVAL == 0 && days != lastTriggeredDay && isNightTime) {
                lastTriggeredDay = days;
                Collection<ServerPlayer> players = serverWorld.getPlayers((Predicate<ServerPlayer>) p -> true);
                for (ServerPlayer player : players) {
                    player.sendSystemMessage(Component.translatable("A new wave of mobs has spawned!"));
                }
                currentWave = 0;
                spawnNextWave(serverWorld);
            }

            // Check if the current wave is defeated
            if (currentWave > 0 && mobsRemainingInWave == 0) {
                spawnNextWave(serverWorld);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        currentWaveMobs.remove(event.getEntity());
        mobsRemainingInWave--;
    }

    public static void spawnCommandedMobWave(ServerLevel world) {
        lastTriggeredDay = -1;
        currentWave = 0;
        spawnNextWave(world);
    }

    private static void spawnNextWave(ServerLevel world) {
        currentWave++;
        currentWaveMobs.clear();
        mobCounts.clear();
        mobsRemainingInWave = 0;

        for (Player player : world.players()) {
            spawnZombies(world, player);
            spawnSkeletons(world, player);
            spawnCreepers(world, player);
            spawnSpiders(world, player);
        }

        notifyPlayersOfMobCounts(world);
    }

    private static void spawnZombies(ServerLevel world, Player player) {
        for (int i = 0; i < 5; i++) {
            Zombie zombie = new Zombie(EntityType.ZOMBIE, world);
            spawnAndTrack(world, zombie, player);
        }
    }

    private static void spawnSkeletons(ServerLevel world, Player player) {
        for (int i = 0; i < 5; i++) {
            Skeleton skeleton = new Skeleton(EntityType.SKELETON, world);
            spawnAndTrack(world, skeleton, player);
        }
    }

    private static void spawnCreepers(ServerLevel world, Player player) {
        for (int i = 0; i < 5; i++) {
            Creeper creeper = new Creeper(EntityType.CREEPER, world);
            spawnAndTrack(world, creeper, player);
        }
    }

    private static void spawnSpiders(ServerLevel world, Player player) {
        for (int i = 0; i < 5; i++) {
            Spider spider = new Spider(EntityType.SPIDER, world);
            spawnAndTrack(world, spider, player);
        }
    }

    private static void spawnAndTrack(ServerLevel world, LivingEntity entity, Player target) {
        // Calculate spawn position near the player
        double x = target.getX() + world.random.nextInt(10) - 30;
        double y = target.getY();
        double z = target.getZ() + world.random.nextInt(10) - 30;
        entity.setPos(x, y, z);

        // Set the entity's target to the player
        Mob mob = (Mob) entity;
        mob.setTarget(target);

        // Spawn the entity and track it
        world.addFreshEntity(entity);
        currentWaveMobs.add(entity);
        mobsRemainingInWave++;

        // Update mob count
        mobCounts.put(entity.getType(), mobCounts.getOrDefault(entity.getType(), 0) + 1);
    }

    private static void notifyPlayersOfMobCounts(ServerLevel world) {
        StringBuilder message = new StringBuilder("Current Wave Mob Counts:\n");
        for (Map.Entry<EntityType<?>, Integer> entry : mobCounts.entrySet()) {
            message.append(entry.getKey().getDescription().getString()).append(": ").append(entry.getValue()).append("\n");
        }
        for (Player player : world.players()) {
            player.sendSystemMessage(Component.literal(message.toString()));
        }
    }
}
