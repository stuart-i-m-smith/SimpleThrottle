package com.throttle;

import com.throttle.model.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Connection {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final AtomicInteger currentRequestsIntervalCount = new AtomicInteger();
    private final AtomicReference<Instant> lastRequestTime = new AtomicReference<>(Instant.now());
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    private final String client;
    private final int maxRequestsPerInterval;
    private final int maxRequestsIntervalSeconds;
    private final ConnectionRequestedCheckScheduler connectionRequestedCheckScheduler;
    private final CounterDecrementer counterDecrementer;

    private Connection(String client, int maxRequestsPerInterval, int maxRequestsIntervalSeconds, ConnectionRequestedCheckScheduler connectionRequestedCheckScheduler, CounterDecrementerFactory counterDecrementerFactory) {
        this.client = client;
        this.maxRequestsPerInterval = maxRequestsPerInterval;
        this.maxRequestsIntervalSeconds = maxRequestsIntervalSeconds;
        this.connectionRequestedCheckScheduler = connectionRequestedCheckScheduler;
        this.counterDecrementer = counterDecrementerFactory.getNewCounterDecrementer(currentRequestsIntervalCount);
    }

    public void scheduleClientRequestedCheck(){
        this.connectionRequestedCheckScheduler.scheduleClientRequestedCheck(client, lastRequestTime);
    }

    public Status tryRequest(){
        LOGGER.error("Trying request for client <{}>.", client);

        lastRequestTime.set(Instant.now());
        scheduleClientRequestedCheck();

        int currentCount = currentRequestsIntervalCount.getAndUpdate(current -> {
            if (current >= maxRequestsPerInterval) {
                return current;
            }

            return ++current;
        });

        boolean isThrottled = currentCount >= maxRequestsPerInterval;

        if (isThrottled) {
            return Status.THROTTLED;
        }

        scheduledExecutorService.schedule(
            counterDecrementer,
            maxRequestsIntervalSeconds,
            TimeUnit.SECONDS);

        return Status.OK;
    }

    public Instant getLastRequestTime(){
        return this.lastRequestTime.get();
    }

    public static final class Builder {
        private String client;
        private int maxRequestsPerInterval;
        private int maxRequestsIntervalSeconds;
        private ConnectionRequestedCheckScheduler connectionRequestedCheckScheduler;
        private CounterDecrementerFactory counterDecrementerFactory = new CounterDecrementerFactory();

        public Builder client(String client) {
            this.client = client;
            return this;
        }

        public Builder maxRequestsPerInterval(int maxRequestsInterval) {
            this.maxRequestsPerInterval = maxRequestsInterval;
            return this;
        }

        public Builder maxRequestsIntervalSeconds(int maxRequestsIntervalSeconds) {
            this.maxRequestsIntervalSeconds = maxRequestsIntervalSeconds;
            return this;
        }

        public Builder connectionRequestedCheckScheduler(ConnectionRequestedCheckScheduler connectionRequestedCheckScheduler) {
            this.connectionRequestedCheckScheduler = connectionRequestedCheckScheduler;
            return this;
        }

        public Builder counterDecrementerFactory(CounterDecrementerFactory counterDecrementerFactory) {
            this.counterDecrementerFactory = counterDecrementerFactory;
            return this;
        }

        public Connection build() {
            return new Connection(client, maxRequestsPerInterval, maxRequestsIntervalSeconds, connectionRequestedCheckScheduler, counterDecrementerFactory);
        }
    }

    public void disconnect(){
        LOGGER.error("Disconnecting client <{}>", client);
        scheduledExecutorService.shutdownNow();
        lastRequestTime.set(Instant.MIN);
    }
}
