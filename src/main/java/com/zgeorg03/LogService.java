package com.zgeorg03;

import com.zgeorg03.models.RealTimeOperationStats;
import com.zgeorg03.utilities.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class LogService {
    private static final Logger logger = LoggerFactory.getLogger(LogService.class);
    private File root;
    private Map<String, PrintWriter> pws=new HashMap<>();
    private PrintWriter allCsvPw;


    public LogService(String path, Configuration configuration,String date) throws IOException {
        String exp = date+"_"+configuration.getExperiment();
        root = Paths.get(path,exp).toFile();
        if(!root.exists()){
            if(root.mkdirs())
                logger.info("Created dir: "+ Paths.get(path).toFile().getAbsolutePath());
        }

        allCsvPw = new PrintWriter(new FileWriter(Paths.get(root.getAbsolutePath(),"stats.log").toFile()));
    }

    public void logOperations(long lastLog, long now, String configuredRPS, Map<String, RealTimeOperationStats> operationStats, Map<String, Integer> countRealRequestsMap, Map<String, Integer> countLastRealRequestsMap, Map<String, Integer> countFinishedRequestsMap, Map<String, Integer> countLastFinishedRequestsMap) {
        System.out.println(configuredRPS);

        operationStats.entrySet().forEach(operation->{
            String request = operation.getKey();
            RealTimeOperationStats stats = operation.getValue();
            if(!pws.containsKey(request)) {
                //Check if File exists
                File file = Paths.get(root.getAbsolutePath(), request + ".log").toFile();
                try {
                    PrintWriter pw = pws.getOrDefault(request, new PrintWriter(new FileWriter(file)));
                    pws.putIfAbsent(request, pw);
                    pw.print("#Request:\t" + request);
                    pw.print("\n#time       \trealRPS\tTPS\tmin_lt\tavg_lt\tmax_lt\tsucc\tredir\tcl_err\tse_err\n");
                    pw.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                PrintWriter pw = pws.get(request);

                double diff = (now-lastLog)/1000d;
                //String configRequestsPerSecond = String.format("%.2f",(min+(max-min)*throughput));
                String min_latency = String.format("%d",stats.getMinDuration());
                String average_latency = String.format("%d",stats.getAvgDuration());
                String max_latency = String.format("%d",stats.getMaxDuration());

                //String median_latency = String.format("%.2f",percentileStats.getM.stream().mapToLong(x->x.getMedian()).average().getAsDouble());
                //String p99 = String.format("%.2f",operationStats.values().stream().mapToLong(x->x.getP99()).average().getAsDouble());
                String success_err = String.format("%d",stats.getCountSuccessStatus());
                String redir = String.format("%d",stats.getCountRedirectionStatus());
                String clie_err = String.format("%d",stats.getCountClientErrorStatus());
                String serv_err = String.format("%d",stats.getCountServerErrorStatus());


                double realRequestsPerSecond = ((float) countRealRequestsMap.getOrDefault(request,0)-
                        countLastRealRequestsMap.getOrDefault(request,0))/diff;
                String realRequestsPerSecondStr = String.format("%.2f",realRequestsPerSecond);

                double throughput = ((float) countFinishedRequestsMap.getOrDefault(request,0)-
                        countLastFinishedRequestsMap.getOrDefault(request,0))/diff;
                String throughputStr = String.format("%.2f",throughput);

                String res =  String.format("%.4f",((double)now/1000f)) + "\t"
                        + realRequestsPerSecondStr +"\t"
                        + throughputStr +"\t"
                        + min_latency +"\t"
                        + average_latency +"\t"
                        + max_latency +"\t"
                        + success_err +"\t"
                        + redir +"\t"
                        + clie_err +"\t"
                        + serv_err;
                pw.println(res);
                pw.flush();
            }


        });

    }

    public PrintWriter getAllCsvPw() {
        return allCsvPw;
    }
}
