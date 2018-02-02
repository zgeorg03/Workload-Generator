package com.zgeorg03.generators;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by zgeorg03 on 4/14/17.
 */
public abstract class Generator {

    protected final AtomicReference<Float> throughput;
    protected final String name;
    private long lastTime;
    private long fistTime;
    private boolean isFirstTime;

    public Generator(AtomicReference<Float> throughput, String name) {
        this.throughput = throughput;
        this.name = name;
    }

    protected abstract float generateLoad(long time);

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
}
