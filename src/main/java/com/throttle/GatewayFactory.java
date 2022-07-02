package com.throttle;

import com.throttle.model.Gateway;
import com.throttle.model.RequestProcessor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class GatewayFactory {

    private final int maxRequestsPerInterval;
    private final int maxRequestsIntervalSeconds;
    private final int noRequestsIntervalSeconds;

    public GatewayFactory(int maxRequestsPerInterval, int maxRequestsIntervalSeconds, int noRequestsIntervalSeconds) {
        this.maxRequestsPerInterval = maxRequestsPerInterval;
        this.maxRequestsIntervalSeconds = maxRequestsIntervalSeconds;
        this.noRequestsIntervalSeconds = noRequestsIntervalSeconds;
    }

    public Gateway getGateway(){
        RequestProcessor requestProcessor = new RequestProcessorImpl();
        ConcurrentMap<String, Connection> clientMap = new ConcurrentHashMap<>();

        ClientTimeoutDetector clientTimeoutDetector = new ClientTimeoutDetector(clientMap);
        clientTimeoutDetector.startDetector();

        ConnectionRequestedCheckScheduler connectionRequestedCheckScheduler = new ConnectionRequestedCheckScheduler(noRequestsIntervalSeconds);

        return new GatewayImpl(clientMap, requestProcessor, maxRequestsPerInterval, maxRequestsIntervalSeconds, connectionRequestedCheckScheduler);
    }
}
