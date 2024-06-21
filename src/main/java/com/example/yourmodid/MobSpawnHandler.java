package com.example.yourmodid;

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
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.command.TextComponentHelper;
import java.util.Collection;
import java.util.function.Predicate;

@Mod.EventBusSubscriber(modid = YourMod.MODID)
public class MobSpawnHandler {
    private static final int DAYS_INTERVAL = 7;
    private static int lastTriggeredDay = -1;

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
                    player.sendSystemMessage(
                            TextComponentHelper.createComponentTranslation(player, "A new wave of mobs has spawned!"));
                }
                spawnMobWave(serverWorld);
            }
        }
    }

    public static void spawnCommandedMobWave(ServerLevel world) {
        lastTriggeredDay = -1;
        spawnMobWave(world);
       
    }

    public static void spawnMobWave(ServerLevel world) {
        for (Player player : world.players()) {
            spawnZombies(world, player);
            spawnSkeletons(world, player);
            spawnCreepers(world, player);
            spawnSpiders(world, player);
        }
    }

    private static void spawnZombies(ServerLevel world, Player player) {
        for (int i = 0; i < 15; i++) {
            Zombie zombie = new Zombie(EntityType.ZOMBIE, world);
            spawnAndAttack(world, zombie, player);
        }
    }

    private static void spawnSkeletons(ServerLevel world, Player player) {
        for (int i = 0; i < 10; i++) {
            Skeleton skeleton = new Skeleton(EntityType.SKELETON, world);
            spawnAndAttack(world, skeleton, player);
        }
    }

    private static void spawnCreepers(ServerLevel world, Player player) {
        for (int i = 0; i < 5; i++) {
            Creeper creeper = new Creeper(EntityType.CREEPER, world);
            spawnAndAttack(world, creeper, player);
        }
    }

    private static void spawnSpiders(ServerLevel world, Player player) {
        for (int i = 0; i < 8; i++) {
            Spider spider = new Spider(EntityType.SPIDER, world);
            spawnAndAttack(world, spider, player);
        }
    }

    private static void spawnAndAttack(ServerLevel world, LivingEntity entity, Player target) {
        // Set the entity's target to the player
        // Calculate spawn position near the player
        double x = target.getX() + world.random.nextInt(10) - 20;
        double y = target.getY();
        double z = target.getZ() + world.random.nextInt(10) - 20;
        entity.setPos(x, y, z);
        // Set target to the entity        
        ((Mob) entity).setTarget(target);        
        // Spawn the entity
        world.addFreshEntity(entity);
    }
}
