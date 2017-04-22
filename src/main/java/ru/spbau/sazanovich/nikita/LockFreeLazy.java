package ru.spbau.sazanovich.nikita;

import javax.xml.ws.Holder;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Supplier;

/**
 * Lock-free implementation of {@link Lazy} interface
 * which may be used for calling {@link #get()} from multiple threads.
 *
 * @param <T> result type of the computation
 */
class LockFreeLazy<T> implements Lazy<T> {

    /**
     * Used to update field if we have different threads performing computations.
     */
    private static final AtomicReferenceFieldUpdater<LockFreeLazy, Holder> resultUpdater =
            AtomicReferenceFieldUpdater.newUpdater(LockFreeLazy.class, Holder.class, "result");

    private final Supplier<T> computation;
    private volatile Holder<T> result;

    /**
     * Creates a lazy computation from a Supplier object.
     *
     * @param computation a computation to perform
     */
    LockFreeLazy(Supplier<T> computation) {
        this.computation = computation;
    }

    /**
     * Method to initiate the computation or get the result from previously performed one.
     *
     * <p>Computation may be performed several times but the result object will be the same.</p>
     *
     * @return a result
     */
    @Override
    public T get() {
        if (result == null) {
            final Holder<T> result = new Holder<>(computation.get());
            resultUpdater.compareAndSet(this, null, result);
        }
        return result.value;
    }
}
