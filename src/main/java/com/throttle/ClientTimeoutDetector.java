package com.throttle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.time.Instant;
import java.util.Iterator;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ClientTimeoutDetector {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ConcurrentMap<String, Connection> clientMap;

    public ClientTimeoutDetector(ConcurrentMap<String, Connection> clientMap){
        this.clientMap = clientMap;
    }

    public void startDetector(){
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                this::detectTimeouts, 3, 3, TimeUnit.SECONDS);
    }

    void detectTimeouts(){

        Instant staleTime = Instant.now().minusSeconds(3);

        Iterator<String> clientIterator = clientMap.keySet().iterator();
        while(clientIterator.hasNext()){
            String clientKey = clientIterator.next();
            Connection connection = clientMap.get(clientKey);
            Instant lastRequestTime = connection.getLastRequestTime();

            if(lastRequestTime.isBefore(staleTime)){
                LOGGER.info("Client <{}> last interaction was <{}>, disconnecting.", clientKey, lastRequestTime);
                connection.disconnect();
                clientIterator.remove();
            }
        }
    }
}
