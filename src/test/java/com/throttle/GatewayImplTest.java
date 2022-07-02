package com.throttle;

import com.throttle.model.RequestProcessor;
import com.throttle.model.Status;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class GatewayImplTest {

    @Test
    public void canConnectAndSubmit() throws InterruptedException {
        CountDownLatch requestProcessorLatch = new CountDownLatch(1);
        RequestProcessor requestProcessor = (client, request, status) -> {
            assertThat(client, equalTo("client"));
            assertThat(request, equalTo("request"));
            assertThat(status, is(Status.OK));
            requestProcessorLatch.countDown();
        };

        int maxRequestsInterval = 2;
        int maxRequestsIntervalSeconds = 1;
        int noRequestsIntervalSeconds = 2;

        GatewayImpl gateway = new GatewayImpl(
                new ConcurrentHashMap<>(),
                requestProcessor,
                maxRequestsInterval,
                maxRequestsIntervalSeconds,
                new ConnectionRequestedCheckScheduler(noRequestsIntervalSeconds));

        gateway.connect("client");

        assertThat(gateway.submit("client", "request"), is(Status.OK));

        assertTrue(requestProcessorLatch.await(2, TimeUnit.SECONDS));
    }

    @Test
    public void receiveErrorIfNotConnected() throws InterruptedException {
        CountDownLatch requestProcessorLatch = new CountDownLatch(1);
        RequestProcessor requestProcessor = (client, request, status) -> {
            assertThat(client, equalTo("client"));
            assertThat(request, equalTo("request"));
            assertThat(status, is(Status.ERROR));
            requestProcessorLatch.countDown();
        };

        int maxRequestsInterval = 2;
        int maxRequestsIntervalSeconds = 1;
        int noRequestsIntervalSeconds = 2;

        GatewayImpl gateway = new GatewayImpl(
                new ConcurrentHashMap<>(),
                requestProcessor,
                maxRequestsInterval,
                maxRequestsIntervalSeconds,
                new ConnectionRequestedCheckScheduler(noRequestsIntervalSeconds));

        assertThat(gateway.submit("client", "request"), is(Status.ERROR));

        assertTrue(requestProcessorLatch.await(2, TimeUnit.SECONDS));
    }

    @Test
    public void receiveThrottledIfTooFast() throws InterruptedException {
        CountDownLatch requestProcessorLatch = new CountDownLatch(1);
        RequestProcessor requestProcessor = (client, request, status) -> {
            assertThat(client, equalTo("client"));
            assertThat(request, equalTo("request"));
            requestProcessorLatch.countDown();
        };

        int maxRequestsInterval = 2;
        int maxRequestsIntervalSeconds = 1;
        int noRequestsIntervalSeconds = 2;

        GatewayImpl gateway = new GatewayImpl(
                new ConcurrentHashMap<>(),
                requestProcessor,
                maxRequestsInterval,
                maxRequestsIntervalSeconds,
                new ConnectionRequestedCheckScheduler(noRequestsIntervalSeconds));

        gateway.connect("client");

        assertThat(gateway.submit("client", "request"), is(Status.OK));
        assertThat(gateway.submit("client", "request"), is(Status.OK));
        assertThat(gateway.submit("client", "request"), is(Status.THROTTLED));

        assertTrue(requestProcessorLatch.await(2, TimeUnit.SECONDS));
    }

    @Test
    public void staleClientIsDisconnected() throws InterruptedException {

        RequestProcessorImpl requestProcessor = spy(new RequestProcessorImpl());

        int maxRequestsInterval = 2;
        int maxRequestsIntervalSeconds = 1;
        int noRequestsIntervalSeconds = 2;
        CountDownLatch clientTimeoutLatch = new CountDownLatch(2);
        ConnectionRequestedCheckScheduler connectionRequestedCheckScheduler = new ConnectionRequestedCheckScheduler(noRequestsIntervalSeconds);

        ConcurrentMap<String, Connection> clientMap = new ConcurrentHashMap<>();
        ClientTimeoutDetector clientTimeoutDetector = new LatchedClientTimeoutDetector(clientMap, clientTimeoutLatch);
        clientTimeoutDetector.startDetector();

        GatewayImpl gateway = new GatewayImpl(
                clientMap,
                requestProcessor,
                maxRequestsInterval,
                maxRequestsIntervalSeconds,
                connectionRequestedCheckScheduler);

        gateway.connect("client");

        assertTrue(clientTimeoutLatch.await(7, TimeUnit.SECONDS));

        verify(requestProcessor, never()).onRequest(any(), any(), any());

        assertThat(gateway.submit("client", "request"), is(Status.ERROR));
    }




}