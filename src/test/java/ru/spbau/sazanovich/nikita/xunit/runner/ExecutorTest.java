package ru.spbau.sazanovich.nikita.xunit.runner;

import org.junit.Before;
import org.junit.Test;
import ru.spbau.sazanovich.nikita.xunit.*;

import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class ExecutorTest {

    private static final PrintStream LOG_STREAM = mock(PrintStream.class);

    private Executor executor;

    @Before
    public void init() throws Exception {
        executor = spy(new Executor(LOG_STREAM));
    }

    @Test
    public void execute() throws Exception {
        TestClass testClass = spy(new TestClass());
        executor.execute(testClass);

        final List<String> order = TestClass.methodOrder;
        assertEquals(8, order.size());
        assertEquals("beforeClass", order.get(0));
        assertEquals("afterClass", order.get(7));
        assertTrue(order.get(1).equals("before") && order.get(4).equals("before"));
        assertTrue(order.get(3).equals("after") && order.get(6).equals("after"));
        assertTrue((order.get(2).equals("test") && order.get(5).equals("test2"))
                   || (order.get(2).equals("test2") && order.get(5).equals("test")));
    }

    @SuppressWarnings("WeakerAccess")
    public static class TestClass {
        private static List<String> methodOrder = new LinkedList<>();

        @XBeforeClass
        public static void beforeClass() {
            methodOrder.add("beforeClass");
        }

        @XBefore
        public void before() {
            methodOrder.add("before");
        }

        @XTest
        public void test() throws Exception {
            methodOrder.add("test");
        }

        @XTest
        public void test2() throws Exception {
            methodOrder.add("test2");
        }

        @XAfter
        public void after() {
            methodOrder.add("after");
        }

        @XAfterClass
        public static void afterClass() {
            methodOrder.add("afterClass");
        }
    }

    @Test
    public void ignore() throws Exception {
        TestClassWithIgnore testClassWithIgnore = spy(new TestClassWithIgnore());
        executor.execute(testClassWithIgnore);
        verify(testClassWithIgnore, never()).test();
    }

    @SuppressWarnings("WeakerAccess")
    public static class TestClassWithIgnore {
        @XTest(ignore = "Throws IOException so fails. Fix someone.")
        public void test() throws Exception {
        }
    }

    @Test
    public void showSuccessful() throws Exception {
        executor.execute(TestClassWithSuccessful.class.getName());
        verify(executor, times(2)).showSuccessful();
        verify(executor, never()).showFailedBecauseExpected(any());
        verify(executor, never()).showFailedBecauseOf(any());
        verify(executor, never()).showFailedBecauseExpectedButGot(any(), any());
    }

    @SuppressWarnings("WeakerAccess")
    public static class TestClassWithSuccessful {
        @XTest
        public void test() throws Exception {
        }

        @XTest(expected = IOException.class)
        public void test2() throws Exception {
            throw new IOException();
        }
    }

    @Test
    public void showFailedBecauseExpected() throws Exception {
        executor.execute(TestClassWithFailedBecauseExpected.class.getName());
        verify(executor, never()).showSuccessful();
        verify(executor).showFailedBecauseExpected(IOException.class.getName());
        verify(executor, never()).showFailedBecauseOf(any());
        verify(executor, never()).showFailedBecauseExpectedButGot(any(), any());
    }

    @SuppressWarnings("WeakerAccess")
    public static class TestClassWithFailedBecauseExpected {
        @XTest(expected = IOException.class)
        public void test() throws Exception {
        }
    }

    @Test
    public void showFailedBecauseOf() throws Exception {
        executor.execute(TestClassWithFailedBecauseOf.class.getName());
        verify(executor, never()).showSuccessful();
        verify(executor, never()).showFailedBecauseExpected(any());
        verify(executor).showFailedBecauseOf(any(IOException.class));
        verify(executor, never()).showFailedBecauseExpectedButGot(any(), any());
    }

    @SuppressWarnings("WeakerAccess")
    public static class TestClassWithFailedBecauseOf {
        @XTest
        public void test() throws Exception {
            throw new IOException();
        }
    }

    @Test
    public void showFailedBecauseExpectedButGot() throws Exception {
        executor.execute(TestClassWithFailedBecauseExpectedButGot.class.getName());
        verify(executor, never()).showSuccessful();
        verify(executor, never()).showFailedBecauseExpected(any());
        verify(executor, never()).showFailedBecauseOf(any());
        verify(executor)
                .showFailedBecauseExpectedButGot(eq(IOException.class.getName()), any(NullPointerException.class));
    }

    @SuppressWarnings("WeakerAccess")
    public static class TestClassWithFailedBecauseExpectedButGot {
        @XTest(expected = IOException.class)
        public void test() throws Exception {
            throw new NullPointerException();
        }
    }
}
