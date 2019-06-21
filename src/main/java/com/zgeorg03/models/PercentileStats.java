package com.zgeorg03.models;

import com.zgeorg03.utilities.SortedArrayList;

import java.util.HashMap;
import java.util.Map;

public class PercentileStats {
    private Map<String, SortedArrayList<Long>> stats = new HashMap<>();
    private long p99;
    private long median;
    public void update(String operation,long duration){
        SortedArrayList<Long> stat = stats.getOrDefault(operation, new SortedArrayList<>());
        stat.insertSorted(duration);
        stats.putIfAbsent(operation,stat);

    }

    public long getP99(String operation) {
        SortedArrayList<Long> stat = stats.getOrDefault(operation, new SortedArrayList<>());
        p99 = stat.get((int)(stat.size()*0.99));
        return p99;
    }

    public long getMedian(String operation) {
        SortedArrayList<Long> stat = stats.getOrDefault(operation, new SortedArrayList<>());
        median = stat.get((int)(stat.size()*0.99));
        return median;
    }

}
