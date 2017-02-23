package ru.spbau.sazanovich.nikita;

import javax.xml.ws.Holder;
import java.util.function.Supplier;

/**
 * Implementation of <a href="Lazy.html">Lazy interface</a>
 * which is for calling {@link #get()} in a single thread.
 *
 * @param <T> result type of the computation
 */
class SingleThreadedLazy<T> implements Lazy<T> {

    private final Supplier<T> computation;
    private Holder<T> result;

    /**
     * Creates a lazy computation from a Supplier object.
     *
     * @param computation a computation to perform
     */
    SingleThreadedLazy(Supplier<T> computation) {
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
            result = new Holder<>(computation.get());
        }
        return result.value;
    }
}
