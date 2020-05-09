package com.romanwuattier.urlservice;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ThreadPoolUtility {
    private static final int CPU_CORES = Runtime.getRuntime().availableProcessors();
    private static final int NB_CPU_THREAD = (int) (CPU_CORES * 0.8 * (1 + (float) (2 / 10)));
    private static final int NB_IO_THREADS = (int) (CPU_CORES * 0.2 * (1 + (float) (30 / 3)));

    /**
     * {@link ExecutorService} used for CPU intensive tasks.
     * The {@code NB_CPU_THREAD} = Number of Available Cores * Target CPU utilization * (1 + Wait time (ms) / Service time (ms))
     * Where:
     * Number of Available Cores = {@code CPU_CORES}
     * Target CPU utilization = 0.8 (80% of the CPU)
     * Wait time = 2 ms
     * Service time = 10 ms
     */
    public static final ExecutorService CPU_FIXED_EXECUTOR = Executors.newFixedThreadPool(NB_CPU_THREAD);

    /**
     * {@link ExecutorService} used for CPU intensive tasks.
     * The {@code NB_IO_THREAD} = Number of Available Cores * Target CPU utilization * (1 + Wait time (ms) / Service time (ms))
     * Where:
     * Number of Available Cores = {@code CPU_CORES}
     * Target CPU utilization = 0.2 (20% of the CPU)
     * Wait time = 30 ms
     * Service time = 3 ms
     */
    public static final ExecutorService IO_FIXED_EXECUTOR = Executors.newFixedThreadPool(NB_IO_THREADS);
}
