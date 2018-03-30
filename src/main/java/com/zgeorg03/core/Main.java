package com.zgeorg03.core;


import com.zgeorg03.models.Configuration;
import com.zgeorg03.utils.ConfigurationLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zgeorg03 on 4/13/17.
 */

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String args[]) throws UnsupportedEncodingException {

        String configurationFile = "configuration.json";
        if(args.length>=1)
            configurationFile = args[0];

        String path="./";
        if(args.length==2)
            path = args[1];

        logger.info("Configuration file:"+configurationFile);
        
        if(!Paths.get(path).toFile().exists()){
            if(Paths.get(path).toFile().mkdir())
                logger.info("Created dir: "+ Paths.get(path).toFile().getAbsolutePath());
        }

        String dateFull = new SimpleDateFormat("yyyy-MM-dd_hhmm") .format(new java.util.Date(System.currentTimeMillis()));
        String date = dateFull.substring(0,dateFull.length()-5);



        /**
        BasicCookieStore cookieStore = new BasicCookieStore();
        BasicClientCookie cookie = new BasicClientCookie("connect.sid", "s%3A1RDgG0iUAjQ5iWSAjRcDk6v-EHbcidVD.tBfy4%2BCsfRBgbtpD4WCXCkdHiaiAwLs4OQLFfh57hak");
        cookie.setDomain("mandola.grid.ucy.ac.cy");
        cookie.setPath("/");
        cookieStore.addCookie(cookie);
         **/
        try {

            Configuration configuration = ConfigurationLoader.load(configurationFile);
            configuration.setDate(date);

            String experiment = configuration.getExperimentName();

            PrintWriter printWriter = new PrintWriter(new FileWriter(Paths.get(path,date+"_"+experiment+".log").toFile()));

            ExecutorService executorService = Executors.newFixedThreadPool(configuration.getMaxThreads());

            RequestsHandler requestsHandler = new RequestsHandler(executorService);

            OperationsHandler operationsHandler = new OperationsHandler(configuration, requestsHandler, printWriter);

            operationsHandler.run();
        } catch (Exception e) {
            logger.error("Configuration file seems to be invalid.Exiting...");
            System.exit(1);
        }



    }
}
