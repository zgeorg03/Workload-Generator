package com.zgeorg03.generators;

import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by zgeorg03 on 4/20/17.
 */
public class MountainGenerator extends AbstractGenerator{

    private long start;
    private boolean firstTime=true;
    private final Random random = new Random(System.currentTimeMillis());
    private final long ascent;
    private final long descent;
    private final long plateau;
    private final long period;

    public MountainGenerator(AtomicReference<Float> throughput, long ascent, long descent, long plateau) {
        super(throughput, "mountain");
        this.ascent = ascent;
        this.descent = descent;
        this.plateau = plateau;
        this.period = ascent+descent+plateau;
    }

    @Override
    protected float generateLoad(long time) {
        if(firstTime){
            start = time;
            firstTime=false;
        }
        long duration = time- start;

        long m = duration % period;
        if(m<ascent){ //Ascending phase
            float perc = m/(float)ascent;

            if(perc<0)
                perc = 0;
            if(perc>1)
                perc = 1;
            return perc;

        }else if(m<ascent+plateau){ // Plateau Phase
            float perc = (float) (random.nextGaussian() * 0.005 + 1);
            if(perc<0)
                perc = 0;
            if(perc>1)
                perc = 1;
            return perc;
        }else{ // Descending phase
            float perc = (m-ascent-plateau)/(float)descent;
            if(perc<0)
                perc = 0;
            if(perc>1)
                perc = 1;
            return 1- perc;

        }
    }
}
