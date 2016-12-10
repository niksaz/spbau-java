package ru.spbau.sazanovich.nikita.concurrent;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.lang.Thread.sleep;
import static org.junit.Assert.*;

public class ThreadPoolImplTest {

    private static final long ONES_TO_COUNT = 4_000_000_000L;
    private static final Function<Long, Supplier<Long>> COUNTER =
            n -> () -> {
                long sum = 0;
                for (long i = 1; i <= n; i++) {
                    sum++;
                }
                return sum;
            };

    @Test
    public void testThreadPoolCountingInOneThread() throws Exception {
        testThreadPoolCountingInNThreads(1, 1);
    }

    @Test
    public void testThreadPoolCountingInTwoThread() throws Exception {
        testThreadPoolCountingInNThreads(2, 2);
    }

    @Test
    public void testThreadPoolCountingInFourThread() throws Exception {
        testThreadPoolCountingInNThreads(4, 4);
    }

    @Test
    public void testThreadPoolShutdown() throws Exception {
        final ThreadPoolImpl pool = new ThreadPoolImpl(1);
        final Supplier<Long> threadCounter = COUNTER.apply(ONES_TO_COUNT);
        final LightFuture<Long> future = pool.submit(threadCounter);
        final LightFuture<Long> futureCancelled = pool.submit(threadCounter);
        // waiting ThreadPool to take over the first task
        sleep(100);
        pool.shutdown();
        assertEquals(Long.valueOf(ONES_TO_COUNT), future.get());
        try {
            futureCancelled.get();
        } catch (LightCancellationException expected) {
        } catch (Exception e) {
            fail();
        }
        final LightFuture<Long> deniedFuture = pool.submit(COUNTER.apply(ONES_TO_COUNT));
        assertNull(deniedFuture);
    }

    @Test
    public void testLightFutureThenApply() throws Exception {
        final ThreadPoolImpl pool = new ThreadPoolImpl(1);
        final Supplier<Long> threadCounter = COUNTER.apply(ONES_TO_COUNT);
        final LightFuture<Long> future = pool.submit(threadCounter);
        final LightFuture<Long> transformedFuture = future.thenApply(aLong -> aLong / 10);
        assertEquals(Long.valueOf(ONES_TO_COUNT / 10), transformedFuture.get());
    }

    @Test
    public void testThreadPoolForActuallyCreatingNThreads() throws Exception {
        final int threadsNumber = 16;
        final ThreadPoolImpl pool = new ThreadPoolImpl(threadsNumber);
        final WaitingToBeAskedNTimes waiter = new WaitingToBeAskedNTimes(threadsNumber);
        final List<LightFuture<Integer>> futures = new ArrayList<>();
        for (int i = 0; i < threadsNumber; i++) {
            futures.add(pool.submit(waiter));
        }
        for (int i = 0; i < threadsNumber; i++) {
            assertEquals(Integer.valueOf(threadsNumber), futures.get(i).get());
        }
    }

    private void testThreadPoolCountingInNThreads(int threadsNumber, int parts)
            throws Exception {
        final ThreadPoolImpl pool = new ThreadPoolImpl(threadsNumber);
        final long threadShare = ONES_TO_COUNT / parts;
        final Supplier<Long> threadCounter = COUNTER.apply(threadShare);
        final List<LightFuture<Long>> futures = new ArrayList<>();
        for (int i = 0; i < parts; i++) {
            futures.add(pool.submit(threadCounter));
        }
        long actualResult = 0L;
        for (LightFuture<Long> future : futures) {
            actualResult += future.get();
        }
        assertEquals(ONES_TO_COUNT, actualResult);
    }
}
