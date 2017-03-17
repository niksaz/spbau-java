package ru.spbau.sazanovich.nikita;

import org.junit.Test;
import ru.spbau.sazanovich.nikita.mygit.MyGit;
import ru.spbau.sazanovich.nikita.mygit.MyGitHandler;
import ru.spbau.sazanovich.nikita.mygit.logs.CommitLog;
import ru.spbau.sazanovich.nikita.mygit.status.Change;
import ru.spbau.sazanovich.nikita.mygit.status.UntrackedFile;
import ru.spbau.sazanovich.nikita.testing.MyGitInitialized;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WorkflowTest extends MyGitInitialized {

    private MyGitHandler handler;

    @Override
    public void initializeMyGit() throws Exception {
        super.initializeMyGit();
        MyGit.init(myGitRepositoryPath);
        handler = new MyGitHandler(myGitRepositoryPath);
    }

    @Test
    public void workflowWithMutualChangesMerge() throws Exception {
        final Path filePath = Paths.get(myGitRepositoryPath.toString(), "file.txt");
        Files.createFile(filePath);
        final List<Change> changeList = handler.getHeadChanges();
        assertEquals(1, changeList.size());
        assertTrue(changeList.get(0) instanceof UntrackedFile);

        handler.addPathsToIndex(Collections.singletonList(filePath.toString()));
        handler.commitWithMessage("first file");

        handler.createBranch("test");
        Files.write(filePath, Collections.singletonList("will be replaced"));
        handler.addPathsToIndex(Collections.singletonList(filePath.toString()));
        handler.commitWithMessage("file change 1");

        sleep(100);

        handler.checkout("test");
        final String ultimateString = "ultimate content";
        Files.write(filePath, Collections.singletonList(ultimateString));
        handler.addPathsToIndex(Collections.singletonList(filePath.toString()));
        handler.commitWithMessage("file change 2");

        handler.checkout("master");
        handler.mergeHeadWithBranch("test");
        assertEquals(5, handler.getCommitsLogsHistory().size());
        assertEquals(Collections.singletonList(ultimateString), Files.readAllLines(filePath));
        handler.deleteBranch("test");
        assertEquals(1, handler.listBranches().size());
    }

    @Test
    public void workflowWithMergingDifferentFiles() throws Exception {
        final List<CommitLog> logsHistory = handler.getCommitsLogsHistory();
        assertEquals(1, logsHistory.size());
        final String initialRevisionHash = logsHistory.get(0).getRevisionHash();

        final Path readmePath = Paths.get(myGitRepositoryPath.toString(), "README.md");
        Files.createFile(readmePath);
        final Path inputsPath = Paths.get(myGitRepositoryPath.toString(), "inputs");
        Files.createDirectory(inputsPath);
        final Path someInputPath = Paths.get(inputsPath.toString(), "input1.txt");
        Files.createFile(someInputPath);
        handler.addPathsToIndex(
                Arrays.asList(readmePath.toString(), inputsPath.toString(), someInputPath.toString()));
        handler.commitWithMessage("inputs directory added");

        handler.checkout(initialRevisionHash);
        final Path howToFile = Paths.get(myGitRepositoryPath.toString(), "HOWTO.md");
        Files.createFile(howToFile);
        Files.createFile(inputsPath);
        handler.addPathsToIndex(Arrays.asList(howToFile.toString(), inputsPath.toString()));
        handler.commitWithMessage("inputs files created");
        handler.createBranch("test");

        handler.checkout("master");
        assertTrue(Files.isDirectory(inputsPath));
        assertTrue(Files.isRegularFile(someInputPath));

        handler.mergeHeadWithBranch("test");
        assertTrue(Files.isRegularFile(inputsPath));
        assertTrue(!Files.exists(someInputPath));
        assertTrue(Files.exists(howToFile));
        assertTrue(Files.exists(readmePath));

        final List<Change> changes = handler.getHeadChanges();
        assertTrue(changes.isEmpty());
    }
}
