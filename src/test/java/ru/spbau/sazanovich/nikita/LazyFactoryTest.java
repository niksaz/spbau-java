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

    @Test
    public void createSingleThreadedLazySimpleCase() {
        final Lazy<Long> computation = LazyFactory.createSingleThreadedLazy(SUMMER_OF_FIRST_NUMBERS);
        testOnSimpleCase(computation);
    }

    @Test
    public void createMultiThreadedLazySimpleCase() {
        final Lazy<Long> computation = LazyFactory.createMultiThreadedLazy(SUMMER_OF_FIRST_NUMBERS);
        testOnSimpleCase(computation);
    }

    @Test
    public void createLockFreeLazySimpleCase() {
        final Lazy<Long> computation = LazyFactory.createLockFreeLazy(SUMMER_OF_FIRST_NUMBERS);
        testOnSimpleCase(computation);
    }

    private void testOnSimpleCase(Lazy<Long> lazy) {
        final Long result = lazy.get();
        assertEquals(SUMMER_OF_FIRST_NUMBERS.get().longValue(), result.longValue());
        final Long anotherResult = lazy.get();
        assertEquals(result, anotherResult);
    }
}