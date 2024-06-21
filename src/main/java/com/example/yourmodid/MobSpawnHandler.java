package com.example.yourmodid;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

@Mod.EventBusSubscriber(modid = YourMod.MODID)
public class MobSpawnHandler {
    private static final int DAYS_INTERVAL = 7;
    private static final List<LivingEntity> currentWaveMobs = new ArrayList<>();
    private static final int maxMobs = 40;

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.level.isClientSide()) {
            ServerLevel serverWorld = (ServerLevel) event.level;
            long time = serverWorld.getDayTime();
            int dayDuration = 24000;
            int days = (int) (time / dayDuration);
            int timeOfDay = (int) (time % dayDuration);
            boolean isNightTime = timeOfDay >= 13000 && timeOfDay <= 23000;

            if (days % DAYS_INTERVAL == 0) {
                if (isNightTime && currentWaveMobs.size() < maxMobs / 2) {
                    spawnNextWave(serverWorld);
                }
            } else {
                currentWaveMobs.clear();
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {

        if (event.getEntity() instanceof Mob) {
            Mob mob = (Mob) event.getEntity();
            System.out.println(mob.getName().getString() + " has died.");
            System.err.println("currentWaveMobs >>>>>>>:" + currentWaveMobs.size());

            if (currentWaveMobs.contains(mob)) {
                currentWaveMobs.remove(mob);
                System.err.println("currentWaveMobs >>>>>>>:" + currentWaveMobs.size());
            }
        }
    }

    @SubscribeEvent
    public static void onLiving(PlayerRespawnEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player) {
            Player player = (Player) entity;
            targetMobsToPlayer(player);
            // Apply Damage Resistance effect for 5 seconds (100 ticks)
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 4, false, false));
            player.sendSystemMessage(
                    Component.translatable("You are invulnerable to damage for 5 seconds!"));
        }
    }

    public static void spawnCommandedMobWave(ServerLevel world) {
        spawnNextWave(world);
    }

    private static void spawnNextWave(ServerLevel world) {
        if (currentWaveMobs.isEmpty()) {
            Collection<ServerPlayer> players = world.getPlayers((Predicate<ServerPlayer>) p -> true);
            for (ServerPlayer player : players) {
                player.sendSystemMessage(
                        Component.translatable("The night starts, the mobs are incoming!"));
            }
        }
        for (Player player : world.players()) {
            spawnZombies(world, player);
            spawnSkeletons(world, player);
            spawnCreepers(world, player);
            spawnSpiders(world, player);
        }
    }

    private static void spawnZombies(ServerLevel world, Player player) {
        for (int i = 0; i < maxMobs * 3 / 8; i++) {
            Zombie zombie = new Zombie(EntityType.ZOMBIE, world);
            spawnAndTrack(world, zombie, player);
        }
    }

    private static void spawnSkeletons(ServerLevel world, Player player) {
        for (int i = 0; i < maxMobs / 4; i++) {
            Skeleton skeleton = new Skeleton(EntityType.SKELETON, world);
            spawnAndTrack(world, skeleton, player);
        }
    }

    private static void spawnCreepers(ServerLevel world, Player player) {
        for (int i = 0; i < maxMobs / 4; i++) {
            Creeper creeper = new Creeper(EntityType.CREEPER, world);
            spawnAndTrack(world, creeper, player);
        }
    }

    private static void spawnSpiders(ServerLevel world, Player player) {
        for (int i = 0; i < maxMobs / 8; i++) {
            Spider spider = new Spider(EntityType.SPIDER, world);
            spawnAndTrack(world, spider, player);
        }
    }

    private static void spawnAndTrack(ServerLevel world, LivingEntity entity, Player target) {
        if (currentWaveMobs.size() == maxMobs) {
            return;
        }
        // Calculate spawn position near the player
        double x = target.getX() + world.random.nextInt(30) - 40;
        double z = target.getZ() + world.random.nextInt(30) - 40;

        // Get the highest block at the (x, z) location to ensure the mob spawns on the
        // ground
        double y = world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) x, (int) z);

        // Set the entity's position
        entity.setPos(x, y, z);

        // Set the entity's target to the player
        Mob mob = (Mob) entity;
        mob.setTarget(target);
        mob.setHealth(2);

        // Spawn the entity and track it
        world.addFreshEntity(entity);
        currentWaveMobs.add(entity);
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
