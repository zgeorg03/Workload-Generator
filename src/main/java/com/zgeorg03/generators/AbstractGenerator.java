package com.zgeorg03.generators;

import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractGenerator {

    protected final AtomicReference<Float> throughput;
    protected int duration;
    protected final String name;
    private long lastTime;
    private long fistTime;
    private boolean isFirstTime=true;

    private Integer minOperationsPerSec;
    private Integer maxOperationsPerSec;

    public AbstractGenerator(AtomicReference<Float> throughput, String name) {
        this.throughput = throughput;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    protected abstract float generateLoad(long time);

    public void reset(){
        this.isFirstTime = true;

    }

    public void run(long time){
        if(isFirstTime){
            fistTime = time;
            isFirstTime = false;
        }
        long duration = time-lastTime;
        if(duration<1000)
            return;
        lastTime=time;
        throughput.set(generateLoad(time-fistTime));
    }

    public AtomicReference<Float> getThroughput() {
        return (throughput==null)? new AtomicReference<>(0f):throughput;
    }

    public void setMinOperationsPerSec(Integer minOperationsPerSec) {
        this.minOperationsPerSec = minOperationsPerSec;
    }
    public void setMaxOperationsPerSec(Integer maxOperationsPerSec) {
        this.maxOperationsPerSec = maxOperationsPerSec;
    }

    public Integer getMaxOperationsPerSec() {
        return maxOperationsPerSec;
    }

    public Integer getMinOperationsPerSec() {
        return minOperationsPerSec;
    }
}
