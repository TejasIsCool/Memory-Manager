package com.tejasisamazing.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.tejasisamazing.utils.TaskScheduler;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

import static com.tejasisamazing.MemoryManager.CONFIG;
import static com.tejasisamazing.MemoryManager.LOGGER;
import static com.tejasisamazing.utils.MemoryChecker.getMemoryUsage;
import static com.tejasisamazing.utils.MemoryChecker.maxMemory;

public class MemoryRestart {

    private static boolean restartCallOngoing = false;
    private static long timeStart = 0;

    private static Vector<Player> restartPlayersList = new Vector<>();

    public static int handleRestartCommand(CommandSourceStack source) {
        if (CONFIG.MemoryConfig.MemoryRestartConfig.memoryRestart()) {

            if (nowTimeDifference() >= CONFIG.MemoryConfig.MemoryRestartConfig.restartExpireTimeSeconds() * 1000L) {
                restartCallOngoing = false;
            }

            if (!restartCallOngoing) {
                double memoryEstimate = getMemoryUsage();
                source.sendSuccess(() -> Component.translatable("memory_manager.memory_usage", memoryEstimate, maxMemory), true);
                if ((memoryEstimate / maxMemory) * 100 < CONFIG.MemoryConfig.MemoryRestartConfig.memoryRestartPercent()) {
                    source.sendSuccess(() -> Component.translatable("memory_manager.memory_restart.not_enough"), true);
                    return 0;
                }

                // throughout this, console is counted as a null player

                // State setting / cleanup
                restartCallOngoing = true;
                // Starting a new memory restart call, so reset the players
                restartPlayersList = new Vector<>();
                restartPlayersList.add(source.getPlayer());
                timeStart = Util.getMillis();


                Component styledCommand = Component.literal("/memory restart")
                    .withStyle(style -> style
                        .withColor(ChatFormatting.RED)
                        .withUnderlined(true)
                        .withClickEvent(new ClickEvent.SuggestCommand("/memory restart"))
                        .withHoverEvent(new HoverEvent.ShowText(Component.translatable("memory_manager.memory_restart.hover_text")))
                    );

                try {
                    Component styledPlayerName = Component.literal(source.getPlayerOrException().getName().getString()).withStyle(ChatFormatting.RED);

                    // PlayerChatMessage
                    source.getServer().getPlayerList().broadcastSystemMessage(
                        Component.translatable("memory_manager.memory_restart.call_for_restart", styledPlayerName, styledCommand).withStyle(ChatFormatting.YELLOW),
                        false
                    );
                } catch (CommandSyntaxException e) {
                    // Console called

                    Component styledConsole = Component.literal("Console").withStyle(ChatFormatting.RED);

                    source.getServer().getPlayerList().broadcastSystemMessage(
                        Component.translatable("memory_manager.memory_restart.call_for_restart", styledConsole, styledCommand).withStyle(ChatFormatting.YELLOW),
                        false
                    );
                }

            } else {
                float time_remaining = CONFIG.MemoryConfig.MemoryRestartConfig.restartExpireTimeSeconds() - (nowTimeDifference() / 1000f);

                // Check if already in list
                if (restartPlayersList.contains(source.getPlayer())) {
                    source.sendSuccess(() -> Component.translatable(
                        "memory_manager.memory_restart.silent_restart_already_exists",
                        time_remaining
                    ), false);
                    return 0;
                }


                // Other players enter their names as well
                restartPlayersList.add(source.getPlayer());
                Component styledName;


                if (CONFIG.MemoryConfig.MemoryRestartConfig.broadcastPlayerProgress()) {
                    try {
                        styledName = Component.literal(source.getPlayerOrException().getName().getString()).withStyle(ChatFormatting.RED);
                    } catch (CommandSyntaxException e) {
                        styledName = Component.literal("Console").withStyle(ChatFormatting.RED);

                    }
                    source.getServer().getPlayerList().broadcastSystemMessage(Component.translatable(
                        "memory_manager.memory_restart.restart_agreement",
                        styledName,
                        player_agreement_percent(source),
                        CONFIG.MemoryConfig.MemoryRestartConfig.memoryRestartPlayerPercent(),
                        time_remaining
                    ), false);

                } else {
                    source.sendSuccess(() -> Component.translatable(
                        "memory_manager.memory_restart.silent_restart_agreement",
                        time_remaining
                    ), false);
                }

            }

            if (player_agreement_percent(source) >= CONFIG.MemoryConfig.MemoryRestartConfig.memoryRestartPlayerPercent()) {
                int restartTime = CONFIG.MemoryConfig.MemoryRestartConfig.restartingInXSeconds();
                // Restart the server!
                source.getServer().getPlayerList().broadcastSystemMessage(Component.translatable(
                    "memory_manager.memory_restart.threshold_reached",
                    restartTime
                ).withStyle(ChatFormatting.YELLOW), false);

                if (restartTime > 5) {
                    TaskScheduler.scheduleTask(restartTime - 5, "restart", () -> {
                        source.getServer().getPlayerList().broadcastSystemMessage(Component.translatable(
                            "memory_manager.memory_restart.five_seconds_before_restart"
                        ).withStyle(ChatFormatting.YELLOW), false);

                        TaskScheduler.scheduleTask(5, "restart", () -> {
                            source.getServer().getPlayerList().broadcastSystemMessage(Component.translatable(
                                "memory_manager.memory_restart.currently_restarting"
                            ).withStyle(ChatFormatting.YELLOW), false);
                            appropriateRestarter(source, false);
                        });


                    });
                } else if (restartTime > 0) {
                    TaskScheduler.scheduleTask(restartTime, "restart", () -> {
                        source.getServer().getPlayerList().broadcastSystemMessage(Component.translatable(
                            "memory_manager.memory_restart.currently_restarting"
                        ).withStyle(ChatFormatting.YELLOW), false);
                        appropriateRestarter(source, false);
                    });
                } else {
                    source.getServer().getPlayerList().broadcastSystemMessage(Component.translatable(
                        "memory_manager.memory_restart.currently_restarting"
                    ).withStyle(ChatFormatting.YELLOW), false);
                    appropriateRestarter(source, false);
                }


            }


        } else {
            source.sendSuccess(() -> Component.translatable("memory_manager.memory.disabled"), true);
        }
        return 0;
    }

    public static float player_agreement_percent(CommandSourceStack source) {
        return (((float) restartPlayersList.size()) / source.getServer().getPlayerList().getPlayerCount()) * 100;
    }

    public static long nowTimeDifference() {
        return Util.getMillis() - timeStart;
    }

    // TODO: Way of cancelling forced restarts

    public static void appropriateRestarter(CommandSourceStack source, boolean force) {
        // Potentially can add different things happening if force is true, but not sure what now


        if (CONFIG.MemoryConfig.MemoryRestartConfig.kickPlayersBeforeRestarting()) {
            for (ServerPlayer player : source.getServer().getPlayerList().getPlayers()) {
                player.connection.disconnect(Component.translatable("memory_manager.memory_restart.currently_restarting", false));
            }
        }

        int restartingChoice = CONFIG.MemoryConfig.MemoryRestartConfig.restartingChoice();
        // Using stop command
        if (restartingChoice == 0) {
            // Halt is the one used by minecraft /stop command!
            source.getServer().halt(false);
        }

        // Using custom restart terminal command
        if (restartingChoice == 1) {
            try {
                Runtime.getRuntime().exec(new String[]{CONFIG.MemoryConfig.MemoryRestartConfig.restartTerminalCommand()});
            } catch (IOException e) {
                LOGGER.info("Restarting server failed: "+ Arrays.toString(e.getStackTrace()));
            }
            if (CONFIG.MemoryConfig.MemoryRestartConfig.alsoStopServerWhenRunningRestartTerminalCommand()) {
                source.getServer().halt(false);
            }
        }


    }

    public static int handleForcedRestartCommand(CommandSourceStack source) {
        if (CONFIG.MemoryConfig.MemoryRestartConfig.forceMemoryRestart()) {

            int restartTime = CONFIG.MemoryConfig.MemoryRestartConfig.forceRestartingInXSeconds();
            // Restart the server!
            source.getServer().getPlayerList().broadcastSystemMessage(Component.translatable(
                "memory_manager.memory_restart_force.restartinginxseconds",
                restartTime
            ).withStyle(ChatFormatting.YELLOW), false);

            if (restartTime > 5) {
                TaskScheduler.scheduleTask(restartTime - 5, "restart", () -> {
                    source.getServer().getPlayerList().broadcastSystemMessage(Component.translatable(
                        "memory_manager.memory_restart.five_seconds_before_restart"
                    ).withStyle(ChatFormatting.YELLOW), false);

                    TaskScheduler.scheduleTask(5, "restart", () -> {
                        source.getServer().getPlayerList().broadcastSystemMessage(Component.translatable(
                            "memory_manager.memory_restart.currently_restarting"
                        ).withStyle(ChatFormatting.YELLOW), false);
                        appropriateRestarter(source, true);
                    });


                });
            } else if (restartTime > 0) {
                TaskScheduler.scheduleTask(restartTime, "restart", () -> {
                    source.getServer().getPlayerList().broadcastSystemMessage(Component.translatable(
                        "memory_manager.memory_restart.currently_restarting"
                    ).withStyle(ChatFormatting.YELLOW), false);
                    appropriateRestarter(source, true);
                });
            } else {
                source.getServer().getPlayerList().broadcastSystemMessage(Component.translatable(
                    "memory_manager.memory_restart.currently_restarting"
                ).withStyle(ChatFormatting.YELLOW), false);
                appropriateRestarter(source, true);
            }

        } else {
            source.sendSuccess(() -> Component.translatable("memory_manager.memory.disabled"), true);
        }
        return 0;
    }


    public static int restartCanceller(CommandSourceStack source) {
        // 1. Clear the scheduler
        TaskScheduler.cancelType("restart");

        // 2. Notify the players
        source.getServer().getPlayerList().broadcastSystemMessage(
            Component.translatable("memory_manager.memory_restart.cancel", false)
                .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD),
            false
        );
        return 0;
    }

}
