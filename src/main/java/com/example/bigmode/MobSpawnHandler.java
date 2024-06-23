package com.example.bigmode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = YourMod.MODID)
public class MobSpawnHandler {
    private static final int DAYS_INTERVAL = 7;
    private static final List<LivingEntity> currentWaveMobs = new ArrayList<>();
    private static final int maxMobs = 40;
    private static final int maxMobPack = 30;
    private static final int maxMobDiff = maxMobs - maxMobPack;

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.level.isClientSide()) {
            ServerLevel serverWorld = (ServerLevel) event.level;
            long time = serverWorld.getDayTime();
            int day = (int) time / 24000;
            int timeDay = (int) time - (day * 24000);
            boolean isNightTime = timeDay >= 13000 && timeDay <= 23000;

            if (day % DAYS_INTERVAL == 0) {
                if (isNightTime && currentWaveMobs.size() < maxMobPack) {
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
            if (currentWaveMobs.contains(mob)) {
                currentWaveMobs.remove(mob);
            }
        }
    }

    @SubscribeEvent
    public static void onLiving(PlayerRespawnEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player) {
            Player player = (Player) entity;
            ServerLevel world = (ServerLevel) player.level();
            targetMobsToPlayer(player);
            // Apply Damage Resistance effect for 5 seconds (100 ticks)
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 4, false, false));
            player.sendSystemMessage(
                    Component.translatable("You are invulnerable to damage for 5 seconds!"));
            mobTeleport(world, player);
        }
    }

    @SubscribeEvent
    public static void onLiving(PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Level world = event.player.getCommandSenderWorld();
            if (world instanceof ServerLevel) {
                ServerLevel serverWorld = (ServerLevel) world;
                long time = world.getDayTime();
                int day = (int) time / 24000;
                int timeDay = (int) time - (day * 24000);
                boolean isNightTime = timeDay >= 13000 && timeDay <= 23000;
                if (time % (20 * 30) == 0 && isNightTime && day % DAYS_INTERVAL == 0) {
                    Player player = event.player;
                    mobTeleport(serverWorld, player);
                }
            }

        }
    }

    public static void mobTeleport(ServerLevel world, Player player) {
        double playerPosX = player.getX();
        double playerPosY = player.getY();

        for (int i = 0; i < currentWaveMobs.size(); i++) {
            LivingEntity entity = currentWaveMobs.get(i);
            Mob mob = (Mob) entity;
            double firstMobPosX = mob.getX();
            double firstMobPosY = mob.getY();
            double distance = Math
                    .sqrt(Math.pow(playerPosX - firstMobPosX, 2) + Math.pow(playerPosY - firstMobPosY, 2));
            System.out.println("Distance: " + distance + ' ' + mob.getName().getString());
            if (distance > 30) {
                // teleport mobs
                double x = playerPosX + world.random.nextInt(30) - 40;
                double z = playerPosY + world.random.nextInt(30) - 40;
                double y = world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) x, (int) z);
                mob.teleportTo(x, y, z);
                mob.setTarget(player);
            }
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
        for (int i = 0; i < maxMobDiff * 2 / 4; i++) {
            Zombie zombie = new Zombie(EntityType.ZOMBIE, world);
            spawnAndTrack(world, zombie, player);
        }
    }

    private static void spawnSkeletons(ServerLevel world, Player player) {
        for (int i = 0; i < maxMobDiff * 1 / 4; i++) {
            Skeleton skeleton = new Skeleton(EntityType.SKELETON, world);
            spawnAndTrack(world, skeleton, player);
        }
    }

    private static void spawnCreepers(ServerLevel world, Player player) {
        for (int i = 0; i < maxMobDiff * 1 / 4; i++) {
            Creeper creeper = new Creeper(EntityType.CREEPER, world);
            spawnAndTrack(world, creeper, player);
        }
    }

    private static void spawnSpiders(ServerLevel world, Player player) {
        for (int i = 0; i < maxMobDiff * 1 / 4; i++) {
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
        double y = world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) x, (int) z);

        // Get the highest block at the (x, z) location to ensure the mob spawns on the
        // ground

        // Set the entity's position
        entity.setPos(x, y, z);

        // Set the entity's target to the player
        Mob mob = (Mob) entity;
        mob.setTarget(target);
        // mob.setHealth(1);

        if (mob instanceof Skeleton) {
            // Equip the skeleton with a bow and arrows
            Skeleton skeleton = (Skeleton) mob;

            // Check if the skeleton doesn't already have a bow (optional)
            if (!skeleton.isHolding(Items.BOW)) {
                skeleton.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW)); // Equip a bow
            }

            // Check if the skeleton doesn't already have arrows (optional)
            if (skeleton.getOffhandItem().isEmpty() || skeleton.getOffhandItem().getItem() != Items.ARROW) {
                skeleton.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.ARROW, 64)); // Give arrows
            }
        }

        // Spawn the entity and track it
        world.addFreshEntity(entity);
        MobBlockBreaker.enableMobBlockBreaking(mob, world, target);
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
