package ru.spbau.sazanovich.nikita.console;

import org.junit.Test;
import ru.spbau.sazanovich.nikita.mygit.MyGit;
import ru.spbau.sazanovich.nikita.testing.FolderInitialized;

import java.io.PrintStream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class CommandLineArgsHandlerTest extends FolderInitialized {

    private static final PrintStream FAKE_STREAM = mock(PrintStream.class);

    private CommandLineArgsHandler handler;

    @Override
    public void initialize() throws Exception {
        super.initialize();
        handler = new CommandLineArgsHandler(FAKE_STREAM, folderPath);
    }

    @Test
    public void handleEmpty() throws Exception {
        assertFalse(handler.handle(new String[0]));
    }

    @Test
    public void handleInit() throws Exception {
        final String[] args = {"init"};
        assertTrue(handler.handle(args));
    }

    @Test
    public void handleAdd() throws Exception {
        MyGit.init(folderPath);
        final String[] args = {"add", "."};
        assertTrue(handler.handle(args));
    }

    @Test
    public void handleReset() throws Exception {
        MyGit.init(folderPath);
        final String[] args = {"reset", "."};
        assertTrue(handler.handle(args));
    }

    @Test
    public void handleResetAll() throws Exception {
        MyGit.init(folderPath);
        final String[] args = {"resetall"};
        assertTrue(handler.handle(args));
    }

    @Test
    public void handleLog() throws Exception {
        MyGit.init(folderPath);
        final String[] args = {"log"};
        assertTrue(handler.handle(args));
    }

    @Test
    public void handleStatus() throws Exception {
        MyGit.init(folderPath);
        final String[] args = {"status"};
        assertTrue(handler.handle(args));
    }

    @Test
    public void handleBranch() throws Exception {
        MyGit.init(folderPath);
        final String[] argsToList = {"branch"};
        assertTrue(handler.handle(argsToList));
        final String[] argsToCreate = {"branch", "test"};
        assertTrue(handler.handle(argsToCreate));
        final String[] argsToDelete = {"branch", "-d", "test"};
        assertTrue(handler.handle(argsToDelete));
    }

    @Test
    public void handleCheckout() throws Exception {
        MyGit.init(folderPath);
        final String[] args = {"checkout", "master"};
        assertTrue(handler.handle(args));
    }

    @Test
    public void handleCommit() throws Exception {
        MyGit.init(folderPath);
        final String[] args = {"commit", "hello"};
        assertTrue(handler.handle(args));
    }

    @Test
    public void handleMerge() throws Exception {
        MyGit.init(folderPath);
        final String[] branchCreateArgs = {"branch", "test"};
        handler.handle(branchCreateArgs);
        final String[] args = {"merge", "test"};
        assertTrue(handler.handle(args));
    }
}
