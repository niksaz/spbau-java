package ru.spbau.sazanovich.nikita;

import org.junit.Test;

import static org.junit.Assert.*;

public class PredicateTest {

    public static final Predicate<Integer> isEven = x -> x % 2 == 0;
    public static final Predicate<Integer> isOdd = isEven.not();

    @Test
    public void testApply() throws Exception {
        assertFalse(isEven.apply(5));
        assertTrue(isEven.apply(6));
    }

    @Test
    public void testOr() throws Exception {
        final Predicate<Integer> comp = isEven.or(isOdd);
        assertTrue(comp.apply(5));
        assertTrue(comp.apply(6));
    }

    @Test
    public void testAnd() throws Exception {
        final Predicate<Integer> comp = isEven.and(isOdd);
        assertFalse(comp.apply(5));
        assertFalse(comp.apply(6));
    }

    @Test
    public void testNot() throws Exception {
        assertTrue(isOdd.apply(5));
        assertFalse(isOdd.apply(6));
    }
}