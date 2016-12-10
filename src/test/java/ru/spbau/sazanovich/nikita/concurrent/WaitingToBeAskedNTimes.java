package ru.spbau.sazanovich.nikita.concurrent;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class WaitingToBeAskedNTimes implements Supplier<Integer> {

    private final AtomicInteger timesAsked = new AtomicInteger();
    private final int integerToPass;

    public WaitingToBeAskedNTimes(int integerToPass) {
        this.integerToPass = integerToPass;
    }

    @Override
    public synchronized Integer get() {
        int current = timesAsked.addAndGet(1);
        if (current == integerToPass) {
            notifyAll();
        } else {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return timesAsked.get();
    }
}
