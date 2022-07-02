package com.throttle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class ConnectionRequestedCheckScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private final int noRequestsIntervalSeconds;

    public ConnectionRequestedCheckScheduler(int noRequestsIntervalSeconds) {
        this.noRequestsIntervalSeconds = noRequestsIntervalSeconds;
    }

    public void scheduleClientRequestedCheck(String client, AtomicReference<Instant> lastRequestTime){
        this.scheduledExecutorService.schedule(() -> clientRequestedCheck(client, lastRequestTime),
                noRequestsIntervalSeconds,
                TimeUnit.SECONDS);
    }

    void clientRequestedCheck(String client, AtomicReference<Instant> lastRequestTime){
        if(lastRequestTime.get() != Instant.MIN && lastRequestTime.get().isBefore(Instant.now().minusSeconds(noRequestsIntervalSeconds))) {
            LOGGER.error("Client <{}> last request time <{}> is outside minimum request frequency <{}>s.", client, lastRequestTime, noRequestsIntervalSeconds);
            scheduleClientRequestedCheck(client, lastRequestTime);
        }
    }
}