package ru.spbau.sazanovich.nikita.mygit;

import org.junit.Test;
import ru.spbau.sazanovich.nikita.mygit.utils.MyGitHasher;
import ru.spbau.sazanovich.nikita.mygit.utils.MyGitHasher.HashParts;
import ru.spbau.sazanovich.nikita.testing.FolderInitialized;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InternalStateAccessorTest extends FolderInitialized {

    @Override
    public void initialize() throws Exception {
        super.initialize();
        final Path myGitInternalPath = Paths.get(folderPath.toString(), ".mygit");
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

        final InternalStateAccessor internalStateAccessor = new InternalStateAccessor(folderPath, mockHasher);
        final String mappedHash = internalStateAccessor.map(objectToMap);
        assertEquals(mockedHash, mappedHash);

        final Path mappedObjectPath =
                Paths.get(folderPath.toString(), ".mygit", "objects", mockedParts.getFirst(), mockedParts.getLast());
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
        final Set<Path> paths = new HashSet<>();
        final Path path1 = Paths.get(folderPath.toString(), "file1.txt");
        final Path path2 = Paths.get(folderPath.toString(), "file2.txt");
        final Path path3 = Paths.get(folderPath.toString(), "file3.txt");
        paths.add(path1);
        paths.add(path2);
        paths.add(path3);
        final Path indexPath = Paths.get(folderPath.toString(), ".mygit", "index");
        try (FileWriter fileWriter = new FileWriter(indexPath.toFile())
        ) {
            fileWriter.write(path1 + "\n");
            fileWriter.write(path2 + "\n");
            fileWriter.write(path3 + "\n");
        }

        final InternalStateAccessor internalStateAccessor = new InternalStateAccessor(folderPath, mock(MyGitHasher.class));
        final Set<Path> readPath = internalStateAccessor.readIndexPaths();
        assertEquals(paths, readPath);
    }

    @Test
    public void writeIndexPaths() throws Exception {
        final Set<Path> relativePaths = new HashSet<>();
        final Path path1 = Paths.get(folderPath.toString(), "file1.txt");
        final Path path2 = Paths.get(folderPath.toString(), "file2.txt");
        final Path path3 = Paths.get(folderPath.toString(), "file3.txt");
        relativePaths.add(folderPath.relativize(path1));
        relativePaths.add(folderPath.relativize(path2));
        relativePaths.add(folderPath.relativize(path3));
        final InternalStateAccessor internalStateAccessor = new InternalStateAccessor(folderPath, mock(MyGitHasher.class));
        internalStateAccessor.writeIndexPaths(relativePaths);

        final Path indexPath = Paths.get(folderPath.toString(), ".mygit", "index");
        final Set<Path> writtenPaths = Files.lines(indexPath).map(line -> Paths.get(line)).collect(Collectors.toSet());
        assertEquals(relativePaths, writtenPaths);
    }


    @Test
    public void writeBranch() throws Exception {

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
