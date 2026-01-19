package com.tejasisamazing.utils;

import net.minecraft.Util;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import java.util.ArrayList;
import java.util.List;

public class TaskScheduler {
    // Record to store task details
    private record ScheduledTask(long targetTime, String type, Runnable action) {}

    private static List<ScheduledTask> TASKS = new ArrayList<>();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (TASKS.isEmpty()) return;

            long currentTime = Util.getMillis();


            List<ScheduledTask> toRun = new ArrayList<>();
            TASKS.removeIf(task -> {
                if (currentTime >= task.targetTime) {
                    toRun.add(task);
                    return true;
                }
                return false;
            });

            // Now run the tasks
            for (ScheduledTask task : toRun) {
                task.action.run();
            }
        });
    }

    public static void scheduleTask(int seconds, String type, Runnable action) {
        long target = Util.getMillis() + (seconds * 1000L);
        TASKS.add(new ScheduledTask(target, type, action));
    }

    public static void cancelAll() {
        TASKS.clear(); // This removes every scheduled restart message and halt command
    }

    public static void cancelType(String cancelType) {
        List<ScheduledTask> newTaskList = new ArrayList<>();
        for (ScheduledTask task : TASKS) {
            if (!task.type.equals(cancelType)) {
                newTaskList.add(task);
            }
        }
        TASKS = newTaskList;
    }
}
