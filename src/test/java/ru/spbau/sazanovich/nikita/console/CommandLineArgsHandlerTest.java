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
    public void handleAdd() throws Exception {
        MyGit.init(folderPath);
        final String[] args = {"add", "."};
        handler.handle(args);
    }

    @Test
    public void handleReset() throws Exception {
        MyGit.init(folderPath);
        final String[] args = {"reset", "."};
        handler.handle(args);
    }

    @Test
    public void handleResetAll() throws Exception {
        MyGit.init(folderPath);
        final String[] args = {"resetall"};
        handler.handle(args);
    }

    @Test
    public void handleLog() throws Exception {
        MyGit.init(folderPath);
        final String[] args = {"log"};
        handler.handle(args);
    }

    @Test
    public void handleStatus() throws Exception {
        MyGit.init(folderPath);
        final String[] args = {"status"};
        handler.handle(args);
    }

    @Test
    public void handleBranch() throws Exception {
        MyGit.init(folderPath);
        final String[] argsToList = {"branch"};
        handler.handle(argsToList);
        final String[] argsToCreate = {"branch", "test"};
        handler.handle(argsToCreate);
        final String[] argsToDelete = {"branch", "-d", "test"};
        handler.handle(argsToDelete);
    }

    @Test
    public void handleCheckout() throws Exception {
        MyGit.init(folderPath);
        final String[] args = {"checkout", "master"};
        handler.handle(args);
    }

    @Test
    public void handleCommit() throws Exception {
        MyGit.init(folderPath);
        final String[] args = {"commit", "hello"};
        handler.handle(args);
    }

    @Test
    public void handleMerge() throws Exception {
        MyGit.init(folderPath);
        final String[] branchCreateArgs = {"branch", "test"};
        handler.handle(branchCreateArgs);
        final String[] args = {"merge", "test"};
        handler.handle(args);
    }
}
