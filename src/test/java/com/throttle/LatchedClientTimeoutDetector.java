package com.throttle;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;

public class LatchedClientTimeoutDetector extends ClientTimeoutDetector {

    private final CountDownLatch clientTimeoutLatch;

    public LatchedClientTimeoutDetector(ConcurrentMap<String, Connection> clientMap, CountDownLatch clientTimeoutLatch) {
        super(clientMap);
        this.clientTimeoutLatch = clientTimeoutLatch;
    }

    @Override
    void detectTimeouts(){
        super.detectTimeouts();
        clientTimeoutLatch.countDown();
    }
}