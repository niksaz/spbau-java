package ru.spbau.sazanovich.nikita;

import org.junit.Test;

import static org.junit.Assert.*;

public class PredicateTest {

    public static final Predicate<Integer> IS_EVEN = x -> x % 2 == 0;
    public static final Predicate<Integer> IS_ODD = IS_EVEN.not();
    public static final Predicate<Object> UNDEFINED = o -> {
        throw new IllegalStateException();
    };

    @Test
    public void testApply() throws Exception {
        assertFalse(IS_EVEN.apply(5));
        assertTrue(IS_EVEN.apply(6));
    }

    @Test
    public void testOr() throws Exception {
        final Predicate<Integer> comp = IS_EVEN.or(IS_ODD);
        assertTrue(comp.apply(5));
        assertTrue(comp.apply(6));
    }

    @Test
    public void testOrLaziness() throws Exception {
        final Predicate<Integer> comp = IS_EVEN.or(UNDEFINED);
        boolean thrown = false;
        try {
            assertTrue(comp.apply(5));
        } catch (IllegalStateException correct) {
            thrown = true;
        } finally {
            assertTrue(thrown);
        }
        assertTrue(comp.apply(6));
    }

    @Test
    public void testAnd() throws Exception {
        final Predicate<Integer> comp = IS_EVEN.and(IS_ODD);
        assertFalse(comp.apply(5));
        assertFalse(comp.apply(6));
    }

    @Test
    public void testAndLaziness() throws Exception {
        final Predicate<Integer> comp = IS_EVEN.and(UNDEFINED);
        assertFalse(comp.apply(5));
        boolean thrown = false;
        try {
            assertTrue(comp.apply(6));
        } catch (IllegalStateException correct) {
            thrown = true;
        } finally {
            assertTrue(thrown);
        }
    }

    @Test
    public void testNot() throws Exception {
        assertTrue(IS_ODD.apply(5));
        assertFalse(IS_ODD.apply(6));
    }
}