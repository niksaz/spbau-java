package ru.spbau.sazanovich.nikita.concurrent;

/**
 * Same semantics as java.util.function.Supplier but may throw checked exceptions.
 * Introduced for more elegant implementation of ThreadPoolImpl.
 */
@FunctionalInterface
interface ThreadSupplier<R> {

    R get() throws LightException, InterruptedException;
}
