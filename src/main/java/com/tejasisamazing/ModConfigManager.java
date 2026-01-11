package com.tejasisamazing;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Nest;
import io.wispforest.owo.config.annotation.RangeConstraint;

@Config(name = "memory_manager", wrapperName = "ModConfig")
public class ModConfigManager {

    @Nest
    public MemoryCommand MemoryConfig = new MemoryCommand();

    public static class MemoryCommand {
        // The memory command
        public boolean memory = true;

        // memory clear
        public boolean memoryClear = true;


        // memory stop
        public boolean memoryStop = true;

        @RangeConstraint(min = 0, max = 100)
        public int memoryStopPercent = 70;

        @RangeConstraint(min = 0, max = 100)
        public int memoryStopPlayerPercent = 100;
    }

}
