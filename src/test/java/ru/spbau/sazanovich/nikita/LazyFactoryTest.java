package ru.spbau.sazanovich.nikita;

import org.junit.Test;

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

    /**
     * Tests basic properties: correctness of the result, returning of the same object each time,
     * calling exactly once from a single threaded environment.
     */

    @Test
    public void testSingleThreadedLazySimpleCase() {
        testOnSimpleCase(LazyFactory.createSingleThreadedLazy(new LimitedSupplier<>(SUMMER_OF_NUMBERS, 1)));
    }

    @Test
    public void testMultiThreadedLazySimpleCase() {
        testOnSimpleCase(LazyFactory.createMultiThreadedLazy(new LimitedSupplier<>(SUMMER_OF_NUMBERS, 1)));
    }

    @Test
    public void testLockFreeLazySimpleCase() {
        testOnSimpleCase(LazyFactory.createLockFreeLazy(new LimitedSupplier<>(SUMMER_OF_NUMBERS, 1)));
    }

    private void testOnSimpleCase(Lazy<Long> lazy) {
        final Long result = lazy.get();
        assertEquals(SUMMER_OF_NUMBERS.get(), result);
        final Long anotherResult = lazy.get();
        assertSame(result, anotherResult);
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

    private <T> void testOnNull(Lazy<T> lazy) {
        final T result = lazy.get();
        assertEquals(null, result);
        final T anotherResult = lazy.get();
        assertSame(result, anotherResult);
    }

    private static class LimitedSupplier<T> implements Supplier<T> {

        final Supplier<T> supplier;
        int allowedUsages;

        LimitedSupplier(Supplier<T> supplier, int allowedUsages) {
            this.supplier = supplier;
            this.allowedUsages = allowedUsages;
        }

        @Override
        public T get() {
            if (allowedUsages == 0) {
                throw new RuntimeException();
            } else {
                allowedUsages--;
            }
            return supplier.get();
        }
    }
}