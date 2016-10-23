package ru.spbau.sazanovich.nikita;

public interface Predicate<T> extends Function1<T, Boolean> {

    Predicate ALWAYS_TRUE  = x -> true;
    Predicate ALWAYS_FALSE = x -> false;

    default Predicate<T> or(Predicate<? super T> other) {
        return arg -> apply(arg) || other.apply(arg);
    }

    default Predicate<T> and(Predicate<? super T> other) {
        return arg -> apply(arg) && other.apply(arg);
    }

    default Predicate<T> not() {
        return arg -> !apply(arg);
    }
}
