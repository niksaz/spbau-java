package ru.spbau.sazanovich.nikita.mygit.utils;

import org.jetbrains.annotations.NotNull;
import ru.spbau.sazanovich.nikita.mygit.exceptions.MyGitFilesystemException;
import ru.spbau.sazanovich.nikita.mygit.exceptions.MyGitStateException;
import ru.spbau.sazanovich.nikita.mygit.objects.Branch;
import ru.spbau.sazanovich.nikita.mygit.objects.Commit;
import ru.spbau.sazanovich.nikita.mygit.objects.Tree;
import ru.spbau.sazanovich.nikita.mygit.utils.Hasher.HashParts;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utils method to serialize Java {@link java.io.Serializable} classes.
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
    public Tree getHeadTree() throws MyGitStateException, IOException {
        final File headFile = new File(myGitDirectory + "/.mygit/HEAD");
        if (!headFile.exists()) {
            throw new MyGitStateException("could not find " + headFile.getAbsolutePath());
        }
        final List<String> headLines = Files.lines(headFile.toPath()).collect(Collectors.toList());
        if (headLines.size() != 2) {
            throw new MyGitStateException("corrupted HEAD file: odd number of lines");
        }
        final String headType = headLines.get(0);
        final String headPath = headLines.get(1);
        String commitHash;
        switch (headType) {
            case Branch.TYPE:
                final File branchFile = new File(myGitDirectory + "/.mygit/branches/" + headPath);
                if (!branchFile.exists()) {
                    throw new MyGitStateException("corrupted HEAD file: could not find " + branchFile.getAbsolutePath());
                }
                final List<String> branchLines =
                        Files.lines(branchFile.toPath()).collect(Collectors.toCollection(ArrayList::new));
                if (branchLines.size() != 1) {
                    throw new MyGitStateException("not single line in branch " + headPath);
                }
                commitHash = branchLines.get(0);
                break;
            case Commit.TYPE:
                commitHash = headPath;
                break;
            default:
                throw new MyGitStateException("corrupted HEAD file");
        }
        return getCommitsTree(commitHash);
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
    private Tree getCommitsTree(@NotNull String commitHash) throws MyGitStateException, IOException {
        final Commit commit = readCommit(commitHash);
        return readTree(commit.getTreeHash());
    }

    @NotNull
    private <T> T readObject(@NotNull String objectHash, @NotNull Class<T> objectClass)
            throws MyGitStateException, IOException {
        final HashParts hashParts = new HashParts(objectHash);
        final String objectPath =
                myGitDirectory + "/.mygit/objects/" + hashParts.getFirst() + "/" + hashParts.getLast();
        final File objectFile = new File(objectPath);
        if (!objectFile.exists()) {
            throw new MyGitStateException("could not find object's file: " + objectFile.getAbsolutePath());
        }
        try (FileInputStream fileInputStream = new FileInputStream(objectFile);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)
        ) {
            return objectClass.cast(objectInputStream.readObject());
        } catch (ClassNotFoundException e) {
            throw new MyGitStateException("could not read object's file + " + e.getMessage());
        }
    }
}
