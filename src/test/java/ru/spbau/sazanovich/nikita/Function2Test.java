package ru.spbau.sazanovich.nikita;

import org.junit.Test;

import static org.junit.Assert.*;

public class Function2Test {

    public static final Function2<Integer, Integer, Integer> SUM = (x, y) -> x + y;
    public static final Function2<Integer, Integer, Integer> DIV = (x, y) -> x / y;

    @Test
    public void testApply() throws Exception {
        assertEquals(Integer.valueOf(50), SUM.apply(15, 35));
        assertEquals(Integer.valueOf(50), SUM.apply(35, 15));
    }

    @Test
    public void testCompose() throws Exception {
        final Function2<Integer, Integer, Integer> squareSum = SUM.compose(Function1Test.SQUARE);
        assertEquals(Integer.valueOf(100), squareSum.apply(3, 7));
        assertEquals(Integer.valueOf(81), squareSum.apply(-1, 10));
    }

    @Test
    public void testBind1() throws Exception {
        final Function1<Integer, Integer> bound = DIV.bind1(10);
        assertEquals(Integer.valueOf(5), bound.apply(2));
    }

    @Test
    public void testBind2() throws Exception {
        final Function1<Integer, Integer> bound = DIV.bind2(2);
        assertEquals(Integer.valueOf(5), bound.apply(10));
    }

    @Test
    public void testCurry() throws Exception {
        final Function1<Integer, Integer> curried = DIV.curry().apply(100);
        assertEquals(Integer.valueOf(20), curried.apply(5));
    }

    @Test
    public void testFlip() throws Exception {
        final Function2<Integer, Integer, Integer> flipped = DIV.flip();
        assertEquals(Integer.valueOf(5), flipped.apply(100, 500));
    }
}