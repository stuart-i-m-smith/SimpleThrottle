package com.throttle;

import com.throttle.model.Gateway;
import com.throttle.model.RequestProcessor;
import com.throttle.model.Status;

import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

public class GatewayImpl implements Gateway {

    private final ConcurrentMap<String, Connection> clientMap;
    private final RequestProcessor requestProcessor;
    private final int maxRequestsPerInterval;
    private final int maxRequestsIntervalSeconds;
    private final ConnectionRequestedCheckScheduler connectionRequestedCheckScheduler;

    public  GatewayImpl(
            ConcurrentMap<String, Connection> clientMap,
            RequestProcessor requestProcessor,
            int maxRequestsPerInterval,
            int maxRequestsIntervalSeconds,
            ConnectionRequestedCheckScheduler connectionRequestedCheckScheduler){
        this.clientMap = clientMap;
        this.requestProcessor = requestProcessor;
        this.maxRequestsPerInterval = maxRequestsPerInterval;
        this.maxRequestsIntervalSeconds = maxRequestsIntervalSeconds;
        this.connectionRequestedCheckScheduler = connectionRequestedCheckScheduler;
    }

    @Override
    public void connect(String client) {
        Connection connection = new Connection.Builder()
                .client(client)
                .maxRequestsPerInterval(maxRequestsPerInterval)
                .maxRequestsIntervalSeconds(maxRequestsIntervalSeconds)
                .connectionRequestedCheckScheduler(connectionRequestedCheckScheduler)
                .build();

        Connection existingConnection = clientMap.putIfAbsent(client, connection);
        Objects.requireNonNullElse(existingConnection, connection).scheduleClientRequestedCheck();
    }

    @Override
    public Status submit(String client, Object request) {

        Status status;
        if(clientMap.containsKey(client)){
            status = clientMap.get(client).tryRequest();
        }else {
            status = Status.ERROR;
        }

        requestProcessor.onRequest(client, request, status);

        return status;
    }

    @Override
    public void disconnect(String client) {
        Connection connection = clientMap.remove(client);

        if(connection != null){
            connection.disconnect();
        }
    }
}
