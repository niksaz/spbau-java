package ru.spbau.sazanovich.nikita.mygit;

import org.junit.Test;
import ru.spbau.sazanovich.nikita.mygit.exceptions.MyGitAlreadyInitialized;
import ru.spbau.sazanovich.nikita.mygit.exceptions.MyGitIllegalArgumentException;
import ru.spbau.sazanovich.nikita.testing.MyGitInitialized;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MyGitTest extends MyGitInitialized {

    @Test
    public void init() throws Exception {
        MyGit.init(myGitRepositoryPath);
        final Path myGitDirectory = Paths.get(myGitRepositoryPath.toString(), ".mygit");
        assertTrue(Files.exists(myGitDirectory));
        assertTrue(Files.isDirectory(myGitDirectory));
        final Path headPath = Paths.get(myGitDirectory.toString(), "HEAD");
        assertTrue(Files.exists(headPath));
        assertTrue(Files.isRegularFile(headPath));
        final Path indexPath = Paths.get(myGitDirectory.toString(), "index");
        assertTrue(Files.exists(indexPath));
        assertTrue(Files.isRegularFile(indexPath));
        final Path branchesPath = Paths.get(myGitDirectory.toString(), "branches");
        assertTrue(Files.exists(branchesPath));
        assertTrue(Files.isDirectory(branchesPath));
        final Path branchMasterPath = Paths.get(branchesPath.toString(), "master");
        assertTrue(Files.exists(branchMasterPath));
        assertTrue(Files.isRegularFile(branchMasterPath));
        final Path objectsPath = Paths.get(myGitDirectory.toString(), "objects");
        assertTrue(Files.exists(objectsPath));
        assertTrue(Files.isDirectory(objectsPath));

        final List<Path> objects =
                Files.walk(objectsPath).filter(path -> Files.isRegularFile(path)).collect(Collectors.toList());
        assertEquals(2, objects.size());
    }

    @Test(expected = MyGitIllegalArgumentException.class)
    public void initNotInAbsolute() throws Exception {
        MyGit.init(Paths.get(""));
    }

    @Test(expected = MyGitAlreadyInitialized.class)
    public void initInAlreadyInitialized() throws Exception {
        Files.createDirectory(Paths.get(myGitRepositoryPath.toString(), ".mygit"));
        MyGit.init(myGitRepositoryPath);
    }
}
