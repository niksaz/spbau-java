package ru.spbau.sazanovich.nikita;

import org.junit.Test;

import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;

public class LazyFactoryTest {

    private static final Supplier<Long> SUMMER_OF_FIRST_NUMBERS = () -> {
        long sum = 0;
        for (int i = 0; i <= 1000000000; i++) {
            sum += i;
        }
        return sum;
    };

    /**
     * Tests basic properties: correctness of the result, returning of the same object each time,
     * calling exactly once from a single threaded environment.
     */

    @Test
    public void createSingleThreadedLazySimpleCase() {
        testOnSimpleCase(LazyFactory.createSingleThreadedLazy(new LimitedSupplier<>(SUMMER_OF_FIRST_NUMBERS, 1)));
    }

    @Test
    public void createMultiThreadedLazySimpleCase() {
        testOnSimpleCase(LazyFactory.createMultiThreadedLazy(new LimitedSupplier<>(SUMMER_OF_FIRST_NUMBERS, 1)));
    }

    @Test
    public void createLockFreeLazySimpleCase() {
        testOnSimpleCase(LazyFactory.createLockFreeLazy(new LimitedSupplier<>(SUMMER_OF_FIRST_NUMBERS, 1)));
    }

    private void testOnSimpleCase(Lazy<Long> lazy) {
        final Long result = lazy.get();
        assertEquals(SUMMER_OF_FIRST_NUMBERS.get().longValue(), result.longValue());
        final Long anotherResult = lazy.get();
        assertEquals(result, anotherResult);
    }

    /**
     * Tests whether a computation is starting before calling get().
     */

    @Test
    public void createSingleThreadedLazyBottom() {
        LazyFactory.createSingleThreadedLazy(new LimitedSupplier<>(SUMMER_OF_FIRST_NUMBERS, 0));
    }

    @Test
    public void createMultiThreadedLazyBottom() {
        LazyFactory.createMultiThreadedLazy(new LimitedSupplier<>(SUMMER_OF_FIRST_NUMBERS, 0));
    }

    @Test
    public void createLockFreeLazyBottom() {
        LazyFactory.createLockFreeLazy(new LimitedSupplier<>(SUMMER_OF_FIRST_NUMBERS, 0));
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