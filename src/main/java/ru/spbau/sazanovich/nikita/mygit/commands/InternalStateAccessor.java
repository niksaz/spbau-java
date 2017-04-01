package ru.spbau.sazanovich.nikita.mygit.commands;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbau.sazanovich.nikita.mygit.MyGitAlreadyInitializedException;
import ru.spbau.sazanovich.nikita.mygit.MyGitIllegalArgumentException;
import ru.spbau.sazanovich.nikita.mygit.MyGitStateException;
import ru.spbau.sazanovich.nikita.mygit.objects.HeadStatus;
import ru.spbau.sazanovich.nikita.mygit.objects.Blob;
import ru.spbau.sazanovich.nikita.mygit.objects.Branch;
import ru.spbau.sazanovich.nikita.mygit.objects.Commit;
import ru.spbau.sazanovich.nikita.mygit.objects.Tree;
import ru.spbau.sazanovich.nikita.mygit.objects.Tree.TreeEdge;
import ru.spbau.sazanovich.nikita.mygit.utils.MyGitHasher;
import ru.spbau.sazanovich.nikita.mygit.utils.MyGitHasher.HashParts;
import ru.spbau.sazanovich.nikita.mygit.utils.SHA1Hasher;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

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
        final Path path = findMyGitDirectoryPath(currentDirectory);
        if (path == null) {
            throw new MyGitStateException("Not a mygit repository (or any of the parent directories)");
        }
        this.myGitDirectory = path;
        this.currentDirectory = currentDirectory;
        this.hasher = hasher;
    }

    @NotNull
    Path getMyGitDirectory() {
        return myGitDirectory;
    }

    @NotNull
    Path getCurrentDirectory() {
        return currentDirectory;
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
                Paths.get(myGitDirectory.toString(),".mygit", "objects", shaHashParts.getFirst());
        final Path filePath = Paths.get(directoryPath.toString(), shaHashParts.getLast());
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
    Set<Path> readIndexPaths() throws MyGitStateException, IOException {
        final File indexFile = getIndexFile();
        return Files
                .lines(indexFile.toPath())
                .map(Paths::get)
                .collect(Collectors.toCollection(HashSet::new));
    }

    void writeIndexPaths(@NotNull Set<Path> paths) throws MyGitStateException, IOException {
        final File indexFile = getIndexFile();
        try (FileWriter fileWriter = new FileWriter(indexFile);
             BufferedWriter writer = new BufferedWriter(fileWriter)
        ) {
            for (Path path : paths) {
                writer.write(path + "\n");
            }
        }
    }

    void writeBranch(@NotNull String branchName, @NotNull String commitHash) throws IOException {
        final Path branchPath = Paths.get(myGitDirectory.toString(), ".mygit", "branches", branchName);
        if (!branchPath.toFile().exists()) {
            Files.createFile(branchPath);
        }
        try (FileWriter writer = new FileWriter(branchPath.toFile())
        ) {
            writer.write(commitHash);
        }
    }

    void deleteBranch(@NotNull String branchName) throws IOException {
        final File branchFile = new File(myGitDirectory + "/.mygit/branches/" + branchName);
        Files.delete(branchFile.toPath());
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
        final File headFile = getHeadFile();
        try (FileWriter fileWriter = new FileWriter(headFile)
        ) {
            fileWriter.write(headStatus.getType() + "\n");
            fileWriter.write(headStatus.getName());
        }
    }

    @NotNull
    HeadStatus getHeadStatus() throws MyGitStateException, IOException {
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
    String createBlobFromPath(@NotNull Path path) throws MyGitStateException, IOException {
        final byte[] data = Files.readAllBytes(path);
        final Blob blob = new Blob(data);
        return map(blob);
    }

    @NotNull
    private Object readObject(@NotNull String objectHash) throws MyGitStateException, IOException {
        HashParts shaHashParts;
        try {
            shaHashParts = hasher.splitHash(objectHash);
        } catch (MyGitIllegalArgumentException ignored) {
            throw new MyGitStateException("met an illegal hash value " + objectHash);
        }
        final String objectPath =
                myGitDirectory + "/.mygit/objects/" + shaHashParts.getFirst() + "/" + shaHashParts.getLast();
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

    void moveFromCommitToCommit(@NotNull Commit fromCommit, Commit toCommit)
            throws MyGitStateException, IOException {
        final Tree fromTree = readTree(fromCommit.getTreeHash());
        final Tree toTree = readTree(toCommit.getTreeHash());
        deleteFilesFromTree(fromTree, myGitDirectory);
        loadFilesFromTree(toTree, myGitDirectory);
    }

    /**
     * Computes the object's hash using current hasher.
     *
     * @param object an object to hash
     * @return hash of the object
     * @throws IOException if an exception occurs in a hasher
     */
    String getObjectHash(@NotNull Object object) throws IOException {
        return hasher.getHashFromObject(object);
    }

    @NotNull
    private <T> T readObjectAndCast(@NotNull String objectHash, @NotNull Class<T> objectClass)
            throws MyGitStateException, IOException {
        return objectClass.cast(readObject(objectHash));
    }

    private void loadFilesFromTree(@NotNull Tree tree, @NotNull Path path) throws MyGitStateException, IOException {
        for (TreeEdge child : tree.getChildren()) {
            final Path childPath = Paths.get(path.toString(), child.getName());

            if (child.getType().equals(Blob.TYPE)) {
                if (Files.exists(childPath) && !getTypeForPath(childPath).equals(Blob.TYPE)) {
                    deleteDirectoryWithFiles(childPath);
                }
                if (!Files.exists(childPath)) {
                    Files.createFile(childPath);
                }
                final Blob childBlob = readBlob(child.getHash());
                Files.write(childPath, childBlob.getContent());
            } else {
                if (Files.exists(childPath) && !getTypeForPath(childPath).equals(Tree.TYPE)) {
                    Files.delete(childPath);
                }
                if (!Files.exists(childPath)) {
                    Files.createDirectory(childPath);
                }
                final Tree childTree = readTree(child.getHash());
                loadFilesFromTree(childTree, childPath);
            }
        }
    }

    private void deleteFilesFromTree(@NotNull Tree tree, @NotNull Path path) throws MyGitStateException, IOException {
        for (TreeEdge child : tree.getChildren()) {
            final Path childPath = Paths.get(path.toString(), child.getName());
            final File childFile = childPath.toFile();
            if (!childFile.exists()) {
                continue;
            }
            if (childFile.isDirectory()) {
                if (child.isDirectory()) {
                    deleteFilesFromTree(readTree(child.getHash()), childPath);
                    //noinspection ConstantConditions
                    if (childFile.list().length == 0) {
                        Files.delete(childPath);
                    }
                } else {
                    deleteDirectoryWithFiles(childPath);
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void deleteDirectoryWithFiles(@NotNull Path directoryPath) throws IOException {
        Files
        .walk(directoryPath)
        .sorted(Comparator.reverseOrder())
        .map(Path::toFile)
        .forEach(File::delete);
    }

    @NotNull
    List<Branch> listBranches() throws MyGitStateException, IOException {
        final File branchesDirectory = new File(myGitDirectory + "/.mygit/branches/");
        if (!branchesDirectory.exists()) {
            throw new MyGitStateException("could not find " + branchesDirectory);
        }
        final File[] branches = branchesDirectory.listFiles();
        if (branches == null) {
            throw new IOException("could not read " + branchesDirectory);
        }
        return Arrays
                .stream(branches)
                .map(file -> new Branch(file.getName()))
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

    @Nullable
    private static Path findMyGitDirectoryPath(@Nullable Path currentDirectory) {
        if (currentDirectory == null) {
            return null;
        }
        final Path possibleMyGitDirectory = Paths.get(currentDirectory.toString(), ".mygit");
        if (Files.exists(possibleMyGitDirectory)) {
            return currentDirectory;
        } else {
            return findMyGitDirectoryPath(currentDirectory.getParent());
        }
    }

    static void init(@NotNull Path directory)
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
    }

    @NotNull
    private static String createInitialCommit(@NotNull InternalStateAccessor internalStateAccessor)
            throws MyGitStateException, IOException {
        final String treeHash = internalStateAccessor.map(new Tree());
        final Commit primaryCommit = new Commit(treeHash);
        return internalStateAccessor.map(primaryCommit);
    }

    boolean isAbsolutePathRepresentsInternal(@Nullable Path path) {
        return pathContainsMyGitAsSubpath(myGitDirectory.relativize(path));
    }

    static boolean pathContainsMyGitAsSubpath(@Nullable Path path) {
        return path != null && (path.endsWith(".mygit") || pathContainsMyGitAsSubpath(path.getParent()));
    }

    @NotNull
    Path relativizeWithMyGitDirectory(@NotNull Path path) {
        return myGitDirectory.relativize(path);
    }
}
