package com.tejasisamazing;

import com.tejasisamazing.utils.TaskScheduler;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MemoryManager implements ModInitializer {
    public static final String MOD_ID = "memory-manager";

    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final ModConfig CONFIG = ModConfig.createAndLoad();

    // This is not required, as that is only dev environment requirement
//    static {
//        // Disable owo-lib client-side requirement TODO: TEST THIS.
////        System.setProperty("owo.debug","false");
////		System.setProperty("owo.handshake.disable", "true");
//    }


    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.


        LOGGER.info("Loading Memory manager!");
        MemoryCommand.init();
        TaskScheduler.register();

    }


}