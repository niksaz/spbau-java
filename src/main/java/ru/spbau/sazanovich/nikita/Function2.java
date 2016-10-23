package ru.spbau.sazanovich.nikita;

public interface Function2<T1, T2, R> {

    R apply(T1 arg1, T2 arg2);

    default <V> Function2<T1, T2, V> compose(Function1<? super R, ? extends V> g) {
        return (arg1, arg2) -> g.apply(apply(arg1, arg2));
    }

    default Function1<T2, R> bind1(T1 arg1) {
        return arg -> apply(arg1, arg);
    }

    default Function1<T1, R> bind2(T2 arg2) {
        return arg -> apply(arg, arg2);
    }

    default Function1<T1, Function1<T2, R>> curry() {
        return arg1 -> arg2 -> apply(arg1, arg2);
    }

    default Function2<T2, T1, R> flip() {
        return (arg2, arg1) -> apply(arg1, arg2);
    }
}
