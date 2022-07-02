package com.throttle;

import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class LatchedConnectionRequestedCheckScheduler extends ConnectionRequestedCheckScheduler {

    private final CountDownLatch clientRequestedCheckLatch;

    public LatchedConnectionRequestedCheckScheduler(int noRequestsIntervalSeconds, CountDownLatch clientRequestedCheckLatch) {
        super(noRequestsIntervalSeconds);
        this.clientRequestedCheckLatch = clientRequestedCheckLatch;
    }

    @Override
    void clientRequestedCheck(String client, AtomicReference<Instant> lastRequestTime){
        super.clientRequestedCheck(client, lastRequestTime);
        clientRequestedCheckLatch.countDown();
    }
}
