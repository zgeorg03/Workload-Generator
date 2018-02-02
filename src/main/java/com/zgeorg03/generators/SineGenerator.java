package com.zgeorg03.generators;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by zgeorg03 on 4/14/17.
 */
public class SineGenerator extends Generator {
    private boolean firstTime =true;
    private long start;

    private final long period;
    public SineGenerator(AtomicReference<Float> throughput, long period) {
        super(throughput, "sine");
        this.period = period;
    }

    @Override
    public float generateLoad(long time) {
        if(firstTime){
            start = time;
            firstTime = false;
        }
        time-=start;
        float y = 0.5f+(float) Math.sin(2*Math.PI*time/ period)/2;
        return y;
    }
}
