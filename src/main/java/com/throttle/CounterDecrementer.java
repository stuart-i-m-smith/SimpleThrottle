package com.throttle;

import java.util.concurrent.atomic.AtomicInteger;

public class CounterDecrementer implements Runnable{

    private final AtomicInteger counter;

    public CounterDecrementer(AtomicInteger counter){
        this.counter = counter;
    }

    @Override
    public void run() {
        counter.decrementAndGet();
    }
}