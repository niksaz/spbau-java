package ru.spbau.sazanovich.nikita;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class LazyFactoryTest {

    private static final Supplier<Long> SUMMER_OF_NUMBERS = () -> {
        long sum = 0;
        for (int i = 0; i <= 1000000000; i++) {
            sum += i;
        }
        return sum;
    };
    private static final Supplier<Object> NULLER = () -> null;
    private static final int NUMBER_OF_THREADS = 5;

    /**
     * Tests basic properties: correctness of the result, returning of the same object each time,
     * calling exactly once from a single threaded environment.
     */

    @Test
    public void testSingleThreadedLazySimpleCase() {
        testOnSimpleCase(
                LazyFactory.createSingleThreadedLazy(new LimitedSupplier<>(SUMMER_OF_NUMBERS, 1)),
                SUMMER_OF_NUMBERS);
    }

    @Test
    public void testMultiThreadedLazySimpleCase() {
        testOnSimpleCase(
                LazyFactory.createMultiThreadedLazy(new LimitedSupplier<>(SUMMER_OF_NUMBERS, 1)),
                SUMMER_OF_NUMBERS);
    }

    @Test
    public void testLockFreeLazySimpleCase() {
        testOnSimpleCase(
                LazyFactory.createLockFreeLazy(new LimitedSupplier<>(SUMMER_OF_NUMBERS, 1)),
                SUMMER_OF_NUMBERS);
    }

    /**
     * Tests whether a computation is starting before calling get().
     */

    @Test
    public void testSingleThreadedLazyBottom() {
        LazyFactory.createSingleThreadedLazy(new LimitedSupplier<>(SUMMER_OF_NUMBERS, 0));
    }

    @Test
    public void testMultiThreadedLazyBottom() {
        LazyFactory.createMultiThreadedLazy(new LimitedSupplier<>(SUMMER_OF_NUMBERS, 0));
    }

    @Test
    public void testLockFreeLazyBottom() {
        LazyFactory.createLockFreeLazy(new LimitedSupplier<>(SUMMER_OF_NUMBERS, 0));
    }

    /**
     * Tests whether null suppliers are handled correctly.
     */

    @Test
    public void testSingleThreadedLazyNull() {
        testOnNull(LazyFactory.createSingleThreadedLazy(new LimitedSupplier<>(NULLER, 1)));
    }

    @Test
    public void testMultiThreadedLazyNull() {
        testOnNull(LazyFactory.createMultiThreadedLazy(new LimitedSupplier<>(NULLER, 1)));
    }

    @Test
    public void testLockFreeLazyNull() {
        testOnNull(LazyFactory.createLockFreeLazy(new LimitedSupplier<>(NULLER, 1)));
    }

    /**
     * Multithreaded tests.
     */

    @Test
    public void testMultiThreadedLazyMultiThreaded() throws InterruptedException {
        testInMultithreadedEnvironment(
                LazyFactory.createMultiThreadedLazy(new LimitedSupplier<>(SUMMER_OF_NUMBERS, 1)),
                SUMMER_OF_NUMBERS);
    }

    @Test
    public void testLockFreeLazyMultiThreaded() throws InterruptedException {
        // not limited usage because lock-free Lazy implementation does not guarantee
        // calling supplier's get() method exactly once
        testInMultithreadedEnvironment(
                LazyFactory.createLockFreeLazy(SUMMER_OF_NUMBERS),
                SUMMER_OF_NUMBERS);
    }

    private <T> void testOnSimpleCase(Lazy<T> lazy, Supplier<T> underlyingSupplier) {
        final T result = lazy.get();
        assertEquals(underlyingSupplier.get(), result);
        final T anotherResult = lazy.get();
        assertSame(result, anotherResult);
    }

    private <T> void testOnNull(Lazy<T> lazy) {
        final T result = lazy.get();
        assertEquals(null, result);
        final T anotherResult = lazy.get();
        assertSame(result, anotherResult);
    }

    private <T> void testInMultithreadedEnvironment(Lazy<T> lazy, Supplier<T> underlyingSupplier) throws InterruptedException {
        final List<Thread> threads = new ArrayList<>();
        final Runnable taskToExecute = () -> testOnSimpleCase(lazy, underlyingSupplier);

        for (int threadNumber = 0; threadNumber < NUMBER_OF_THREADS; threadNumber++) {
            final Thread thread = new Thread(taskToExecute);
            thread.start();
            threads.add(thread);
        }

        for (Thread thread : threads) {
            thread.join();
        }
    }

    /**
     * Class which allows calling the Supplier only the specified number of times.
     */
    private static class LimitedSupplier<T> implements Supplier<T> {

        final Supplier<T> supplier;
        final AtomicInteger allowedUsages;

        LimitedSupplier(Supplier<T> supplier, int allowedUsages) {
            this.supplier = supplier;
            this.allowedUsages = new AtomicInteger(allowedUsages);
        }

        @Override
        public T get() {
            if (allowedUsages.get() == 0) {
                throw new RuntimeException();
            } else {
                allowedUsages.decrementAndGet();
            }
            return supplier.get();
        }
    }
}