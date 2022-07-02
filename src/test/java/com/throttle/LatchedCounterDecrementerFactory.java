package com.throttle;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class LatchedCounterDecrementerFactory extends CounterDecrementerFactory {

    private final CountDownLatch latch;

    public LatchedCounterDecrementerFactory(CountDownLatch latch){
        this.latch = latch;
    }

    @Override
    public CounterDecrementer getNewCounterDecrementer(AtomicInteger counter){
        return new LatchedCounterDecrementer(counter, this.latch);
    }
}
