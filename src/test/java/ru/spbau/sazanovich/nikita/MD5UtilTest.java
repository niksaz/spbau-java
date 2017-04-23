package ru.spbau.sazanovich.nikita;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class MD5UtilTest {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    private Path rootPath;

    @Before
    public void initialize() throws Exception {
        rootPath = folder.getRoot().toPath();
    }

    @Test
    public void testHashingOfFile() throws Exception {
        final Path nonEmptyFilePath = Paths.get(rootPath.toString(), "file.txt");
        Files.createFile(nonEmptyFilePath);
        Files.write(nonEmptyFilePath, "just some content".getBytes());
        final Path emptyFilePath = Paths.get(rootPath.toString(), "empty.txt");
        Files.createFile(emptyFilePath);

        final MD5Util util = new MD5Util();
        String nonEmptyFileHash = util.getHashFromFile(emptyFilePath.toString());
        String emptyFileHash = util.getHashFromFile(nonEmptyFilePath.toString());
        assertNotEquals(nonEmptyFileHash, emptyFileHash);

        final MD5Util concurrentUtil = new MD5Util(4);
        nonEmptyFileHash = concurrentUtil.getHashFromFile(emptyFilePath.toString());
        emptyFileHash = concurrentUtil.getHashFromFile(nonEmptyFilePath.toString());
        assertNotEquals(nonEmptyFileHash, emptyFileHash);
    }

    @Test
    public void testHashingOfDirectory() throws Exception {
        final Path nonEmptyFolderPath = Paths.get(rootPath.toString(), "inputs");
        Files.createDirectory(nonEmptyFolderPath);
        final Path emptyFolderPath = Paths.get(nonEmptyFolderPath.toString(), "inputs");
        Files.createDirectory(emptyFolderPath);

        final MD5Util util = new MD5Util();
        String nonEmptyFileHash = util.getHashFromFile(nonEmptyFolderPath.toString());
        String emptyFileHash = util.getHashFromFile(emptyFolderPath.toString());
        assertNotEquals(nonEmptyFileHash, emptyFileHash);

        final MD5Util concurrentUtil = new MD5Util(4);
        nonEmptyFileHash = concurrentUtil.getHashFromFile(nonEmptyFolderPath.toString());
        emptyFileHash = concurrentUtil.getHashFromFile(emptyFolderPath.toString());
        assertNotEquals(nonEmptyFileHash, emptyFileHash);
    }

}