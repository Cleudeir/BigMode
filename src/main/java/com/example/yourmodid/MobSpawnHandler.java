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
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
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
    private static final List<LivingEntity> currentWaveMobs = new ArrayList<>();
  

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.level.isClientSide()) {
            ServerLevel serverWorld = (ServerLevel) event.level;
            long time = serverWorld.getDayTime();
            int dayDuration = 24000;
            int days = (int) (time / dayDuration);
            int timeOfDay = (int) (time % dayDuration);
            boolean isNightTime = timeOfDay >= 13000 && timeOfDay <= 23000;
           
            if (days % DAYS_INTERVAL == 0 && currentWaveMobs.isEmpty()) {
                if(isNightTime){
                    Collection<ServerPlayer> players = serverWorld.getPlayers((Predicate<ServerPlayer>) p -> true);
                    for (ServerPlayer player : players) {
                        player.sendSystemMessage(Component.translatable("A new wave of mobs has spawned!"));
                    }
                    spawnNextWave(serverWorld);
                }else {
                    currentWaveMobs.clear();
                }             
            }
        }
    }

    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Mob) {
            Mob mob = (Mob) event.getEntity();
            if (currentWaveMobs.contains(mob)) {
                currentWaveMobs.remove(mob);
                System.err.println(currentWaveMobs.size());
            }
        }
    }

    @SubscribeEvent
    public static void onLiving(PlayerRespawnEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player) {
            Player player = (Player) entity;
            player.sendSystemMessage(
                    Component.translatable(">>>>>>>>RESPAWNED<<<<<<<<<<<! " + player.getDisplayName().getString()));
            targetMobsToPlayer(player);
        }
    }

    public static void spawnCommandedMobWave(ServerLevel world) {
        spawnNextWave(world);
    }

    private static void spawnNextWave(ServerLevel world) {
        currentWaveMobs.clear();    
        for (Player player : world.players()) {
            spawnZombies(world, player);
            spawnSkeletons(world, player);
            spawnCreepers(world, player);
            // spawnSpiders(world, player);
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
        currentWaveMobs.add(entity);
        // Calculate spawn position near the player
        double x = target.getX() + world.random.nextInt(20) - 10;
        double y = target.getY();
        double z = target.getZ() + world.random.nextInt(20) - 10;
        entity.setPos(x, y, z);

        // Set the entity's target to the player
        Mob mob = (Mob) entity;
        mob.setTarget(target);
        mob.setHealth(2);
        // Spawn the entity and track it
        world.addFreshEntity(entity);
    }

    private static void notifyPlayersOfMobCounts(ServerLevel world) {
        StringBuilder message = new StringBuilder("Current Wave Mob Counts:\n" + currentWaveMobs.size());        
        for (Player player : world.players()) {
            player.sendSystemMessage(Component.literal(message.toString()));
        }
    }

    private static void targetMobsToPlayer(Player player) {
        for (LivingEntity mob : currentWaveMobs) {
            if (mob instanceof Mob) {
                Mob creature = (Mob) mob;
                creature.setTarget(player);
            }
        }
    }
}
