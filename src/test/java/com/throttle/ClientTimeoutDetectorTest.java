package com.throttle;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class ClientTimeoutDetectorTest {

    @Test
    public void staleClientIsDisconnected() throws InterruptedException {
        Connection client = spy(new Connection.Builder()
                .client("client_x")
                .build());

        CountDownLatch clientTimeoutLatch = new CountDownLatch(1);

        ConcurrentMap<String, Connection> clientMap = new ConcurrentHashMap<>();
        ClientTimeoutDetector clientTimeoutDetector = new LatchedClientTimeoutDetector(clientMap, clientTimeoutLatch);
        clientTimeoutDetector.startDetector();

        clientMap.put("client_x", client);

        assertTrue(clientTimeoutLatch.await(7, TimeUnit.SECONDS));

        verify(client).disconnect();

        assertTrue(clientMap.isEmpty());
    }
}
