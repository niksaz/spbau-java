package ru.spbau.sazanovich.nikita;

import org.junit.Test;

import static org.junit.Assert.*;

public class Function1Test {

    public static final Function1<Integer, Integer> SQUARE = x -> x * x;
    public static final Function1<String, Integer> STRING_LENGTH = String::length;

    @Test
    public void testApply() throws Exception {
        assertEquals(Integer.valueOf(25), SQUARE.apply(5));
        assertEquals(Integer.valueOf(10000), SQUARE.apply(-100));
    }

    @Test
    public void testCompose() throws Exception {
        final Function1<String, Integer> squareStringLength = STRING_LENGTH.compose(SQUARE);
        assertEquals(Integer.valueOf(25), squareStringLength.apply("Hello"));
        assertEquals(Integer.valueOf(81), squareStringLength.apply("essential"));
    }
}