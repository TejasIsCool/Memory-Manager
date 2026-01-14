package com.tejasisamazing;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import static com.tejasisamazing.MemoryManager.CONFIG;
import static com.tejasisamazing.utils.MemoryChecker.getMemoryUsage;
import static com.tejasisamazing.utils.MemoryChecker.maxMemory;


public class MemoryCommand {


    private static boolean memoryStopOngoing = false;

    private static int handleMemoryCommand(CommandSourceStack source) {
        if (CONFIG.MemoryConfig.memory()) {
            double memoryEstimate = getMemoryUsage();
            source.sendSuccess(() -> Component.translatable("memory_manager.memory_usage", memoryEstimate, maxMemory), true);
        } else {
            source.sendSuccess(() -> Component.translatable("memory_manager.memory.disabled"), true);
        }
        return 0;
    }

    private static int handleClearCommand(CommandSourceStack source) {
        if (CONFIG.MemoryConfig.memoryClear()) {
            source.sendSuccess(() -> Component.translatable("memory_manager.memory_clear.start"), true);
            double memory_before = getMemoryUsage();

            System.gc();
            try {
                Thread.sleep(1200L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.gc();

            source.sendSuccess(() -> Component.translatable("memory_manager.memory_clear.over", memory_before - getMemoryUsage()), true);
        } else {
            source.sendSuccess(() -> Component.translatable("memory_manager.memory.disabled"), true);
        }
        return 0;
    }

    private static int handleStopCommand(CommandSourceStack source) {
        if (CONFIG.MemoryConfig.memoryStop()) {
            if (!memoryStopOngoing) {
                double memoryEstimate = getMemoryUsage();
                source.sendSuccess(() -> Component.translatable("memory_manager.memory_usage", memoryEstimate, maxMemory), true);
                if (memoryEstimate / maxMemory < CONFIG.MemoryConfig.memoryStopPercent()) {
                    source.sendSuccess(() -> Component.translatable("memory_manager.memory_stop.not_enough"), true);
                    return 0;
                }
                memoryStopOngoing = true;
//                serverPlayer executing_player = source.getPlayer();
            } else {

            }

            // Ask other players for restarting!

        } else {
            source.sendSuccess(() -> Component.translatable("memory_manager.memory.disabled"), true);
        }
        return 0;
    }


    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                Commands.literal("memory")
                    .requires(Permissions.require("memory_manager.command.memory", 0))
                    .executes(context -> handleMemoryCommand(context.getSource()))
                    .then(Commands.literal("clear")
                        .requires(Permissions.require("memory_manager.command.memory_clear", 0))
                        .executes(context -> handleClearCommand(context.getSource())))
//                    .then(Commands.literal("stop")
//                        .requires(Permissions.require("memory_manager.command.memory_stop", 0))
//                        .executes(context -> handleStopCommand(context.getSource())))
            );
        });
    }
}
