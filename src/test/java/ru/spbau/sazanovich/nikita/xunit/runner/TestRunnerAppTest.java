package ru.spbau.sazanovich.nikita.xunit.runner;

import org.junit.Test;
import ru.spbau.sazanovich.nikita.xunit.XTest;

import java.io.PrintStream;

import static org.mockito.Mockito.*;

public class TestRunnerAppTest {

    @Test
    public void testMain() {
        String[] args = new String[2];
        args[0] = TestClass0.class.getName();
        args[1] = TestClass1.class.getName();

        System.setOut(mock(PrintStream.class));
        TestRunnerApp.main(args);

        assert(TestClass0.called == 1);
        assert(TestClass1.called == 1);
    }

    public static class TestClass0 {

        private static int called;

        @XTest
        public void test() throws Exception {
            called++;
        }
    }

    public static class TestClass1 {

        private static int called;

        @XTest
        public void test() throws Exception {
            called++;
        }
    }
}
