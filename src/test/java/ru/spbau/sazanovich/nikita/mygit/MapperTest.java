package ru.spbau.sazanovich.nikita.mygit;

import org.junit.Test;
import ru.spbau.sazanovich.nikita.mygit.utils.MyGitHasher;
import ru.spbau.sazanovich.nikita.mygit.utils.MyGitHasher.HashParts;
import ru.spbau.sazanovich.nikita.testing.MyGitInitialized;

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

public class MapperTest extends MyGitInitialized {

    @Override
    public void initializeMyGit() throws Exception {
        super.initializeMyGit();
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
        final Set<Path> paths = new HashSet<>();
        final Path path1 = Paths.get(myGitPath.toString(), "file1.txt");
        final Path path2 = Paths.get(myGitPath.toString(), "file2.txt");
        final Path path3 = Paths.get(myGitPath.toString(), "file3.txt");
        paths.add(path1);
        paths.add(path2);
        paths.add(path3);
        final Path indexPath = Paths.get(myGitPath.toString(), ".mygit", "index");
        try (FileWriter fileWriter = new FileWriter(indexPath.toFile())
        ) {
            fileWriter.write(path1 + "\n");
            fileWriter.write(path2 + "\n");
            fileWriter.write(path3 + "\n");
        }

        final Mapper mapper = new Mapper(myGitPath, mock(MyGitHasher.class));
        final Set<Path> readPath = mapper.readIndexPaths();
        assertEquals(paths, readPath);
    }

    @Test
    public void writeIndexPaths() throws Exception {
        final Set<Path> relativePaths = new HashSet<>();
        final Path path1 = Paths.get(myGitPath.toString(), "file1.txt");
        final Path path2 = Paths.get(myGitPath.toString(), "file2.txt");
        final Path path3 = Paths.get(myGitPath.toString(), "file3.txt");
        relativePaths.add(myGitPath.relativize(path1));
        relativePaths.add(myGitPath.relativize(path2));
        relativePaths.add(myGitPath.relativize(path3));
        final Mapper mapper = new Mapper(myGitPath, mock(MyGitHasher.class));
        mapper.writeIndexPaths(relativePaths);

        final Path indexPath = Paths.get(myGitPath.toString(), ".mygit", "index");
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
