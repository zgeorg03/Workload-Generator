package com.zgeorg03;

import com.zgeorg03.generators.AbstractGenerator;
import com.zgeorg03.models.KafkaOperation;
import com.zgeorg03.models.Operation;
import com.zgeorg03.utilities.Configuration;
import com.zgeorg03.utilities.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

public class OperationsHandler {

    private static final Logger logger=  LoggerFactory.getLogger(OperationsHandler.class);

    private final Configuration configuration;
    private final RequestsHandler requestsHandler;

    private long timeStarted; //Time starts
    private int minDelay;

    private final PrintWriter printWriter;
    private final Map<String,AbstractGenerator> generators;
    private final long totalDurationOfGeneratorsSequence;

    //TODO Remember to swap
    private AbstractGenerator generator;



    public OperationsHandler(Configuration configuration, RequestsHandler requestsHandler, PrintWriter printWriter) throws Exception {
        this.configuration = configuration;
        this.minDelay = 1000/configuration.getMaxOperationsPerSec();
        this.requestsHandler = requestsHandler;
        this.generators = configuration.getActualGenerators();
        this.printWriter = printWriter;

        this.totalDurationOfGeneratorsSequence = configuration.getSequence().stream()
                .mapToLong(e->{

                    AbstractGenerator abstractGenerator = this.generators.get(e);
                    return abstractGenerator.getDuration();

                })
                .sum();

        if(generators==null){
            throw new Exception("At least one generator is required");
        }
        this.generator = generators.values().stream().findFirst().get();
    }
    public void run() {
        int sleep = (minDelay > 10) ? minDelay - 10 : minDelay;
        timeStarted = System.currentTimeMillis();
        long lastLog = timeStarted;
        logger.info("Starting generator...");
        printWriter.print(configuration.toCSV());
        printWriter.print("#time       \tConfRPS\trealRPS\tTPS\tmin_lt\tavg_lt\tmax_lt\tsucc\tredir\tcl_err\tse_err\n");

        while(true){


            Integer max =  generator.getMaxOperationsPerSec();
            if(max == null)
                max = configuration.getMaxOperationsPerSec();

            Integer min =  generator.getMinOperationsPerSec();
            if(min == null)
                min = configuration.getMinOperationsPerSec();

            try {

                long now = System.currentTimeMillis();

                //Execute the next operation
                Optional<Operation> operation = configuration.getNextOperation(now,min,max);

                if (operation.isPresent()) {
                    Operation op = operation.get();
                    if (op instanceof KafkaOperation) {
                        System.out.println("Kafka Operation executing");
                    }else if(op instanceof HttpRequest){
                        requestsHandler.execute((HttpRequest) op);
                    }

                }

                if (now - lastLog > configuration.getOutputTime()) {
                    try {
                        System.out.println(requestsHandler.log(lastLog, now, generator.getThroughput().get(), min, max));
                        printWriter.print(requestsHandler.logCsv(lastLog, now, generator.getThroughput().get(), min, max));
                        printWriter.print("\n");
                        printWriter.flush();

                        //TODO Decide which generator to choose
                        long duration = now-timeStarted;
                        decideWhichGeneratorToUse(duration);

                    } catch (NoSuchElementException ex) {
                        ex.printStackTrace();
                        logger.error("No values to write");
                    } finally {
                        lastLog = now;
                        requestsHandler.reset();
                    }
                }

                generator.run(now);

                //TimeUnit.MILLISECONDS.sleep(sleep);
            } catch ( UnsupportedEncodingException e) {
                logger.error(e.getLocalizedMessage());
                if (logger.isDebugEnabled())
                    e.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


    private void decideWhichGeneratorToUse(long time) {
        long current = time % totalDurationOfGeneratorsSequence;
        long sum=0;
        for(String w:configuration.getSequence()){
            AbstractGenerator generator = generators.get(w);
            sum+=generator.getDuration();
            if(current<=sum) {
                if(this.generator == generator)
                    break;
                this.generator = generator;
                this.generator.reset();
                break;
            }

        }
        System.out.println("USING:"+generator.getName());

    }
}
