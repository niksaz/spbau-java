package ru.spbau.sazanovich.nikita.console;

import org.junit.Before;
import org.junit.Test;

import java.io.PrintStream;
import java.nio.file.Path;

import static org.mockito.Mockito.*;

public class CommandLineArgsHandlerTest {

    private CommandLineArgsHandler handler;

    @Before
    public void initialize() throws Exception {
        handler = spy(new CommandLineArgsHandler(mock(PrintStream.class), mock(Path.class)));
        doReturn(mock(CommandExecutor.class)).when(handler).createCommandExecutor();
    }

    @Test(expected = CommandNotSupportedException.class)
    public void handleEmpty() throws Exception {
        handler.handle(new String[0]);
    }

    @Test
    public void handleHelp() throws Exception {
        final String[] args = {"help"};
        handler.handle(args);
    }

    @Test
    public void handleInit() throws Exception {
        final String[] args = {"init"};
        handler.handle(args);
    }

    @Test
    public void handleStage() throws Exception {
        final String[] args = {"stage", "."};
        handler.handle(args);
    }

    @Test
    public void handleUnstage() throws Exception {
        final String[] args = {"unstage", "."};
        handler.handle(args);
    }

    @Test
    public void handleUnstageAll() throws Exception {
        final String[] args = {"unstage-all"};
        handler.handle(args);
    }

    @Test
    public void handleReset() throws Exception {
        final String[] args = {"reset", "."};
        handler.handle(args);
    }

    @Test
    public void handleRm() throws Exception {
        final String[] args = {"rm", "greeting.txt"};
        handler.handle(args);
    }

    @Test
    public void handleClean() throws Exception {
        final String[] args = {"clean"};
        handler.handle(args);
    }

    @Test
    public void handleLog() throws Exception {
        final String[] args = {"log"};
        handler.handle(args);
    }

    @Test
    public void handleStatus() throws Exception {
        final String[] args = {"status"};
        handler.handle(args);
    }

    @Test
    public void handleBranch() throws Exception {
        final String[] argsToList = {"branch"};
        handler.handle(argsToList);
        final String[] argsToCreate = {"branch", "test"};
        handler.handle(argsToCreate);
        final String[] argsToDelete = {"branch", "-d", "test"};
        handler.handle(argsToDelete);
    }

    @Test
    public void handleCheckout() throws Exception {
        final String[] args = {"checkout", "master"};
        handler.handle(args);
    }

    @Test
    public void handleCommit() throws Exception {
        final String[] args = {"commit", "hello"};
        handler.handle(args);
    }

    @Test
    public void handleMerge() throws Exception {
        final String[] branchCreateArgs = {"branch", "test"};
        handler.handle(branchCreateArgs);
        final String[] args = {"merge", "test"};
        handler.handle(args);
    }
}
