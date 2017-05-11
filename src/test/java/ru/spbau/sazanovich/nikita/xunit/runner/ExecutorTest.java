package ru.spbau.sazanovich.nikita.xunit.runner;

import org.junit.Before;
import org.junit.Test;
import ru.spbau.sazanovich.nikita.xunit.XTest;

import java.io.IOException;
import java.io.PrintStream;

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
    }

    @Test
    public void ignore() throws Exception {
        executor.execute(TestClassWithIgnore.class.getName());
        assert(TestClassWithIgnore.called == 0);
    }

    @SuppressWarnings("WeakerAccess")
    public static class TestClassWithIgnore {
        private static int called;

        @XTest(ignore = "Throws IOException so fails. Fix someone.")
        public void test() throws Exception {
            called++;
            throw new IOException();
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
