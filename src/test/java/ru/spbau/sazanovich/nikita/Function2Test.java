package ru.spbau.sazanovich.nikita;

import org.junit.Test;

import static org.junit.Assert.*;

public class Function2Test {

    public static final Function2<Integer, Integer, Integer> sum = (x, y) -> x + y;
    public static final Function2<Integer, Integer, Integer> div = (x, y) -> x / y;

    @Test
    public void testApply() throws Exception {
        assertEquals(Integer.valueOf(50), sum.apply(15, 35));
        assertEquals(Integer.valueOf(50), sum.apply(35, 15));
    }

    @Test
    public void testCompose() throws Exception {
        final Function2<Integer, Integer, Integer> squareSum = sum.compose(Function1Test.square);
        assertEquals(Integer.valueOf(100), squareSum.apply(3, 7));
        assertEquals(Integer.valueOf(81), squareSum.apply(-1, 10));
    }

    @Test
    public void testBind1() throws Exception {
        final Function1<Integer, Integer> binded = div.bind1(10);
        assertEquals(Integer.valueOf(5), binded.apply(2));
    }

    @Test
    public void testBind2() throws Exception {
        final Function1<Integer, Integer> binded = div.bind2(2);
        assertEquals(Integer.valueOf(5), binded.apply(10));
    }

    @Test
    public void testCurry() throws Exception {
        final Function1<Integer, Integer> curried = div.curry().apply(100);
        assertEquals(Integer.valueOf(20), curried.apply(5));
    }

    @Test
    public void testFlip() throws Exception {
        final Function2<Integer, Integer, Integer> flipped = div.flip();
        assertEquals(Integer.valueOf(5), flipped.apply(100, 500));
    }
}