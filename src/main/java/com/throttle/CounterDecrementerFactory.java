package com.throttle;

import java.util.concurrent.atomic.AtomicInteger;

public class CounterDecrementerFactory {
    public CounterDecrementer getNewCounterDecrementer(AtomicInteger counter){
        return new CounterDecrementer(counter);
    }
}
