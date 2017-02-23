package ru.spbau.sazanovich.nikita;

/**
 * Interface for an object performing lazy computations.
 *
 * @param <T> the type of the computation's result
 */
public interface Lazy<T> {

    /**
     * Method which performs computation.
     *
     * @return result of the computation
     */
    T get();
}
