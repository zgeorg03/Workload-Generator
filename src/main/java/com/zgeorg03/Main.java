package com.zgeorg03;

import com.zgeorg03.utilities.Configuration;
import com.zgeorg03.utilities.ConfigurationLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String args[]) throws Exception {
        logger.info("Started Workload Generator");

        String path = "./config.yml";


        File parent = Paths.get(path).toFile().getParentFile();

        if(!parent.exists()){
            if(parent.mkdirs())
                logger.info("Created dir: "+ Paths.get(path).toFile().getAbsolutePath());
        }


        Configuration configuration = ConfigurationLoader.load(path);

        logger.info(configuration.toString());



        String dateFull = new SimpleDateFormat("yyyy-MM-dd_hhmm") .format(new java.util.Date(System.currentTimeMillis()));
        String date = dateFull.substring(0,dateFull.length()-5);
        configuration.setDate(date);



        final LogService logService = new LogService(parent.getAbsolutePath(), configuration,date);
        PrintWriter allCsvPw = logService.getAllCsvPw();

        ExecutorService executorService = Executors.newFixedThreadPool(configuration.getThreads());

        RequestsHandler requestsHandler = new RequestsHandler(executorService, configuration.getTimeout(), logService);

        OperationsHandler operationsHandler = new OperationsHandler(configuration, requestsHandler, allCsvPw);

        operationsHandler.run();
    }
}
