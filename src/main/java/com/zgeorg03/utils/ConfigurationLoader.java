package com.zgeorg03.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zgeorg03.generators.GaussianGenerator;
import com.zgeorg03.generators.Generator;
import com.zgeorg03.generators.MountainGenerator;
import com.zgeorg03.generators.SineGenerator;
import com.zgeorg03.models.Config;
import com.zgeorg03.models.Configuration;
import com.zgeorg03.models.Operation;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by zgeorg03 on 4/14/17.
 */
public class ConfigurationLoader {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationLoader.class);


    public static Configuration load(InputStream stream) throws Exception {

        JsonParser parser = new JsonParser();
        try {
            JsonObject root = parser.parse(new InputStreamReader(stream)).getAsJsonObject();
            JsonObject configObj= root.getAsJsonObject("config");
            JsonArray operations= root.getAsJsonArray("operations");

            List<Operation> operationList = loadOperations(operations);
            Config config = loadConfig(configObj);


            Configuration configuration = new Configuration(operationList, config);
            logger.info("Using configuration in classpath!");
            return configuration ;
        } catch (FileNotFoundException e) {
            logger.error(e.getLocalizedMessage());
            if(logger.isDebugEnabled())
                e.printStackTrace();
            throw new Exception();
        }catch (Exception e){
            e.printStackTrace();
            throw new Exception();
        }

    }
    public static Configuration load(String file) throws Exception {
        JsonParser parser = new JsonParser();
        try {
            JsonObject root = parser.parse(new InputStreamReader(new FileInputStream(file))).getAsJsonObject();
            JsonObject configObj= root.getAsJsonObject("config");
            JsonArray operations= root.getAsJsonArray("operations");

            List<Operation> operationList = loadOperations(operations);
            Config config = loadConfig(configObj);


            Configuration configuration = new Configuration(operationList, config);
            return configuration ;
        } catch (FileNotFoundException e) {
            logger.error(e.getLocalizedMessage());
            logger.info("File not found in path. Trying in classpath");
            return ConfigurationLoader.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(file));
        }catch (Exception e){
            e.printStackTrace();
            throw new Exception();
        }

    }

    private static Config loadConfig(JsonObject config) throws Exception {
        AtomicReference<Float> throughput = new AtomicReference<>(0f);
        Generator generator=null;

        try {
            JsonObject genObj = config.get("generator").getAsJsonObject();
            String type = genObj.get("type").getAsString();
            if(type.equalsIgnoreCase("sine")) {
                int period = genObj.get("period").getAsInt()*1000;
                generator = new SineGenerator(throughput, period);
            } else if(type.equalsIgnoreCase("gaussian")) {
                float deviation = genObj.get("deviation").getAsFloat();
                float mean = genObj.get("mean").getAsFloat();
                generator = new GaussianGenerator(throughput, mean, deviation);
            } else if(type.equalsIgnoreCase("mountain")) {
                long ascent = genObj.get("ascent").getAsInt()*1000L;
                long descent = genObj.get("descent").getAsInt()*1000L;
                long plateau= genObj.get("plateau").getAsInt()*1000L;
                generator = new MountainGenerator(throughput,ascent,descent,plateau);
            }
        }catch(NullPointerException ex){logger.error("Missing generator");}

        int operationId = config.get("maxThreads").getAsInt();
        int minOperations = config.get("minOperations").getAsInt();
        int outputTime = config.get("outputTime").getAsInt()*1000;
        int timeOut = config.get("timeOut").getAsInt()*1000;
        int maxOperations = config.get("maxOperations").getAsInt();
        String experiment = config.get("experiment").getAsString();
        return new Config(generator, experiment, operationId, minOperations, maxOperations,  outputTime, timeOut);
    }

    private static List<Operation> loadOperations(JsonArray operations) throws UnsupportedEncodingException {
        List<Operation> operationList = new LinkedList<>();
        for(int i=0;i<operations.size();i++){
            JsonObject operation = operations.get(i).getAsJsonObject();
            String operationId = operation.get("operationId").getAsString();
            String url = operation.get("url").getAsString();
            int weight = operation.get("weight").getAsInt();
            String method = operation.get("method").getAsString();
            JsonElement dataEl = operation.get("data");
            if(dataEl==null){
                operationList.add(new Operation(operationId, weight, url, method, null));
                continue;
            }

            JsonObject data = dataEl.getAsJsonObject();
            List<NameValuePair> dataList = new LinkedList<>();
            for (Map.Entry<String, JsonElement> entry : data.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue().getAsString();
                dataList.add(new BasicNameValuePair(key, value));
            }
            operationList.add(new Operation(operationId, weight, url, method, dataList));

        }
        return operationList;
    }
}
