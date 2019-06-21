package com.zgeorg03.generators;

import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by zgeorg03 on 4/14/17.
 */
public class GaussianGenerator extends AbstractGenerator {
    private final Random random = new Random(System.currentTimeMillis());
    private final double mean;
    private final double sdev;

    public GaussianGenerator(AtomicReference<Float> throughput, double mean, double sdev) {
        super(throughput, "gaussian");
        this.mean = mean;
        this.sdev = sdev;
    }

    @Override
    public float generateLoad(long time) {
        float r = (float) (random.nextGaussian() * sdev + mean);
        if(r>1)
            r=1;
        if(r<0)
            r=0;
        return r;
    }
}
