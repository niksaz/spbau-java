package ru.spbau.sazanovich.nikita.mygit;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.spbau.sazanovich.nikita.mygit.utils.MyGitHasher;
import ru.spbau.sazanovich.nikita.mygit.utils.MyGitHasher.HashParts;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MapperTest {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    private Path myGitPath;

    @Before
    public void initializeMyGit() throws Exception {
        myGitPath = folder.getRoot().toPath();
        final Path myGitInternalPath = Paths.get(myGitPath.toString(), ".mygit");
        Files.createDirectory(myGitInternalPath);
        Files.createFile(Paths.get(myGitInternalPath.toString(), "HEAD"));
        Files.createFile(Paths.get(myGitInternalPath.toString(), "index"));
        Files.createDirectory(Paths.get(myGitInternalPath.toString(), "objects"));
        Files.createDirectory(Paths.get(myGitInternalPath.toString(), "branches"));
    }

    @Test
    public void map() throws Exception {
        final String objectToMap = "mapping object";
        final MyGitHasher mockHasher = mock(MyGitHasher.class);
        final String mockedHash = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        when(mockHasher.getHashFromObject(any())).thenReturn(mockedHash);
        final HashParts mockedParts = mock(HashParts.class);
        when(mockedParts.getFirst()).thenReturn(mockedHash.substring(0, 2));
        when(mockedParts.getLast()).thenReturn(mockedHash.substring(2));
        when(mockHasher.splitHash(any())).thenReturn(mockedParts);

        final Mapper mapper = new Mapper(myGitPath, mockHasher);
        final String mappedHash = mapper.map(objectToMap);
        assertEquals(mockedHash, mappedHash);

        final Path mappedObjectPath =
                Paths.get(myGitPath.toString(), ".mygit", "objects", mockedParts.getFirst(), mockedParts.getLast());
        assertTrue(mappedObjectPath.toFile().exists());
        try (FileInputStream inputStream = new FileInputStream(mappedObjectPath.toFile());
             ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)
        ) {
            final String mappedString = (String) objectInputStream.readObject();
            assertEquals(objectToMap, mappedString);
        }
    }

    @Test
    public void readIndexPaths() throws Exception {
    }

    @Test
    public void writeBranch() throws Exception {

    }

    @Test
    public void writeIndexPaths() throws Exception {

    }

    @Test
    public void moveHeadToCommitHash() throws Exception {

    }

    @Test
    public void setHeadStatus() throws Exception {

    }

    @Test
    public void getHeadStatus() throws Exception {

    }

    @Test
    public void getHeadCommitHash() throws Exception {

    }

    @Test
    public void getHeadCommit() throws Exception {

    }

    @Test
    public void getHeadTree() throws Exception {

    }

    @Test
    public void getBranchTree() throws Exception {

    }

    @Test
    public void getBranchCommitHash() throws Exception {

    }

    @Test
    public void readCommit() throws Exception {

    }

    @Test
    public void readTree() throws Exception {

    }

    @Test
    public void readBlob() throws Exception {

    }

    @Test
    public void createBlobFromPath() throws Exception {

    }

    @Test
    public void readObject() throws Exception {

    }

    @Test
    public void moveFromCommitToCommit() throws Exception {

    }

}