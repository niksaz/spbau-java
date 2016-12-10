package ru.spbau.sazanovich.nikita.concurrent;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.lang.Thread.sleep;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    public void testThreadPoolCountingInTwoThreadsEightParts() throws Exception {
        testThreadPoolCountingInNThreads(2, 8);
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
        assertThatThrownBy(futureCancelled::get).isInstanceOf(LightCancellationException.class);
        final LightFuture<Long> deniedFuture = pool.submit(COUNTER.apply(ONES_TO_COUNT));
        assertNull(deniedFuture);
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

    @Test(expected = LightExecutionException.class)
    public void testThreadPoolThrowingLightExecutionException() throws Exception {
        final ThreadPoolImpl pool = new ThreadPoolImpl(1);
        LightFuture<Object> failedFuture = pool.submit(() -> {
            throw new IllegalStateException();
        });
        failedFuture.get();
    }

    @Test
    public void testLightFutureThenApply() throws Exception {
        final ThreadPoolImpl pool = new ThreadPoolImpl(1);
        final Supplier<Long> threadCounter = COUNTER.apply(ONES_TO_COUNT);
        final LightFuture<Long> future = pool.submit(threadCounter);
        final LightFuture<Long> alphaFuture = future.thenApply(n -> n / 10);
        final LightFuture<Long> betaFuture = future.thenApply(n -> n / 20);
        final LightFuture<Long> gammaFuture = alphaFuture.thenApply(n -> n / 10);

        assertEquals(Long.valueOf(ONES_TO_COUNT / 100), gammaFuture.get());
        assertEquals(Long.valueOf(ONES_TO_COUNT / 20), betaFuture.get());
        assertEquals(Long.valueOf(ONES_TO_COUNT / 10), alphaFuture.get());
        assertEquals(Long.valueOf(ONES_TO_COUNT), future.get());
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
