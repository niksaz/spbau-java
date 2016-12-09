package ru.spbau.sazanovich.nikita.concurrent;

import java.util.function.Supplier;

/**
 * Same semantics as java.util.function.Supplier but may throw checked exceptions.
 * Introduced for more elegant implementation of ThreadPoolImpl.
 */
@FunctionalInterface
interface ThreadSupplier<R> {

    R get() throws LightExecutionException, InterruptedException;

    static <R> ThreadSupplier<R> castSupplier(Supplier<R> supplier) {
        return supplier::get;
    }
}
