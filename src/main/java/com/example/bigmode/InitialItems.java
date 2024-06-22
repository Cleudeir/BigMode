package com.example.bigmode;

import net.minecraft.world.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.io.File;

@Mod.EventBusSubscriber(modid = YourMod.MODID)
public class InitialItems {
    private static final Set<UUID> receivedInitialItems = new HashSet<>();

    public InitialItems() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        // Initialization code, if needed
        loadInitialItemsData();
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!receivedInitialItems.contains(event.getEntity().getUUID())) {
            giveInitialItems(event.getEntity());
            receivedInitialItems.add(event.getEntity().getUUID());
            saveInitialItemsData();
        }
    }

    private void giveInitialItems(Player player) {
        player.getInventory().add(new ItemStack(Items.STONE_SWORD, 1));
        player.getInventory().add(new ItemStack(Items.STONE_PICKAXE, 1));
        player.getInventory().add(new ItemStack(Items.STONE_AXE, 1));
        player.getInventory().add(new ItemStack(Items.STONE_SHOVEL, 1));
        player.getInventory().add(new ItemStack(Items.STONE_HOE, 1));
        player.getInventory().add(new ItemStack(Items.GLASS, 64));
        player.getInventory().add(new ItemStack(Items.BREAD, 8));
    }

    private String getWorldName() {   
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            return server.getWorldData().getLevelName();
        } else {
            // Handle null server case (example fallback)
            return "unknown_world";
        }
    }

    private void loadInitialItemsData() {
        String worldName = getWorldName();
        try {
            File file = new File("./config/bigmode/initial_items_" + worldName + ".dat"); // Adjust the path as needed
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                byte[] data = new byte[(int) file.length()];
                fis.read(data);
                fis.close();
                for (int i = 0; i < data.length; i += 16) {
                    long most = 0;
                    for (int j = 0; j < 8; j++) {
                        most <<= 8;
                        most |= data[i + j] & 0xff;
                    }
                    long least = 0;
                    for (int j = 8; j < 16; j++) {
                        least <<= 8;
                        least |= data[i + j] & 0xff;
                    }
                    receivedInitialItems.add(new UUID(most, least));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveInitialItemsData() {
        String worldName = getWorldName();
        try {
            File file = new File("./config/bigmode/initial_items_" + worldName + ".dat"); // Adjust the path as needed
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file);
            for (UUID uuid : receivedInitialItems) {
                long most = uuid.getMostSignificantBits();
                long least = uuid.getLeastSignificantBits();
                for (int i = 7; i >= 0; i--) {
                    fos.write((byte) (most >> (8 * i)));
                }
                for (int i = 7; i >= 0; i--) {
                    fos.write((byte) (least >> (8 * i)));
                }
            }
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
