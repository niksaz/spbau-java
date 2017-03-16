package ru.spbau.sazanovich.nikita.mygit;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.spbau.sazanovich.nikita.mygit.utils.SHA1Hasher;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class MapperTest {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void map() throws Exception {
//        final File myGitDirectory = folder.newFolder(".mygit");
//        Files.createDirectory(Paths.get(myGitDirectory.toPath().toString(), "objects"));
//        final String objectToMap = "mapping object";
//        final Mapper mapper = new Mapper(folder.getRoot().toPath());
//        final String hash = mapper.map(objectToMap);
    }

    @Test
    public void readIndexPaths() throws Exception {

//        System.out.println(folder.getRoot().toPath());

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