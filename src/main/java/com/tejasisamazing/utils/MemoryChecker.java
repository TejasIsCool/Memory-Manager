package com.tejasisamazing.utils;

import oshi.SystemInfo;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;


public class MemoryChecker {
    private static final SystemInfo SI = new SystemInfo();
    private static final OperatingSystem OS = SI.getOperatingSystem();

    public static double getMemoryUsage() {
        int pid = OS.getProcessId();
        OSProcess process = OS.getProcess(pid);
        double memoryEstimate = -1;
        if (process != null) {
            long bytes = process.getResidentSetSize();
            memoryEstimate = bytes / (1024.0 * 1024.0);
        }
        return memoryEstimate;
    }

    public static double maxMemory = Runtime.getRuntime().maxMemory() / (1024.0 * 1024.0);
}
