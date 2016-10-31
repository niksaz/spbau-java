package ru.spbau.sazanovich.nikita;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CollectionsTest {

    public final static Iterable<Integer> NUMBERS = new ArrayList<Integer>() {
        {
            for (int i = LOWER; i < UPPER; i++) {
                add(i);
            }
        }
    };
    public final static List<String> STRING_NUMBERS = Collections.map(Object::toString, NUMBERS);

    private final static int LOWER = 1;
    private final static int UPPER = 10;
    private final static Function2<Collection<String>, Object, Collection<String>> ADD_TO_STRING_COLLECTION =
            (c, object) -> {
                c.add(object.toString());
                return c;
            };

    @Test
    public void testMap() throws Exception {
        final ArrayList<Integer> correct = new ArrayList<>();
        for (int i = LOWER; i < UPPER; i++) {
            correct.add(Function1Test.SQUARE.apply(i));
        }
        assertEquals(correct, Collections.map(Function1Test.SQUARE, NUMBERS));
    }

    @Test
    public void testFilter() throws Exception {
        final ArrayList<Integer> correct = new ArrayList<>();
        for (int i = LOWER; i < UPPER; i++) {
            if (PredicateTest.IS_EVEN.apply(i)) {
                correct.add(i);
            }
        }
        assertEquals(correct, Collections.filter(PredicateTest.IS_EVEN, NUMBERS));
    }

    @Test
    public void testFilterWildcards() throws Exception {
        final Predicate<Object> equalToFive = o -> "5".equals(o.toString());

        final ArrayList<Integer> correct = new ArrayList<>();
        for (int i = LOWER; i < UPPER; i++) {
            if (equalToFive.apply(i)) {
                correct.add(i);
            }
        }
        assertEquals(correct, Collections.filter(equalToFive, NUMBERS));
    }

    @Test
    public void testTakeWhile() throws Exception {
        @SuppressWarnings("unchecked")
        final Predicate<Integer> alwaysTrue = (Predicate<Integer>) Predicate.ALWAYS_TRUE;

        final ArrayList<Integer> correct = new ArrayList<>();
        for (int i = LOWER; i < UPPER; i++) {
            correct.add(i);
        }
        assertEquals(correct, Collections.takeWhile(alwaysTrue, NUMBERS));
    }

    @Test
    public void testTakeWhileWildcards() throws Exception {
        final int limit = 5;
        final Predicate<Integer> lessThanLimit = x -> x.toString().compareTo(Integer.valueOf(limit).toString()) < 0;

        final ArrayList<Integer> correct = new ArrayList<>();
        for (int i = LOWER; i < UPPER; i++) {
            if (lessThanLimit.apply(i)) {
                correct.add(i);
            }
        }
        assertEquals(correct, Collections.takeWhile(lessThanLimit, NUMBERS));
    }

    @Test
    public void testTakeUnless() throws Exception {
        @SuppressWarnings("unchecked")
        final Predicate<Integer> alwaysTrue = (Predicate<Integer>) Predicate.ALWAYS_FALSE;

        final ArrayList<Integer> correct = new ArrayList<>();
        for (int i = LOWER; i < UPPER; i++) {
            correct.add(i);
        }
        assertEquals(correct, Collections.takeUnless(alwaysTrue, NUMBERS));
    }

    @Test
    public void testTakeUnlessWildcards() throws Exception {
        final int limit = 5;
        final Predicate<Object> moreThanLimit = x -> x.toString().compareTo(Integer.valueOf(limit).toString()) > 0;

        final ArrayList<Integer> correct = new ArrayList<>();
        for (int i = LOWER; i < UPPER; i++) {
            if (moreThanLimit.apply(i)) {
                break;
            }
            correct.add(i);
        }
        assertEquals(correct, Collections.takeUnless(moreThanLimit, NUMBERS));
    }

    @Test
    public void testFoldl() throws Exception {
        final StringBuilder builder = new StringBuilder("?");
        for (int i = LOWER; i < UPPER; i++) {
            builder.append(i);
        }
        assertEquals(builder.toString(), Collections.foldl(String::concat, "?", STRING_NUMBERS));
    }

    @Test
    public void testFoldlBeta() throws Exception {
        int product = 1;
        for (int i = LOWER; i < UPPER; i++) {
            product *= i;
        }
        assertEquals(Long.valueOf(1), Long.valueOf(Collections.foldl(Function2Test.DIV, product, NUMBERS)));
    }

    @Test
    public void testFoldlWildcards() throws Exception {
        assertEquals(STRING_NUMBERS, Collections.foldl(ADD_TO_STRING_COLLECTION, new ArrayList<>(), NUMBERS));
    }

    @Test
    public void testFoldr() throws Exception {
        final StringBuilder builder = new StringBuilder();
        for (int i = LOWER; i < UPPER; i++) {
            builder.append(i);
        }
        builder.append('?');
        assertEquals(builder.toString(), Collections.foldr(String::concat, "?", STRING_NUMBERS));
    }

    @Test
    public void testFoldrBeta() throws Exception {
        final ArrayList<Integer> adjacentMultiplication = new ArrayList<>();
        for (int i = UPPER; i > LOWER; i--) {
            adjacentMultiplication.add(i * (i - 1));
        }
        assertEquals(Long.valueOf(UPPER),
                     Long.valueOf(Collections.foldr(Function2Test.DIV, LOWER, adjacentMultiplication)));
    }

    @Test
    public void testFoldRWildcards() throws Exception {
        final List<String> stringNumbersReversed = STRING_NUMBERS;
        java.util.Collections.reverse(stringNumbersReversed);
        assertEquals(stringNumbersReversed,
                     Collections.foldr(ADD_TO_STRING_COLLECTION.flip(), new ArrayList<>(), NUMBERS));
    }
}