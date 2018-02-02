package com.zgeorg03.models;

/**
 * Created by zgeorg03 on 4/14/17.
 */
public class RealTimeOperationStats {
    private final String id;
    private long count;
    private long avgDuration;
    private long minDuration =Long.MAX_VALUE;
    private long maxDuration = Long.MIN_VALUE;
    private long sum;

    private long countSuccessStatus;
    private long countRedirectionStatus;
    private long countClientErrorStatus;
    private long countServerErrorStatus;

    public RealTimeOperationStats(String id) {
        this.id = id;
    }

    public void update(long duration, int statusCode) {
        count++;
        sum+=duration;
        if(duration<minDuration)
            minDuration=duration;
        if(duration>maxDuration)
            maxDuration=duration;
        avgDuration = sum/count;
        if(statusCode<300)
            countSuccessStatus++;
        else if(statusCode<400)
            countRedirectionStatus++;
        else if(statusCode<500)
            countClientErrorStatus++;
        else if(statusCode<600)
            countServerErrorStatus++;

    }

    public long getAvgDuration() {
        return avgDuration;
    }

    public long getCount() {
        return count;
    }

    public long getMaxDuration() {
        return maxDuration;
    }

    public long getMinDuration() {
        return minDuration;
    }

    public String getId() {
        return id;
    }

    public long getCountClientErrorStatus() {
        return countClientErrorStatus;
    }

    public long getCountRedirectionStatus() {
        return countRedirectionStatus;
    }

    public long getCountSuccessStatus() {
        return countSuccessStatus;
    }

    public long getCountServerErrorStatus() {
        return countServerErrorStatus;
    }

    public long getSum() {
        return sum;
    }

    @Override
    public String toString() {
        return  id +
                ": count=" + count +
                ", avg_latency=" + avgDuration +
                ", min_latency=" + minDuration +
                ", max_latency=" + maxDuration +
                ", success=" + countSuccessStatus +
                ", redirection=" + countRedirectionStatus +
                ", client_errors=" + countClientErrorStatus +
                ", server_errors=" + countServerErrorStatus ;
    }
}
