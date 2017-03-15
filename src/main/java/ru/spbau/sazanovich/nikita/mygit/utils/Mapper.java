package ru.spbau.sazanovich.nikita.mygit.utils;

import org.jetbrains.annotations.NotNull;
import ru.spbau.sazanovich.nikita.mygit.exceptions.MyGitStateException;
import ru.spbau.sazanovich.nikita.mygit.logs.HeadStatus;
import ru.spbau.sazanovich.nikita.mygit.objects.Blob;
import ru.spbau.sazanovich.nikita.mygit.objects.Branch;
import ru.spbau.sazanovich.nikita.mygit.objects.Commit;
import ru.spbau.sazanovich.nikita.mygit.objects.Tree;
import ru.spbau.sazanovich.nikita.mygit.objects.Tree.TreeObject;
import ru.spbau.sazanovich.nikita.mygit.utils.Hasher.HashParts;

import java.io.*;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
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
    public String map(@NotNull Object object) throws MyGitStateException, IOException {
        final String hash = Hasher.getHashFromObject(object);
        final HashParts hashParts = new HashParts(hash);
        final Path directoryPath = Paths.get(myGitDirectory + "/.mygit/objects/" + hashParts.getFirst());
        final Path filePath = Paths.get(directoryPath.toString(), hashParts.getLast());
        if (!directoryPath.toFile().exists()) {
            Files.createDirectory(directoryPath);
        }
        if (!filePath.toFile().exists()) {
            Files.createFile(filePath);
        }
        try (FileOutputStream fileOutputStream = new FileOutputStream(filePath.toFile());
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
        final Path branchPath = Paths.get(myGitDirectory.toString(), ".mygit", "branches", branchName);
        if (!branchPath.toFile().exists()) {
            Files.createFile(branchPath);
        }
        try (FileWriter writer = new FileWriter(branchPath.toFile())
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

    public void moveHeadToCommitHash(@NotNull String commitHash) throws MyGitStateException, IOException {
        final HeadStatus headStatus = getHeadStatus();
        HeadStatus currentHeadStatus;
        if (headStatus.getType().equals(Branch.TYPE)) {
            final String branchName = headStatus.getName();
            writeBranch(branchName, commitHash);
            currentHeadStatus = new HeadStatus(Branch.TYPE, branchName);
        } else {
            currentHeadStatus = new HeadStatus(Commit.TYPE, commitHash);
        }
        setHeadStatus(currentHeadStatus);
    }

    public void setHeadStatus(@NotNull HeadStatus headStatus) throws MyGitStateException, IOException {
        final File headFile = getHeadFile();
        try (FileWriter fileWriter = new FileWriter(headFile)
        ) {
            fileWriter.write(headStatus.getType() + "\n");
            fileWriter.write(headStatus.getName());
        }
    }

    @NotNull
    public HeadStatus getHeadStatus() throws MyGitStateException, IOException {
        final File headFile = getHeadFile();
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
    public String getHeadCommitHash() throws MyGitStateException, IOException {
        final HeadStatus headStatus = getHeadStatus();
        String commitHash;
        if (headStatus.getType().equals(Branch.TYPE)) {
            commitHash = getBranchCommitHash(headStatus.getName());
        } else {
            commitHash = headStatus.getName();
        }
        return commitHash;
    }

    @NotNull
    public Commit getHeadCommit() throws MyGitStateException, IOException {
        return readCommit(getHeadCommitHash());
    }

    @NotNull
    public Tree getHeadTree() throws MyGitStateException, IOException {
        final Commit headCommit = getHeadCommit();
        return readTree(headCommit.getTreeHash());
    }

    @NotNull
    public Tree getBranchTree(@NotNull String branchName) throws MyGitStateException, IOException {
        final String branchCommitHash = getBranchCommitHash(branchName);
        final Commit branchCommit = readCommit(branchCommitHash);
        return readTree(branchCommit.getTreeHash());
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
    public Commit readCommit(@NotNull String commitHash) throws MyGitStateException, IOException {
        return readObjectAndCast(commitHash, Commit.class);
    }

    @NotNull
    public Tree readTree(@NotNull String treeHash) throws MyGitStateException, IOException {
        return readObjectAndCast(treeHash, Tree.class);
    }

    @NotNull
    public Blob readBlob(@NotNull String blobHash) throws MyGitStateException, IOException {
        return readObjectAndCast(blobHash, Blob.class);
    }

    @NotNull
    public String createBlobFromPath(@NotNull Path path) throws MyGitStateException, IOException {
        final byte[] data = Files.readAllBytes(path);
        final Blob blob = new Blob(data);
        return map(blob);
    }

    @NotNull
    public Object readObject(@NotNull String objectHash) throws MyGitStateException, IOException {
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
            return objectInputStream.readObject();
        } catch (ClassNotFoundException e) {
            throw new MyGitStateException("could not read object's file + " + e.getMessage());
        }
    }

    @NotNull
    private <T> T readObjectAndCast(@NotNull String objectHash, @NotNull Class<T> objectClass)
            throws MyGitStateException, IOException {
        return objectClass.cast(readObject(objectHash));
    }

    public void moveFromCommitToCommit(@NotNull Commit fromCommit, Commit toCommit)
            throws MyGitStateException, IOException {
        final Tree fromTree = readTree(fromCommit.getTreeHash());
        final Tree toTree = readTree(toCommit.getTreeHash());
        deleteFilesFromTree(fromTree, myGitDirectory);
        loadFilesFromTree(toTree, myGitDirectory);
    }

    private void loadFilesFromTree(@NotNull Tree tree, @NotNull Path path) throws MyGitStateException, IOException {
        for (TreeObject child : tree.getChildren()) {
            final Path childPath = Paths.get(path.toString(), child.getName());
            final File childFile = childPath.toFile();
            if (child.getType().equals(Blob.TYPE)) {
                if (childFile.exists() && !getTypeForPath(childPath).equals(Blob.TYPE)) {
                    deleteDirectoryWithFiles(childPath);
                    Files.createFile(childPath);
                }
                final Blob childBlob = readBlob(child.getSha());
                Files.write(childPath, childBlob.getContent());
            } else {
                if (childFile.exists() && !getTypeForPath(childPath).equals(Tree.TYPE)) {
                    Files.delete(childPath);
                    Files.createDirectory(childPath);
                }
                final Tree childTree = readTree(child.getSha());
                loadFilesFromTree(childTree, childPath);
            }
        }
    }

    private void deleteFilesFromTree(@NotNull Tree tree, @NotNull Path path) throws MyGitStateException, IOException {
        for (TreeObject child : tree.getChildren()) {
            final Path childPath = Paths.get(path.toString(), child.getName());
            final File childFile = childPath.toFile();
            if (!childFile.exists()) {
                continue;
            }
            if (childFile.isDirectory()) {
                deleteFilesFromTree(readTree(child.getSha()), childPath);
                //noinspection ConstantConditions
                if (childFile.list().length == 0) {
                    Files.delete(childPath);
                }
            } else {
                Files.delete(childPath);
            }
        }
    }

    @NotNull
    private String getTypeForPath(@NotNull Path path) {
        return path.toFile().isDirectory() ? Tree.TYPE : Blob.TYPE;
    }

    @NotNull
    private File getIndexFile() throws MyGitStateException {
        final File indexFile = new File(myGitDirectory + "/.mygit/index");
        if (!indexFile.exists()) {
            throw new MyGitStateException("could not find " + indexFile.getAbsolutePath());
        }
        return indexFile;
    }

    @NotNull
    private File getHeadFile() throws MyGitStateException {
        final File headFile = new File(myGitDirectory + "/.mygit/HEAD");
        if (!headFile.exists()) {
            throw new MyGitStateException("could not find " + headFile.getAbsolutePath());
        }
        return headFile;
    }

    private static void deleteDirectoryWithFiles(@NotNull Path directoryPath) throws IOException {
        Files
        .walk(directoryPath)
        .sorted(Comparator.reverseOrder())
        .map(Path::toFile)
        .forEach(File::delete);
    }
}
