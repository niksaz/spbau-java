package ru.spbau.sazanovich.nikita;

import javax.xml.ws.Holder;
import java.util.function.Supplier;

/**
 * Implementation of {@link Lazy} interface
 * which may be used for calling {@link #get()} from multiple threads.
 *
 * @param <T> result type of the computation
 */
class MultiThreadedLazy<T> implements Lazy<T> {

    private Supplier<T> computation;
    private volatile Holder<T> result;

    /**
     * Creates a lazy computation from a Supplier object.
     *
     * @param computation a computation to perform
     */
    MultiThreadedLazy(Supplier<T> computation) {
        this.computation = computation;
    }

    /**
     * Method to initiate the computation or get the result from previously performed one.
     *
     * <p>Computation will be performed once.</p>
     *
     * @return a result
     */
    @Override
    public T get() {
        if (result == null) {
            synchronized (this) {
                if (result == null) {
                    result = new Holder<>(computation.get());
                    computation = null;
                }
            }
        }
        return result.value;
    }
}

