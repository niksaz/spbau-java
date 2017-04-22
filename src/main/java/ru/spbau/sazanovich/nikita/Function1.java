package ru.spbau.sazanovich.nikita;

public interface Function1<T, R> {

    R apply(T arg);

    default <V> Function1<T, V> compose(Function1<? super R, ? extends V> g) {
        return arg -> g.apply(apply(arg));
    }
}
