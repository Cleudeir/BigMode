package com.example.bigmode;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(YourMod.MODID)
public class YourMod {
    public static final String MODID = "bigmode";

    public YourMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register the MobSpawnHandler
        MinecraftForge.EVENT_BUS.register(new MobSpawnHandler());
        MinecraftForge.EVENT_BUS.register(new ModCommands());      
        MinecraftForge.EVENT_BUS.register(new InitialItems());
    }

    private void setup(final FMLCommonSetupEvent event) {
        // Some preinit code
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        // Some client setup code
    }
}
