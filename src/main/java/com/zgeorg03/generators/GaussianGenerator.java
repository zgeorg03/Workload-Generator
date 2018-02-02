package com.zgeorg03.generators;

import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by zgeorg03 on 4/14/17.
 */
public class GaussianGenerator extends Generator {
    private final Random random = new Random(System.currentTimeMillis());
    private final float mean;
    private final float deviation;
    public GaussianGenerator(AtomicReference<Float> throughput, float mean, float deviation) {
        super(throughput, "gaussian");
        this.mean = mean;
        this.deviation = deviation;
    }

    @Override
    public float generateLoad(long time) {
        float r = (float) (random.nextGaussian() * deviation + mean);
        if(r>1)
            r=1;
        if(r<0)
            r=0;
        return r;
    }
}
