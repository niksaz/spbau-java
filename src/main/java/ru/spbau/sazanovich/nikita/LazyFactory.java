package ru.spbau.sazanovich.nikita;

import java.util.function.Supplier;

/**
 * Factory class which allows you to create different lazy implementations.
 */
public class LazyFactory {

    private LazyFactory() {}

    /**
     * Creates a lazy computation which is suitable for single threaded usage.
     *
     * <p>Computation will be performed only once.</p>
     *
     * @param computation computation to perform
     * @param <T> the return type of the computation
     * @return a result
     */
    public static <T> Lazy<T> createSingleThreadedLazy(Supplier<T> computation) {
        return new SingleThreadedLazy<>(computation);
    }

    /**
     * Creates a lazy computation which is suitable for multi threaded usage.
     *
     * <p>Computation will be performed only once.</p>
     *
     * @param computation computation to perform
     * @param <T> the return type of the computation
     * @return a result
     */
    public static <T> Lazy<T> createMultiThreadedLazy(Supplier<T> computation) {
        return new MultiThreadedLazy<>(computation);
    }

    /**
     * Creates a lazy computation which is suitable for multi threaded usage.
     *
     * <p>Implemented without locks on the object. Computation may be performed several times but the result
     * object will be the same.</p>
     *
     * @param computation computation to perform
     * @param <T> the return type of the computation
     * @return a result
     */
    public static <T> Lazy<T> createLockFreeLazy(Supplier<T> computation) {
        return new LockFreeLazy<>(computation);
    }
}
