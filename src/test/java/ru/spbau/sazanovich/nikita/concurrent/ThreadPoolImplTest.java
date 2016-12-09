package ru.spbau.sazanovich.nikita.concurrent;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ThreadPoolImplTest {

    private static final long ONES_TO_COUNT = 4_000_000_000L;

    @Test
    public void testThreadPoolCountingInOneThread() throws Exception {
        testThreadPoolCountingInNThreads(1);
    }

    @Test
    public void testThreadPoolCountingInTwoThread() throws Exception {
        testThreadPoolCountingInNThreads(2);
    }

    @Test
    public void testThreadPoolCountingInFourThread() throws Exception {
        testThreadPoolCountingInNThreads(4);
    }

    private void testThreadPoolCountingInNThreads(int threadsNumber) throws Exception {
        final ThreadPoolImpl pool = new ThreadPoolImpl(threadsNumber);
        final long threadShare = ONES_TO_COUNT / threadsNumber;
        final List<LightFuture<Long>> futures = new ArrayList<>();
        for (int part = 0; part < threadsNumber; part++) {
            final int currentPart = part;
            futures.add(pool.submit(() -> {
                long sum = 0;
                for (long i = currentPart * threadShare + 1;
                     i <= (currentPart + 1) * threadShare;
                     i++) {
                    sum++;
                }
                return sum;
            }));
        }
        long actualResult = 0L;
        for (LightFuture<Long> future : futures) {
            actualResult += future.get();
        }
        assertEquals(ONES_TO_COUNT, actualResult);
    }
}
