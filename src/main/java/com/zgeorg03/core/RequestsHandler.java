package com.zgeorg03.core;

import com.zgeorg03.models.Operation;
import com.zgeorg03.models.RealTimeOperationStats;
import com.zgeorg03.utils.GetRequest;
import com.zgeorg03.utils.HttpResponse;
import com.zgeorg03.utils.PostRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
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
public class RequestsHandler implements Runnable{
    private final Logger logger=  LoggerFactory.getLogger(RequestsHandler.class);
    private final ExecutorCompletionService<HttpResponse> requests;
    private final Map<String,RealTimeOperationStats> operationStats=new HashMap<>();

    private int countRealRequests;
    private int lastCountRealRequests;
    private double realRequestsPerSecond;

    private int countFinishedReqPerSecond;
    private int lastCountFinishedReqPerSecond;
    private double finishedRequestsPerSecond;

    private final int timeout;
    public RequestsHandler(Executor executor, int timeout) {
        this.requests = new ExecutorCompletionService<>(executor);
        this.timeout = timeout;
        executor.execute(this);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> onShutDown()));
    }

    private void onShutDown() {
        logger.info("Graceful shut down...");

    }

    public String log(long lastLog,long now,float throughput, int min, int max){
        String date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
                .format(new java.util.Date(System.currentTimeMillis()));
        String configuredRPS = String.format("%.2f%%",(throughput*100));

        String stats = operationStats.values()
                .stream().map(RealTimeOperationStats::toString).collect(Collectors.joining("\n\t","\t",""));

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
    }

    @Override
    public void run() {
        while(true){
            try {
                Future<HttpResponse> future = requests.take();
                countFinishedReqPerSecond++;
                HttpResponse httpResponse = future.get();
                int status = httpResponse.getStatus();

                String id = httpResponse.getId();
                RealTimeOperationStats stats = operationStats.getOrDefault(id,new RealTimeOperationStats(id));
                stats.update(httpResponse.getDuration(), status);
                operationStats.putIfAbsent(id,stats);

            } catch (InterruptedException e) {
                logger.error(e.getLocalizedMessage());
            } catch (ExecutionException e) {
                    RealTimeOperationStats stats = operationStats.getOrDefault("-1", new RealTimeOperationStats("-1"));
                    stats.update(10000, 503);
                    operationStats.putIfAbsent("-1", stats);

            }
        }

    }

    public void execute(Operation operation) throws UnsupportedEncodingException {

        if(operation.getMethod().equalsIgnoreCase("POST")){
            PostRequest postRequest = new PostRequest(operation.getOperationId(),operation.getUrl(),operation.getData(),10000);
            requests.submit(postRequest);
            countRealRequests++;
        }else if(operation.getMethod().equalsIgnoreCase("GET")){
            GetRequest getRequest = new GetRequest(operation.getOperationId(),operation.getUrl(),10000);
            requests.submit(getRequest);
            countRealRequests++;
        }
    }

    public String logCsv(long lastLog, long now, float throughput, int min, int max) {

        double diff = (now-lastLog)/1000d;
        String configRequestsPerSecond = String.format("%.2f",(min+(max-min)*throughput));
        String average_latency = String.format("%.2f",operationStats.values().stream().mapToLong(x->x.getAvgDuration()).average().getAsDouble());
        String min_latency = String.format("%d",operationStats.values().stream().mapToLong(x->x.getMinDuration()).min().getAsLong());
        String max_latency = String.format("%d",operationStats.values().stream().mapToLong(x->x.getMaxDuration()).max().getAsLong());
        String success_err = String.format("%.2f",operationStats.values().stream().mapToLong(x->x.getCountSuccessStatus()).sum()/diff);
        String redir = String.format("%.2f",operationStats.values().stream().mapToLong(x->x.getCountRedirectionStatus()).sum()/diff);
        String clie_err = String.format("%.2f",operationStats.values().stream().mapToLong(x->x.getCountClientErrorStatus()).sum()/diff);
        String serv_err = String.format("%.2f",operationStats.values().stream().mapToLong(x->x.getCountServerErrorStatus()).sum()/diff);

        String throughputPerSecond = String.format("%.2f",operationStats.values().stream().mapToLong(RealTimeOperationStats::getCount).sum()/diff);

        realRequestsPerSecond = ((float)countRealRequests-lastCountRealRequests)/diff;
        String realRequestsPerSecondStr = String.format("%.2f",realRequestsPerSecond);


        return  now + "\t"
                + configRequestsPerSecond +"\t"
                + realRequestsPerSecondStr +"\t"
                + throughputPerSecond +"\t"
                + average_latency +"\t"
                + min_latency +"\t"
                + max_latency +"\t"
                + success_err +"\t"
                + redir +"\t"
                + clie_err +"\t"
                + serv_err +"\t";
    }

    public void updateRealRequests(){
    }
}
