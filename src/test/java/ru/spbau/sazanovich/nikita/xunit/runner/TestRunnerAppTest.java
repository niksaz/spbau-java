package ru.spbau.sazanovich.nikita.xunit.runner;

import org.junit.Test;
import ru.spbau.sazanovich.nikita.xunit.XTest;

import java.io.PrintStream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class TestRunnerAppTest {

    @Test
    public void testMain() {
        String[] args = new String[2];
        args[0] = TestClass0.class.getName();
        args[1] = TestClass1.class.getName();

        System.setOut(mock(PrintStream.class));
        TestRunnerApp.main(args);

        assertEquals(1, TestClass0.constructed);
        assertEquals(1, TestClass0.invoked);
        assertEquals(1, TestClass1.constructed);
        assertEquals(1, TestClass1.invoked);
    }

    public static class TestClass0 {

        private static int constructed;
        private static int invoked;

        public TestClass0() {
            constructed++;
        }

        @XTest
        public void test() throws Exception {
            invoked++;
        }
    }

    public static class TestClass1 {

        private static int constructed;
        private static int invoked;

        public TestClass1() {
            constructed++;
        }

        @XTest
        public void test() throws Exception {
            invoked++;
        }
    }
}
