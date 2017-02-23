package ru.spbau.sazanovich.nikita;

import javax.xml.ws.Holder;
import java.util.function.Supplier;

class SingleThreadedLazy<T> implements Lazy<T> {

    private final Supplier<T> computation;
    private Holder<T> result;

    SingleThreadedLazy(Supplier<T> computation) {
        this.computation = computation;
    }

    @Override
    public T get() {
        if (result == null) {
            result = new Holder<>(computation.get());
        }
        return result.value;
    }
}
