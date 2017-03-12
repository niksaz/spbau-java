package ru.spbau.sazanovich.nikita.mygit.utils;

import org.jetbrains.annotations.NotNull;
import ru.spbau.sazanovich.nikita.mygit.exceptions.MyGitFilesystemException;
import ru.spbau.sazanovich.nikita.mygit.exceptions.MyGitStateException;
import ru.spbau.sazanovich.nikita.mygit.logs.HeadStatus;
import ru.spbau.sazanovich.nikita.mygit.objects.Blob;
import ru.spbau.sazanovich.nikita.mygit.objects.Branch;
import ru.spbau.sazanovich.nikita.mygit.objects.Commit;
import ru.spbau.sazanovich.nikita.mygit.objects.Tree;
import ru.spbau.sazanovich.nikita.mygit.utils.Hasher.HashParts;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class which is used to map object and files to
 */
public class Mapper {

    @NotNull
    private final Path myGitDirectory;

    public Mapper(@NotNull Path path) {
        this.myGitDirectory = path;
    }

    @NotNull
    public String map(@NotNull Object object) throws MyGitFilesystemException, IOException, MyGitStateException {
        final String hash = Hasher.getHashFromObject(object);
        final HashParts hashParts = new HashParts(hash);
        final String directoryName = myGitDirectory + "/.mygit/objects/" + hashParts.getFirst();
        final String fileName = directoryName + "/" + hashParts.getLast();
        boolean createdSuccessfully = new File(directoryName).mkdir();
        if (!createdSuccessfully) {
            throw new MyGitFilesystemException("could not create " + fileName);
        }
        final File objectFile = new File(fileName);
        //noinspection ResultOfMethodCallIgnored
        objectFile.createNewFile();
        try (FileOutputStream fileOutputStream = new FileOutputStream(objectFile);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)
        ) {
            objectOutputStream.writeObject(object);
        }
        return hash;
    }

    @NotNull
    public Set<Path> readIndexPaths() throws MyGitStateException, IOException {
        final File indexFile = getIndexFile();
        return Files
               .lines(indexFile.toPath())
               .map(Paths::get)
               .map(Path::toAbsolutePath)
               .collect(Collectors.toCollection(HashSet::new));
    }

    public void writeBranch(@NotNull String branchName, @NotNull String commitHash) throws IOException {
        final File branchFile = new File(myGitDirectory + "/.mygit/branches/" + branchName);
        //noinspection ResultOfMethodCallIgnored
        branchFile.createNewFile();
        try (FileWriter writer = new FileWriter(branchFile)
        ) {
            writer.write(commitHash);
        }
    }

    public void writeIndexPaths(@NotNull Set<Path> paths) throws MyGitStateException, IOException {
        final File indexFile = getIndexFile();
        try (FileWriter fileWriter = new FileWriter(indexFile);
             BufferedWriter writer = new BufferedWriter(fileWriter)
        ) {
            for (Path path : paths) {
                writer.write(myGitDirectory.relativize(path).toString() + "\n");
            }
        }
    }

    @NotNull
    public HeadStatus getHeadStatus() throws MyGitStateException, IOException {
        final File headFile = new File(myGitDirectory + "/.mygit/HEAD");
        if (!headFile.exists()) {
            throw new MyGitStateException("could not find " + headFile.getAbsolutePath());
        }
        final List<String> headLines = Files.lines(headFile.toPath()).collect(Collectors.toList());
        if (headLines.size() != 2) {
            throw new MyGitStateException("corrupted HEAD file -- odd number of lines");
        }
        final String headType = headLines.get(0);
        final String headPath = headLines.get(1);
        if (!headType.equals(Branch.TYPE) && !headType.equals(Commit.TYPE)) {
            throw new MyGitStateException("corrupted HEAD file -- unknown HEAD type");
        }
        return new HeadStatus(headType, headPath);
    }

    @NotNull
    public Commit getHeadCommit() throws MyGitStateException, IOException {
        final HeadStatus headStatus = getHeadStatus();
        String commitHash;
        if (headStatus.getType().equals(Branch.TYPE)) {
            commitHash = getBranchCommitHash(headStatus.getName());
        } else {
            commitHash = headStatus.getName();
        }
        return readCommit(commitHash);
    }

    @NotNull
    public String getBranchCommitHash(@NotNull String branchName) throws MyGitStateException, IOException {
        final File branchFile = new File(myGitDirectory + "/.mygit/branches/" + branchName);
        if (!branchFile.exists()) {
            throw new MyGitStateException("could not find branch '" + branchName + "'");
        }
        final List<String> branchLines =
                Files.lines(branchFile.toPath()).collect(Collectors.toCollection(ArrayList::new));
        if (branchLines.size() != 1) {
            throw new MyGitStateException("not a single line in branch '" + branchName + "'");
        }
        return branchLines.get(0);
    }

    @NotNull
    public Tree getHeadTree() throws MyGitStateException, IOException {
        final Commit headCommit = getHeadCommit();
        return readTree(headCommit.getTreeHash());
    }

    @NotNull
    public Commit readCommit(@NotNull String commitHash) throws MyGitStateException, IOException {
        return readObject(commitHash, Commit.class);
    }

    @NotNull
    public Tree readTree(@NotNull String treeHash) throws MyGitStateException, IOException {
        return readObject(treeHash, Tree.class);
    }

    @NotNull
    public Blob readBlob(@NotNull String blobHash) throws MyGitStateException, IOException {
        return readObject(blobHash, Blob.class);
    }

    @NotNull
    private <T> T readObject(@NotNull String objectHash, @NotNull Class<T> objectClass)
            throws MyGitStateException, IOException {
        final HashParts hashParts = new HashParts(objectHash);
        final String objectPath =
                myGitDirectory + "/.mygit/objects/" + hashParts.getFirst() + "/" + hashParts.getLast();
        final File objectFile = new File(objectPath);
        if (!objectFile.exists()) {
            throw new MyGitStateException("could not find object's file -- " + objectFile.getAbsolutePath());
        }
        try (FileInputStream fileInputStream = new FileInputStream(objectFile);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)
        ) {
            return objectClass.cast(objectInputStream.readObject());
        } catch (ClassNotFoundException e) {
            throw new MyGitStateException("could not read object's file + " + e.getMessage());
        }
    }

    @NotNull
    private File getIndexFile() throws MyGitStateException {
        final File indexFile = new File(myGitDirectory + "/.mygit/index");
        if (!indexFile.exists()) {
            throw new MyGitStateException("could not find " + indexFile.getAbsolutePath());
        }
        return indexFile;
    }
}
