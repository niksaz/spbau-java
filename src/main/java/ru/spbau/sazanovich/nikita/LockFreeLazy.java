package ru.spbau.sazanovich.nikita;

import javax.xml.ws.Holder;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Supplier;

class LockFreeLazy<T> implements Lazy<T> {

    private static final AtomicReferenceFieldUpdater<LockFreeLazy, Holder> resultUpdater =
            AtomicReferenceFieldUpdater.newUpdater(LockFreeLazy.class, Holder.class, "result");

    private final Supplier<T> computation;
    private volatile Holder<T> result;

    LockFreeLazy(Supplier<T> computation) {
        this.computation = computation;
    }

    @Override
    public T get() {
        if (result == null) {
            final Holder<T> result = new Holder<>(computation.get());
            resultUpdater.compareAndSet(this, null, result);
        }
        return result.value;
    }
}
