package ru.spbau.sazanovich.nikita.mygit.commands;

import org.apache.logging.log4j.core.Logger;
import org.junit.Before;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Class for test cases which need mocked {@link InternalStateAccessor} to performNormal test.
 */
public abstract class MockedInternalStateAccessorTest {

    InternalStateAccessor accessor;

    @Before
    public void initialize() throws Exception {
        accessor = mock(InternalStateAccessor.class);
        final Logger fakeLogger = mock(Logger.class);
        when(accessor.getLogger()).thenReturn(fakeLogger);
    }
}
