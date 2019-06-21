package com.zgeorg03.utilities;

import com.zgeorg03.generators.AbstractGenerator;
import com.zgeorg03.generators.GaussianGenerator;
import com.zgeorg03.generators.MountainGenerator;
import com.zgeorg03.generators.SineGenerator;
import com.zgeorg03.models.Operation;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class Configuration {
    private String experiment = "test";
    private int threads = 1;
    private int outputTime = 5000;
    private int maxOperationsPerSec = 40;
    private int minOperationsPerSec = 10;
    private int timeout = 10000;
    private long seed = -1;
    private AbstractGenerator actualGenerator;
    private List<Operation> operationsReal = new LinkedList<>();
    private List<Map<String,Object>> operations = new LinkedList<>();

    private List<String> sequence = new LinkedList<>();
    private List<Map<String,Object>> generators;
    private Map<String,AbstractGenerator> actualGenerators = new HashMap<>();



    public List<String> getSequence() {
        return sequence;
    }

    public void setSequence(List<String> sequence) {
        this.sequence = sequence;
    }

    public Map<String, AbstractGenerator> getActualGenerators() {
        return actualGenerators;
    }

    public AbstractGenerator getActualGenerator() {
        return actualGenerator;
    }

    public void setActualGenerator(AbstractGenerator actualGenerator) throws Exception {
        this.actualGenerator = actualGenerator;
    }

    public List<Operation> getOperationsReal() {
        return operationsReal;
    }


    public void setOperationsReal(List<Operation> operationsReal) {
        this.operationsReal = operationsReal;
        totalWeight = operationsReal.stream().mapToInt(Operation::getWeight).sum();
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
        System.out.println("Timeout configured");

    }

    public void setOperations(List<Map<String, Object>> operations) throws URISyntaxException, UnsupportedEncodingException {
        this.operations = operations;
        for(Map<String, Object> operation : operations){
            String id = (String) operation.get("id");
            String method = (String) operation.get("method");
            int duration = (int) operation.getOrDefault("weight",1) ;
            String url = (String) operation.getOrDefault("url", "");
           Operation op;
            if(method.equalsIgnoreCase("GET")){
                op = new GetRequest(id,url,duration,timeout);
                this.operationsReal.add(op);

            }else if(method.equalsIgnoreCase("POST")){
                op = new PostRequest(id,duration,url,null,timeout);
                this.operationsReal.add(op);
            }else{
                System.out.println("Skipped operation: "+id);
            }

        }
    }

    //Dynamic
    private Random random;
    private long lastOperation;
    private float totalWeight; // 1-> MaxOperationsPerSec
    private AtomicReference<Float> throughput;
    private String date;
    public Configuration() {
        this.random = new Random(seed);
        totalWeight = operationsReal.stream().mapToInt(Operation::getWeight).sum();
        this.throughput = new AtomicReference<>(0f);

    }

    @Override
    public String toString() {
        return "Configuration{" +
                "experiment='" + experiment + '\'' +
                ", threads=" + threads +
                ", outputTime=" + outputTime +
                ", maxOperationsPerSec=" + maxOperationsPerSec +
                ", minOperationsPerSec=" + minOperationsPerSec +
                ", timeout=" + timeout +
                ", seed=" + seed +
                ", actualGenerator=" + actualGenerator +
                ", operationsReal=" + operationsReal +
                ", random=" + random +
                ", lastOperation=" + lastOperation +
                ", totalWeight=" + totalWeight +
                ", throughput=" + throughput +
                ", date='" + date + '\'' +
                '}';
    }

    public int getMinOperationsPerSec() {
        return minOperationsPerSec;
    }

    public void setMinOperationsPerSec(int minOperationsPerSec) {
        this.minOperationsPerSec = minOperationsPerSec;
    }


    public String getExperiment() {
        return experiment;
    }


    public void setGenerators(List<Map<String,Object>> generators) throws Exception {
        this.generators = generators;
        for(Map<String, Object> generator : generators){
            AbstractGenerator abstractGenerator;
            String id = (String) generator.get("id");
            int duration = (int) generator.getOrDefault("duration",300) * 1000;
            String type = (String) generator.getOrDefault("type", "gaussian");

            Integer maxOperationsPerSec = (Integer) generator.get("maxOperationsPerSec");
            Integer minOperationsPerSec = (Integer) generator.get("minOperationsPerSec");


            if (type.equals("gaussian")) {

                double mean = (double) generator.getOrDefault("mean", 0.5f);
                double sdev = (double) generator.getOrDefault("sdev", 0.1f);

                abstractGenerator = new GaussianGenerator(throughput, mean, sdev);
                abstractGenerator.setDuration(duration);
                actualGenerators.put(id,abstractGenerator);

            } else if (type.equals("sine")) {

                int period = (int) generator.getOrDefault("period", 300);

                abstractGenerator = new SineGenerator(throughput, period * 1000);
                abstractGenerator.setDuration(duration);
                actualGenerators.put(id,abstractGenerator);



            } else if (type.equals("mountain")) {

                long ascent = (int) generator.getOrDefault("ascent", 100);
                long plateau = (int) generator.getOrDefault("plateau", 100);
                long descent = (int) generator.getOrDefault("descent", 100);

                abstractGenerator = new MountainGenerator(throughput, ascent * 1000, descent * 1000, plateau * 1000);
                abstractGenerator.setDuration(duration);
                actualGenerators.put(id,abstractGenerator);

            } else {

                throw new Exception("Generator not found!");
            }


            if(minOperationsPerSec!=null){
                abstractGenerator.setMinOperationsPerSec(minOperationsPerSec);
            }
            if(maxOperationsPerSec!=null){
                abstractGenerator.setMaxOperationsPerSec(maxOperationsPerSec);
            }
        }
    }

    public void setExperiment(String experiment) {
        this.experiment = experiment;
    }

    public int getThreads() {
        return threads;
    }

    public void setThroughput(AtomicReference<Float> throughput) {
        this.throughput = throughput;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    public int getOutputTime() {
        return outputTime;
    }

    public void setOutputTime(int outputTime) {
        this.outputTime = outputTime;
    }

    public int getMaxOperationsPerSec() {
        return maxOperationsPerSec;
    }

    public void setMaxOperationsPerSec(int maxOperationsPerSec) {
        this.maxOperationsPerSec = maxOperationsPerSec;
    }


    public int getTimeout() {
        return timeout;
    }

    public Optional<Operation> getNextOperation(long now, int min, int max) {
        long duration = now - lastOperation;
        if(operationsReal.isEmpty())
            return Optional.empty();


        //System.out.println(getDelay());
        if(duration<getDelay(min,max))
            return Optional.empty();

        lastOperation = now;
        return Optional.ofNullable(pickRandomWeightedOperation());

    }
    public int getDelay(int min,int max){
        int d = max-min;
        float operations =  min + throughput.get()*d;
        return (int)(1000/operations);
    }
    public Operation pickRandomWeightedOperation(){

        float rnd = (float) (Math.random() *totalWeight);
        float currentWeight = 0f;
        for(Operation operation : operationsReal){
            currentWeight+=operation.getWeight();
            if(currentWeight>=rnd)
                return operation;
        }

        throw new RuntimeException("Shouldn't happen");
    }
    public String toCSV() {
        StringBuilder sb = new StringBuilder();
        sb.append("#\tExperimentID="+experiment);
        sb.append("\n#\tDate="+date);
        sb.append("\n#\tThreads="+ threads);
        sb.append("\n#\tMinOperations="+minOperationsPerSec);
        sb.append("\n#\tMaxOperations="+ maxOperationsPerSec);
        sb.append("\n#\tOutputTime="+outputTime);
        sb.append("\n");
        return sb.toString();


    }
}
