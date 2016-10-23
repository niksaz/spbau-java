package ru.spbau.sazanovich.nikita;

import org.junit.Test;

import static org.junit.Assert.*;

public class Function1Test {

    public static final Function1<Integer, Integer> square = x -> x * x;
    public static final Function1<String, Integer> stringLength = String::length;

    @Test
    public void testApply() throws Exception {
        assertEquals(Integer.valueOf(25), square.apply(5));
        assertEquals(Integer.valueOf(10000), square.apply(-100));
    }

    @Test
    public void testCompose() throws Exception {
        final Function1<String, Integer> squareStringLength = stringLength.compose(square);
        assertEquals(Integer.valueOf(25), squareStringLength.apply("Hello"));
        assertEquals(Integer.valueOf(81), squareStringLength.apply("essential"));
    }
}