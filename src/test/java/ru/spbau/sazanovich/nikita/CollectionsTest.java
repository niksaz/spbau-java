package ru.spbau.sazanovich.nikita;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class CollectionsTest {

    public final static Iterable<Integer> numbers = new ArrayList<Integer>() {
        {
            for (int i = LOWER; i < UPPER; i++) {
                add(i);
            }
        }
    };
    public final static Iterable<String> stringNumbers = Collections.map(Object::toString, numbers);

    private final static int LOWER = 0;
    private final static int UPPER = 10;

    @Test
    public void testMap() throws Exception {
        final ArrayList<Integer> correct = new ArrayList<>();
        for (int i = LOWER; i < UPPER; i++) {
            correct.add(Function1Test.square.apply(i));
        }
        assertEquals(correct, Collections.map(Function1Test.square, numbers));
    }

    @Test
    public void testFilter() throws Exception {
        final ArrayList<Integer> correct = new ArrayList<>();
        for (int i = LOWER; i < UPPER; i++) {
            if (PredicateTest.isEven.apply(i)) {
                correct.add(i);
            }
        }
        assertEquals(correct, Collections.filter(PredicateTest.isEven, numbers));
    }

    @Test
    public void testTakeWhile() throws Exception {
        @SuppressWarnings("unchecked")
        final Predicate<Integer> alwaysTrue = Predicate.ALWAYS_TRUE;

        final ArrayList<Integer> correct = new ArrayList<>();
        for (int i = LOWER; i < UPPER; i++) {
            correct.add(i);
        }
        assertEquals(correct, Collections.takeWhile(alwaysTrue, numbers));
    }

    @Test
    public void testTakeUnless() throws Exception {
        @SuppressWarnings("unchecked")
        final Predicate<Integer> alwaysTrue = Predicate.ALWAYS_FALSE;

        final ArrayList<Integer> correct = new ArrayList<>();
        for (int i = LOWER; i < UPPER; i++) {
            correct.add(i);
        }
        assertEquals(correct, Collections.takeUnless(alwaysTrue, numbers));
    }

    @Test
    public void testFoldl() throws Exception {
        final StringBuilder builder = new StringBuilder("?");
        for (int i = LOWER; i < UPPER; i++) {
            builder.append(i);
        }
        assertEquals(builder.toString(), Collections.foldl(String::concat, "?", stringNumbers));
    }

    @Test
    public void testFoldr() throws Exception {
        final StringBuilder builder = new StringBuilder();
        for (int i = LOWER; i < UPPER; i++) {
            builder.append(i);
        }
        builder.append('?');
        assertEquals(builder.toString(), Collections.foldr(String::concat, "?", stringNumbers));
    }
}