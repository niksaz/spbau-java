package ru.spbau.sazanovich.nikita.mygit.commands;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbau.sazanovich.nikita.mygit.MyGitAlreadyInitializedException;
import ru.spbau.sazanovich.nikita.mygit.MyGitIllegalArgumentException;
import ru.spbau.sazanovich.nikita.mygit.MyGitStateException;
import ru.spbau.sazanovich.nikita.mygit.logger.Log4j2ContextBuilder;
import ru.spbau.sazanovich.nikita.mygit.objects.*;
import ru.spbau.sazanovich.nikita.mygit.objects.Tree.TreeEdge;
import ru.spbau.sazanovich.nikita.mygit.utils.*;
import ru.spbau.sazanovich.nikita.mygit.utils.MyGitHasher.HashParts;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static ru.spbau.sazanovich.nikita.mygit.utils.FileSystem.pathContainsNameAsSubpath;

/**
 * Class which is used to make updates to internal representation in a filesystem.
 */
class InternalStateAccessor {

    @NotNull
    private final Path myGitDirectory;
    @NotNull
    private final Path currentDirectory;
    @NotNull
    private final MyGitHasher hasher;
    @NotNull
    private final Logger logger;

    /**
     * Constructs an accessor in a given directory.
     *
     * @param currentDirectory a path to the current directory for a handler
     * @throws MyGitIllegalArgumentException if the directory path is not absolute
     * @throws MyGitStateException           if the directory (or any of the parent directories) is not a MyGit repository
     */
    InternalStateAccessor(@NotNull Path currentDirectory, @NotNull MyGitHasher hasher)
            throws MyGitIllegalArgumentException, MyGitStateException {
        if (!currentDirectory.isAbsolute()) {
            throw new MyGitIllegalArgumentException("parameter should be an absolute path");
        }
        final Path path = FileSystem.findFirstDirectoryAbove(".mygit", currentDirectory);
        if (path == null) {
            throw new MyGitStateException("Not a mygit repository (or any of the parent directories)");
        }
        this.myGitDirectory = path;
        this.currentDirectory = currentDirectory;
        this.hasher = hasher;
        final Path myGitInternalsDirectory = myGitDirectory.resolve(Paths.get(".mygit"));
        final LoggerContext loggerContext =
                Log4j2ContextBuilder.createContext(myGitInternalsDirectory);
        this.logger = loggerContext.getRootLogger();
        logger.trace("Initialized logger");
    }

    @NotNull
    Path getMyGitDirectory() {
        return myGitDirectory;
    }

    @NotNull
    Logger getLogger() {
        return logger;
    }

    @NotNull
    String map(@NotNull Object object) throws MyGitStateException, IOException {
        final String hash = hasher.getHashFromObject(object);
        HashParts shaHashParts;
        try {
            shaHashParts = hasher.splitHash(hash);
        } catch (MyGitIllegalArgumentException ignored) {
            throw new MyGitStateException("met an illegal hash value " + hash);
        }
        final Path directoryPath =
                Paths.get(myGitDirectory.toString(), ".mygit", "objects", shaHashParts.getFirst());
        final Path filePath = Paths.get(directoryPath.toString(), shaHashParts.getLast());
        if (!Files.exists(directoryPath)) {
            Files.createDirectory(directoryPath);
        }
        if (!Files.exists(filePath)) {
            Files.createFile(filePath);
        }
        try (OutputStream outputStream = Files.newOutputStream(filePath);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)
        ) {
            objectOutputStream.writeObject(object);
        }
        return hash;
    }

    @NotNull
    Set<Path> readIndexPaths() throws MyGitStateException, IOException {
        final Path indexFile = getIndexFile();
        return Files
                .lines(indexFile)
                .map(Paths::get)
                .collect(Collectors.toCollection(HashSet::new));
    }

    void writeIndexPaths(@NotNull Set<Path> paths) throws MyGitStateException, IOException {
        final Path indexFile = getIndexFile();
        try (BufferedWriter writer = Files.newBufferedWriter(indexFile)
        ) {
            for (Path path : paths) {
                writer.write(path + "\n");
            }
        }
    }

    void writeBranch(@NotNull String branchName, @NotNull String commitHash) throws IOException {
        final Path branchPath = Paths.get(myGitDirectory.toString(), ".mygit", "branches", branchName);
        if (!Files.exists(branchPath)) {
            Files.createFile(branchPath);
        }
        try (FileWriter writer = new FileWriter(branchPath.toFile())
        ) {
            writer.write(commitHash);
        }
    }

    void deleteBranch(@NotNull String branchName) throws IOException {
        final Path branchFile = Paths.get(myGitDirectory.toString(), ".mygit", "branches", branchName);
        Files.delete(branchFile);
    }

    void moveHeadToCommitHash(@NotNull String commitHash) throws MyGitStateException, IOException {
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

    void setHeadStatus(@NotNull HeadStatus headStatus) throws MyGitStateException, IOException {
        final Path headFile = getHeadFile();
        try (BufferedWriter writer = Files.newBufferedWriter(headFile)
        ) {
            writer.write(headStatus.getType() + "\n");
            writer.write(headStatus.getName());
        }
    }

    @NotNull
    HeadStatus getHeadStatus() throws MyGitStateException, IOException {
        final Path headFile = getHeadFile();
        final List<String> headLines = Files.lines(headFile).collect(Collectors.toList());
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
    String getHeadCommitHash() throws MyGitStateException, IOException {
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
    Commit getHeadCommit() throws MyGitStateException, IOException {
        return readCommit(getHeadCommitHash());
    }

    @NotNull
    Tree getHeadTree() throws MyGitStateException, IOException {
        final Commit headCommit = getHeadCommit();
        return readTree(headCommit.getTreeHash());
    }

    @NotNull
    Tree getBranchTree(@NotNull String branchName) throws MyGitStateException, IOException {
        final String branchCommitHash = getBranchCommitHash(branchName);
        final Commit branchCommit = readCommit(branchCommitHash);
        return readTree(branchCommit.getTreeHash());
    }

    @NotNull
    String getBranchCommitHash(@NotNull String branchName) throws MyGitStateException, IOException {
        final Path branchFile = Paths.get(myGitDirectory.toString(), ".mygit", "branches", branchName);
        if (!Files.exists(branchFile)) {
            throw new MyGitStateException("could not find branch '" + branchName + "'");
        }
        final List<String> branchLines =
                Files.lines(branchFile).collect(Collectors.toCollection(ArrayList::new));
        if (branchLines.size() != 1) {
            throw new MyGitStateException("not a single line in branch '" + branchName + "'");
        }
        return branchLines.get(0);
    }

    @NotNull
    Commit readCommit(@NotNull String commitHash) throws MyGitStateException, IOException {
        return readObjectAndCast(commitHash, Commit.class);
    }

    @NotNull
    Tree readTree(@NotNull String treeHash) throws MyGitStateException, IOException {
        return readObjectAndCast(treeHash, Tree.class);
    }

    @NotNull
    Blob readBlob(@NotNull String blobHash) throws MyGitStateException, IOException {
        return readObjectAndCast(blobHash, Blob.class);
    }

    @NotNull
    private <T> T readObjectAndCast(@NotNull String objectHash, @NotNull Class<T> objectClass)
            throws MyGitStateException, IOException {
        return objectClass.cast(readObject(objectHash));
    }

    @NotNull
    private Object readObject(@NotNull String objectHash) throws MyGitStateException, IOException {
        HashParts shaHashParts;
        try {
            shaHashParts = hasher.splitHash(objectHash);
        } catch (MyGitIllegalArgumentException ignored) {
            throw new MyGitStateException("met an illegal hash value " + objectHash);
        }
        final Path objectPath = Paths.get(
                myGitDirectory.toString(), ".mygit", "objects", shaHashParts.getFirst(), shaHashParts.getLast());

        if (!Files.exists(objectPath)) {
            throw new MyGitStateException("could not find object's file -- " + objectPath);
        }
        try (InputStream inputStream = Files.newInputStream(objectPath);
             ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)
        ) {
            return objectInputStream.readObject();
        } catch (ClassNotFoundException e) {
            throw new MyGitStateException("could not read object's file + " + e.getMessage());
        }
    }

    @NotNull
    String createBlobFromPath(@NotNull Path path) throws MyGitStateException, IOException {
        final byte[] data = Files.readAllBytes(path);
        final Blob blob = new Blob(data);
        return map(blob);
    }

    String getObjectHash(@NotNull Object object) throws IOException {
        return hasher.getHashFromObject(object);
    }

    @NotNull
    private Path getIndexFile() throws MyGitStateException {
        final Path indexFile = Paths.get(myGitDirectory.toString(), ".mygit", "index");
        if (!Files.exists(indexFile)) {
            throw new MyGitStateException("could not find " + indexFile);
        }
        return indexFile;
    }

    @NotNull
    private Path getHeadFile() throws MyGitStateException {
        final Path headFile = Paths.get(myGitDirectory.toString(), ".mygit", "HEAD");
        if (!Files.exists(headFile)) {
            throw new MyGitStateException("could not find " + headFile);
        }
        return headFile;
    }

    @NotNull
    List<Branch> listBranches() throws MyGitStateException, IOException {
        final Path branchesDirectory = Paths.get(myGitDirectory.toString(), ".mygit", "branches");
        if (!Files.exists(branchesDirectory)) {
            throw new MyGitStateException("could not find " + branchesDirectory);
        }
        return Files
                .list(branchesDirectory)
                .map(path -> new Branch(path.getFileName().toString()))
                .collect(Collectors.toList());
    }

    @NotNull
    List<String> listCommitHashes() throws MyGitStateException, IOException {
        final Path objectsPath = Paths.get(myGitDirectory.toString(), ".mygit", "objects");
        final List<String> objectHashes =
                Files
                        .walk(objectsPath)
                        .filter(path -> !path.toFile().isDirectory())
                        .map(path -> {
                            final Path parent = path.getParent();
                            return parent.getFileName().toString() + path.getFileName().toString();
                        })
                        .collect(Collectors.toList());
        final List<String> commitHashes = new ArrayList<>();
        for (String objectHash : objectHashes) {
            final Object object = readObject(objectHash);
            if (object instanceof Commit) {
                commitHashes.add(objectHash);
            }
        }
        return commitHashes;
    }

    static InternalStateAccessor init(@NotNull Path directory)
            throws MyGitAlreadyInitializedException, MyGitStateException, IOException {
        final Path myGitPath = Paths.get(directory.toString(), ".mygit");
        if (Files.exists(myGitPath)) {
            throw new MyGitAlreadyInitializedException();
        }
        Files.createDirectory(myGitPath);
        InternalStateAccessor internalStateAccessor;
        try {
            internalStateAccessor = new InternalStateAccessor(directory, new SHA1Hasher());
        } catch (MyGitIllegalArgumentException ignored) {
            throw new IllegalStateException();
        }
        Files.createFile(Paths.get(myGitPath.toString(), "HEAD"));
        internalStateAccessor.setHeadStatus(new HeadStatus(Branch.TYPE, "master"));
        Files.createFile(Paths.get(myGitPath.toString(), "index"));
        Files.createDirectory(Paths.get(myGitPath.toString(), "objects"));
        Files.createDirectory(Paths.get(myGitPath.toString(), "branches"));
        final String commitHash = createInitialCommit(internalStateAccessor);
        internalStateAccessor.writeBranch("master", commitHash);
        return internalStateAccessor;
    }

    @NotNull
    private static String createInitialCommit(@NotNull InternalStateAccessor internalStateAccessor)
            throws MyGitStateException, IOException {
        final String treeHash = internalStateAccessor.map(new Tree());
        final Commit primaryCommit = new Commit(treeHash);
        return internalStateAccessor.map(primaryCommit);
    }

    @NotNull
    Path relativizeWithMyGitDirectory(@NotNull Path path) {
        return myGitDirectory.relativize(path);
    }

    @Nullable
    Path convertStringToPathRelativeToMyGitDirectory(@NotNull String stringPath)
            throws MyGitIllegalArgumentException {
        Path path;
        try {
            path = Paths.get(stringPath);
        } catch (InvalidPathException e) {
            throw new MyGitIllegalArgumentException(stringPath + " is invalid path");
        }
        if (!path.isAbsolute()) {
            path = currentDirectory.resolve(path).normalize();
        }
        if (!path.startsWith(myGitDirectory)) {
            throw new MyGitIllegalArgumentException(
                    "paths should be located in the mygit repository's directory, but " + path + " does not");
        }
        return isAbsolutePathRepresentsInternal(path) ? null : relativizeWithMyGitDirectory(path);
    }

    boolean isAbsolutePathRepresentsInternal(@NotNull Path path) {
        return myGitDirectory.equals(path)
                || pathContainsNameAsSubpath(relativizeWithMyGitDirectory(path), ".mygit");
    }

    @Nullable
    TreeEdge findElementInHeadTree(@NotNull Path path) throws MyGitStateException, IOException {
        final Tree tree;
        if (path.getParent() == null) {
            tree = getHeadTree();
        } else {
            final TreeEdge edge = findElementInHeadTree(path.getParent());
            if (edge == null || !edge.isDirectory()) {
                return null;
            }
            tree = readTree(edge.getHash());
        }
        for (TreeEdge edge : tree.getChildren()) {
            if (edge.getName().equals(path.toString())) {
                return edge;
            }
        }
        return null;
    }
}
