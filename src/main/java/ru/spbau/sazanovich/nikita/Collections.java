package ru.spbau.sazanovich.nikita;

import java.util.*;

public class Collections {

    private Collections() {}

    public static <T, R> List<R> map(Function1<? super T, ? extends R> mapper, Iterable<T> iterable) {
        final List<R> result = new ArrayList<>();
        iterable.forEach(element -> result.add(mapper.apply(element)));
        return result;
    }

    public static <T> List<T> filter(Predicate<? super T> predicate, Iterable<T> iterable) {
        final List<T> result = new ArrayList<>();
        for (T element : iterable) {
            if (predicate.apply(element)) {
                result.add(element);
            }
        }
        return result;
    }

    public static <T> List<T> takeWhile(Predicate<? super T> predicate, Iterable<T> iterable) {
        final List<T> result = new ArrayList<>();
        for (T element : iterable) {
            if (!predicate.apply(element)) {
                break;
            }
            result.add(element);
        }
        return result;
    }

    public static <T> List<T> takeUnless(Predicate<? super T> predicate, Iterable<T> iterable) {
        return takeWhile(predicate.not(), iterable);
    }

    public static <T, R> R foldl(Function2<? super R, ? super T, ? extends R> folder,
                                 R initialValue, Iterable<T> iterable) {
        R result = initialValue;
        for (T element : iterable) {
            result = folder.apply(result, element);
        }
        return result;
    }

    public static <T, R> R foldr(Function2<? super T, ? super R, ? extends R> folder,
                                 R initialValue, Iterable<T> iterable) {
        return foldl(folder.flip(), initialValue, () -> new Iterator<T>() {

            final ArrayDeque<T> stack = new ArrayDeque<>();

            {
                iterable.forEach(stack::addLast);
            }

            @Override
            public boolean hasNext() {
                return !stack.isEmpty();
            }

            @Override
            public T next() {
                return stack.pollLast();
            }
        });
    }
}
