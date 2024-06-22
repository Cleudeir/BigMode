package com.example.bigmode;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod.EventBusSubscriber(modid = YourMod.MODID)
public class InitialItems {
    public InitialItems() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        // Initialization code, if needed
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        event.getEntity().getInventory().add(new ItemStack(Items.STONE_SWORD, 1));
        event.getEntity().getInventory().add(new ItemStack(Items.STONE_PICKAXE, 1));
        event.getEntity().getInventory().add(new ItemStack(Items.STONE_AXE, 1));
        event.getEntity().getInventory().add(new ItemStack(Items.STONE_SHOVEL, 1));
        event.getEntity().getInventory().add(new ItemStack(Items.STONE_HOE, 1));
        event.getEntity().getInventory().add(new ItemStack(Items.GLASS, 64));
        event.getEntity().getInventory().add(new ItemStack(Items.BREAD, 8));
    }
}
