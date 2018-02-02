package com.zgeorg03.core;

import com.zgeorg03.generators.Generator;
import com.zgeorg03.models.Configuration;
import com.zgeorg03.models.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Created by zgeorg03 on 4/13/17.
 */
public class OperationsHandler{
    private final Logger logger=  LoggerFactory.getLogger(OperationsHandler.class);
    private final Configuration configuration;
    private final RequestsHandler requestsHandler;
    private final Generator generator;
    private long timeStarted;
    private final int minDelay;
    private final PrintWriter printWriter;

    public OperationsHandler(Configuration configuration, RequestsHandler requestsHandler, PrintWriter printWriter){
        this.configuration = configuration;
        this.requestsHandler = requestsHandler;


        this.minDelay = 1000/configuration.getMaxOperations();

        this.generator = configuration.getGenerator();
        this.printWriter = printWriter;
    }

    public void run(){
        int sleep = (minDelay>10)?minDelay-10:minDelay;
        timeStarted = System.currentTimeMillis();
        long lastLog=timeStarted;
        logger.info("Starting generator...");
        printWriter.print(configuration.toCSV());
        printWriter.print("#time       \tConfRPS\trealRPS\tTPS\tavg_lt\tmin_lt\t max_lt\tsucc\tredir\tcl_err\tse_err\n");

        int max = configuration.getMaxOperations();
        int min = configuration.getMinOperations();

        while(true){

            try {
                long now = System.currentTimeMillis();

                //Execute the next operation
                Optional<Operation> operation  = configuration.getNextOperation(now);
                if(operation.isPresent())
                    requestsHandler.execute(operation.get());

                if(now-lastLog>configuration.getOutputTime()){
                    try{
                        System.out.println(requestsHandler.log(lastLog,now,generator.getThroughput().get(),min,max));
                        printWriter.print(requestsHandler.logCsv(lastLog,now,generator.getThroughput().get(),min, max));
                        printWriter.print("\n");
                        printWriter.flush();
                    }catch(NoSuchElementException ex){
                        logger.error("No values to write");
                    }finally{
                        lastLog =  now;
                        requestsHandler.reset();
                    }
                }

                generator.run(now);

                //TimeUnit.MILLISECONDS.sleep(sleep);
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getLocalizedMessage());
                if(logger.isDebugEnabled())
                    e.printStackTrace();
            }catch (Exception ex){
                ex.printStackTrace();
            }


        }
    }

}
