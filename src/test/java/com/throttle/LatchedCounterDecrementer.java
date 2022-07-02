package com.throttle;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class LatchedCounterDecrementer extends CounterDecrementer {
    private final CountDownLatch latch;

    public LatchedCounterDecrementer(AtomicInteger counter, CountDownLatch latch) {
        super(counter);
        this.latch = latch;
    }

    @Override
    public void run(){
        super.run();
        latch.countDown();
    }
}