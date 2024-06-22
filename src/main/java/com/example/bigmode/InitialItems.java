package com.example.bigmode;

import java.util.HashSet;
import java.util.UUID;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod.EventBusSubscriber(modid = YourMod.MODID)
public class InitialItems {
    private HashSet<UUID> items = new HashSet<>();
  

    public InitialItems() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        // Initialization code, if needed
        items = storage.loadInitialItemsData("initial_data");    
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!items.contains(event.getEntity().getUUID())) {
            giveInitialItems(event.getEntity());
            items.add(event.getEntity().getUUID());
            storage.saveInitialItemsData("initial_data",items);
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
}
