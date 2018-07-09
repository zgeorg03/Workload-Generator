package com.zgeorg03.models;

import com.zgeorg03.generators.Generator;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by zgeorg03 on 4/14/17.
 */
public class Configuration {
    private final List<Operation> operations;
    private final Config config;


    private long lastOperation;
    private float totalWeight; // 1-> MaxOperationsPerSec
    private final AtomicReference<Float> throughput; // 1-> MaxOperationsPerSec
    private String date;

    public Configuration(List<Operation> operations, Config config){
        this.operations = operations;
        this.config = config;
        totalWeight = operations.stream().mapToInt(Operation::getWeight).sum();
        this.throughput = config.getGenerator().getThroughput();
    }

    /**
     * According to the configuration, we get the next operation
     * @param now
     * @return
     */
    public Optional<Operation> getNextOperation(long now) {
        long duration = now - lastOperation;
        if(operations.isEmpty())
            return Optional.empty();

        //System.out.println(getDelay());
        if(duration<getDelay())
            return Optional.empty();

        lastOperation = now;
        return Optional.ofNullable(pickRandomWeightedOperation());

    }

    public int getMaxThreads() {
        return config.getMaxThreads();
    }

    public int getMaxOperations() {
        return config.getMaxOperations();
    }
    public int getMinOperations() {
        return config.getMinOperations();
    }

    public int getDelay(){
        int min = config.getMinOperations();
        int d = config.getMaxOperations()-min;
        float operations =  min + throughput.get()*d;
        return (int)(1000/operations);
    }

    public Operation pickRandomWeightedOperation(){

        float rnd = (float) (Math.random() *totalWeight);
        float currentWeight = 0f;
        for(Operation operation : operations){
            currentWeight+=operation.getWeight();
            if(currentWeight>=rnd)
                return operation;
        }

        throw new RuntimeException("Shouldn't happen");
    }

    public int getOutputTime() {
        return config.getOutputTime();
    }

    public Generator getGenerator(){
        return config.getGenerator();
    }

    public String getExperimentName(){
        return config.getExperiment();
    }

    public String toCSV() {
        StringBuilder sb = new StringBuilder();
        sb.append("#\tExperimentID="+config.getExperiment());
        sb.append("\n#\tDate="+date);
        sb.append("\n#\tThreads="+config.getMaxThreads());
        sb.append("\n#\tMinOperations="+config.getMinOperations());
        sb.append("\n#\tMaxOperations="+config.getMaxOperations());
        sb.append("\n#\tTimeOut="+config.getTimeOut());
        sb.append("\n#\tOutputTime="+config.getOutputTime());
        sb.append("\n");
        return sb.toString();


    }
    public int getTimeOut(){
        return config.getTimeOut();
    }

    public void setDate(String date) {
        this.date = date;
    }
}
