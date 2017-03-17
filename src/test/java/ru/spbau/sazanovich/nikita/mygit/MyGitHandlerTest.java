package ru.spbau.sazanovich.nikita.mygit;

import org.junit.Test;
import ru.spbau.sazanovich.nikita.mygit.status.*;
import ru.spbau.sazanovich.nikita.testing.FolderInitialized;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MyGitHandlerTest extends FolderInitialized {

    private MyGitHandler handler;

    @Override
    public void initialize() throws Exception {
        super.initialize();
        MyGit.init(folderPath);
        handler = new MyGitHandler(folderPath);
    }

    @Test
    public void getHeadChanges() throws Exception {
        final Path inputsPath = Paths.get(folderPath.toString(), "inputs");
        Files.createDirectory(inputsPath);
        handler.addPathsToIndex(Collections.singletonList(inputsPath.toString()));
        List<Change> changes = handler.getHeadChanges();
        assertEquals(1, changes.size());
        assertTrue(changes.get(0) instanceof ChangeToBeCommitted);
        assertTrue(((ChangeToBeCommitted) changes.get(0)).getFileChangeType().equals(FileChangeType.ADDITION));
        handler.commitWithMessage("inputs added");

        final Path input1Path = Paths.get(inputsPath.toString(), "input1.txt");
        Files.createFile(input1Path);
        final Path input2Path = Paths.get(inputsPath.toString(), "input2.txt");
        Files.createFile(input2Path);
        changes = handler.getHeadChanges();
        assertEquals(2, changes.size());
        assertTrue(changes.get(0) instanceof UntrackedFile);
        assertTrue(changes.get(1) instanceof UntrackedFile);
        handler.addPathsToIndex(Arrays.asList(input1Path.toString(), input2Path.toString()));
        handler.commitWithMessage("inputs 1 & 2");

        Files.delete(input1Path);
        changes = handler.getHeadChanges();
        assertEquals(1, changes.size());
        assertTrue(changes.get(0) instanceof ChangeNotStagedForCommit);
        assertTrue(((ChangeNotStagedForCommit) changes.get(0)).getFileChangeType().equals(FileChangeType.REMOVAL));
        handler.addPathsToIndex(Collections.singletonList(input1Path.toString()));
        handler.commitWithMessage("removed 1");

        Files.write(input2Path, new byte[10]);
        changes = handler.getHeadChanges();
        assertEquals(1, changes.size());
        assertTrue(changes.get(0) instanceof ChangeNotStagedForCommit);
        assertTrue(((ChangeNotStagedForCommit) changes.get(0)).getFileChangeType().equals(FileChangeType.MODIFICATION));
    }

    @Test
    public void addPathsToIndex() throws Exception {

    }

    @Test
    public void resetIndexPaths() throws Exception {

    }

    @Test
    public void resetAllIndexPaths() throws Exception {

    }

    @Test
    public void getHeadStatus() throws Exception {

    }

    @Test
    public void getCommitsLogsHistory() throws Exception {

    }

    @Test
    public void checkout() throws Exception {

    }

    @Test
    public void mergeHeadWithBranch() throws Exception {

    }

    @Test
    public void listBranches() throws Exception {

    }

    @Test
    public void createBranch() throws Exception {

    }

    @Test
    public void deleteBranch() throws Exception {

    }

    @Test
    public void commitWithMessage() throws Exception {

    }
}
