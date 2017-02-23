package ru.spbau.sazanovich.nikita;

import java.util.function.Supplier;

public class LazyFactory {

    public static <T> Lazy<T> createSingleThreadedLazy(Supplier<T> computation) {
        return new SingleThreadedLazy<>(computation);
    }

    public static <T> Lazy<T> createMultiThreadedLazy(Supplier<T> computation) {
        return new MultiThreadedLazy<>(computation);
    }

    public static <T> Lazy<T> createLockFreeLazy(Supplier<T> computation) {
        return new LockFreeLazy<>(computation);
    }
}
