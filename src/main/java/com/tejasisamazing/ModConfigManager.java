package com.tejasisamazing;

import blue.endless.jankson.Comment;
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
        @Nest
        public MemoryRestart MemoryRestartConfig = new MemoryRestart();

        public static class MemoryRestart {
            public boolean memoryRestart = true;


            @RangeConstraint(min = 0, max = 100)
            public int memoryRestartPercent = 80;

            @RangeConstraint(min = 0, max = 100)
            public int memoryRestartPlayerPercent = 100;

            @Comment("0: stop via Stop command, 1: Stop via custom restart, 2: Stop via running a script")
            @RangeConstraint(min=0,max=2)
            public int restartingChoice = 0;

            @Comment("The number of seconds to allow memory stop command acceptance")
            public int restartExpireTimeSeconds = 60;


            public boolean broadcastPlayerProgress = true;

            public int restartingInXSeconds = 30;

            @Comment("Enter a command that gets runs to restart server (usually using a script). Set restartingChoice to 1 for this")
            public String restartTerminalCommand = "";

            public boolean alsoStopServerWhenRunningRestartTerminalCommand = true;

            @Comment("Kicking the players will save their inventory, useful if restarting the server via external means")
            public boolean kickPlayersBeforeRestarting = true;


            @Comment("Enabling /memory restart force")
            public boolean forceMemoryRestart = true;

            public int forceRestartingInXSeconds = 10;





        }

    }

}
