package ru.spbau.sazanovich.nikita;

import javax.xml.ws.Holder;
import java.util.function.Supplier;

class MultiThreadedLazy<T> implements Lazy<T> {

    private final Supplier<T> computation;
    private Holder<T> result;

    MultiThreadedLazy(Supplier<T> computation) {
        this.computation = computation;
    }

    @Override
    public synchronized T get() {
        if (result == null) {
            result = new Holder<>(computation.get());
        }
        return result.value;
    }
}
