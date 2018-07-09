package com.zgeorg03.models;


import com.zgeorg03.generators.Generator;

/**
 * Created by zgeorg03 on 4/14/17.
 */
public class Config {
    private final Generator generator;
    private final String experiment;
    private final int maxThreads;
    private final int minOperations;
    private final int maxOperations;
    private final int outputTime;
    private final int timeOut;

    public Config(Generator generator, String experiment, int maxThreads, int minOperations, int maxOperations, int outputTime, int timeOut) {
        this.generator = generator;
        this.experiment = experiment;
        this.maxThreads = maxThreads;
        this.minOperations = minOperations;
        this.maxOperations = maxOperations;
        this.outputTime = outputTime;
        this.timeOut = timeOut;
    }

    public int getTimeOut() {
        return timeOut;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public int getMaxOperations() {
        return maxOperations;
    }

    public int getMinOperations() {
        return minOperations;
    }

    public int getOutputTime() {
        return outputTime;
    }

    public String getExperiment() {
        return experiment;
    }

    public Generator getGenerator() {
        return generator;
    }
}
