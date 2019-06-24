package com.zgeorg03;

import com.zgeorg03.models.PercentileStats;
import com.zgeorg03.models.RealTimeOperationStats;
import com.zgeorg03.utilities.GetRequest;
import com.zgeorg03.utilities.HttpRequest;
import com.zgeorg03.utilities.HttpResponse;
import com.zgeorg03.utilities.PostRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Created by zgeorg03 on 4/13/17.
 */
public class RequestsHandler implements Runnable {
    private final Logger logger=  LoggerFactory.getLogger(RequestsHandler.class);
    private final ExecutorCompletionService<HttpResponse> requests;
    private final Map<String,RealTimeOperationStats> operationStats=new HashMap<>();

    private int countRealRequests;
    private Map<String,Integer> countRealRequestsMap=new HashMap<>();
    private int lastCountRealRequests;
    private Map<String,Integer> countLastRealRequestsMap=new HashMap<>();


    private double realRequestsPerSecond;

    private int countFinishedReqPerSecond;
    private Map<String,Integer> countFinishedRequestsMap=new HashMap<>();
    private int lastCountFinishedReqPerSecond;
    private Map<String,Integer> countLastFinishedRequestsMap=new HashMap<>();

    private double finishedRequestsPerSecond;

    private final int timeout;

    private PercentileStats percentileStats = new PercentileStats();

    private final LogService logService;

    public RequestsHandler(Executor executor, int timeout, LogService logService) {
        this.requests = new ExecutorCompletionService<>(executor);
        this.timeout = timeout;
        this.logService = logService;
        executor.execute(this);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> onShutDown()));
    }

    private void onShutDown() {
        logger.info("Graceful shut down...");

    }

    public String log(long lastLog, long now, float throughput, int min, int max){
        String date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
                .format(new java.util.Date(System.currentTimeMillis()));
        String configuredRPS = String.format("%.2f%%",(throughput*100));

        String stats = operationStats.values()
                .stream().map(RealTimeOperationStats::toString).collect(Collectors.joining("\n\t","\t",""));

        logService.logOperations(lastLog,now,configuredRPS, operationStats,countRealRequestsMap,countLastRealRequestsMap,
                countFinishedRequestsMap,countLastFinishedRequestsMap);


        //TODO this is ok, but more computational expensive
        //float throughputPerSecond = operationStats.values().stream().mapToLong(RealTimeOperationStats::getCount).sum()/((now-lastLog)/1000.0f);

        realRequestsPerSecond = ((float)countRealRequests-lastCountRealRequests)/((now-lastLog)/1000f);
        String realRequestsPerSecondStr = String.format("%.2f",realRequestsPerSecond);

        finishedRequestsPerSecond = ((float)countFinishedReqPerSecond-lastCountFinishedReqPerSecond)/((now-lastLog)/1000f);
        String throughputPerSecond2 = String.format("%.2f",finishedRequestsPerSecond);

        return "["+date+"]"+configuredRPS+"\n"+stats+"\n"
                + "\tConfigRequests/s: "+ String.format("%.2f",(min+ (max-min) * throughput))
                + "\tRealRequests/s: "+ realRequestsPerSecondStr
                + "\tThroughput/s: "+ throughputPerSecond2 +"\n"
                ;

    }

    public void reset(){
        operationStats.clear();
        lastCountRealRequests = countRealRequests;
        lastCountFinishedReqPerSecond=countFinishedReqPerSecond;

        countRealRequestsMap.entrySet().forEach(e->{
            String key = e.getKey();
            int value  = e.getValue();
            countLastRealRequestsMap.put(key,value);
        });

        countFinishedRequestsMap.entrySet().forEach(e->{
            String key = e.getKey();
            int value  = e.getValue();
            countLastFinishedRequestsMap.put(key,value);
        });


    }

    @Override
    public void run() {
        while(true){
            try {
                Future<HttpResponse> future = requests.take();
                HttpResponse httpResponse = future.get();
                countFinishedReqPerSecond++;
                int status = httpResponse.getStatus();
                String id = httpResponse.getId();
                increaseFinishedRequestsCounter(id);
                RealTimeOperationStats stats = operationStats.getOrDefault(id,new RealTimeOperationStats(id));
                stats.update(httpResponse.getDuration(), status);
                //percentileStats.update(id,httpResponse.getDuration());
                operationStats.putIfAbsent(id,stats);

            } catch (InterruptedException e) {
                logger.error(e.getLocalizedMessage());
            } catch (ExecutionException e) {
                RealTimeOperationStats stats = operationStats.getOrDefault("TimeOut", new RealTimeOperationStats("TimeOut"));
                stats.update(timeout, 503);
                operationStats.putIfAbsent("TimeOut", stats);

            }catch (Exception ex){
            }
        }

    }


    public void execute(HttpRequest operation) throws UnsupportedEncodingException, URISyntaxException {

        if(operation instanceof PostRequest ){
            requests.submit((PostRequest)operation);
            countRealRequests++;
            increaseRealRequestsCounter(operation.getId());

        }else if(operation instanceof GetRequest){
            requests.submit((GetRequest)operation);
            countRealRequests++;
            increaseRealRequestsCounter(operation.getId());

        }else{
            logger.info("Unsupported requests");
        }
    }

    private void increaseRealRequestsCounter(String id) {
        int realRequests = countRealRequestsMap.getOrDefault(id,0);
        countRealRequestsMap.put(id,realRequests+1);
    }
    private void increaseFinishedRequestsCounter(String id) {
        int realRequests = countFinishedRequestsMap.getOrDefault(id,0);
        countFinishedRequestsMap.put(id,realRequests+1);
    }

    public String logCsv(long lastLog, long now, float throughput, int min, int max) {
        if(operationStats.isEmpty())
            return "";

        double diff = (now-lastLog)/1000d;
        String configRequestsPerSecond = String.format("%.2f",(min+(max-min)*throughput));
        String min_latency = String.format("%d",operationStats.values().stream().mapToLong(x->x.getMinDuration()).min().getAsLong());
        String average_latency = String.format("%.2f",operationStats.values().stream().mapToLong(x->x.getAvgDuration()).average().getAsDouble());
        String max_latency = String.format("%d",operationStats.values().stream().mapToLong(x->x.getMaxDuration()).max().getAsLong());

        //String median_latency = String.format("%.2f",percentileStats.getM.stream().mapToLong(x->x.getMedian()).average().getAsDouble());
        //String p99 = String.format("%.2f",operationStats.values().stream().mapToLong(x->x.getP99()).average().getAsDouble());
        String success_err = String.format("%.2f",operationStats.values().stream().mapToLong(x->x.getCountSuccessStatus()).sum()/diff);
        String redir = String.format("%.2f",operationStats.values().stream().mapToLong(x->x.getCountRedirectionStatus()).sum()/diff);
        String clie_err = String.format("%.2f",operationStats.values().stream().mapToLong(x->x.getCountClientErrorStatus()).sum()/diff);
        String serv_err = String.format("%.2f",operationStats.values().stream().mapToLong(x->x.getCountServerErrorStatus()).sum()/diff);

        String throughputPerSecond = String.format("%.2f",operationStats.values().stream().mapToLong(RealTimeOperationStats::getCount).sum()/diff);
        System.out.println(throughputPerSecond);


        realRequestsPerSecond = ((float)countRealRequests-lastCountRealRequests)/diff;
        String realRequestsPerSecondStr = String.format("%.2f",realRequestsPerSecond);


        return  String.format("%.4f",((double)now/1000f)) + "\t"
                + configRequestsPerSecond +"\t"
                + realRequestsPerSecondStr +"\t"
                + throughputPerSecond +"\t"
                + min_latency +"\t"
                + average_latency +"\t"
                + max_latency +"\t"
                + success_err +"\t"
                + redir +"\t"
                + clie_err +"\t"
                + serv_err +"\t";
    }

    public void updateRealRequests(){
    }
}
