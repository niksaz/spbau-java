package ru.spbau.sazanovich.nikita.mygit;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbau.sazanovich.nikita.mygit.exceptions.*;
import ru.spbau.sazanovich.nikita.mygit.logs.CommitLog;
import ru.spbau.sazanovich.nikita.mygit.logs.HeadStatus;
import ru.spbau.sazanovich.nikita.mygit.objects.Blob;
import ru.spbau.sazanovich.nikita.mygit.objects.Branch;
import ru.spbau.sazanovich.nikita.mygit.objects.Commit;
import ru.spbau.sazanovich.nikita.mygit.objects.Tree;
import ru.spbau.sazanovich.nikita.mygit.objects.Tree.TreeEdge;
import ru.spbau.sazanovich.nikita.mygit.status.*;
import ru.spbau.sazanovich.nikita.mygit.utils.SHA1Hasher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Class which should be instantiated by a user to interact with the library.
 */
public class MyGitHandler {

    @NotNull
    private final Path myGitDirectory;
    @NotNull
    private final Path currentDirectory;

    @NotNull
    private final Mapper mapper;

    /**
     * Initialize MyGit repository in a given directory.
     *
     * @param directory an absolute path to a directory to initialize MyGit in
     * @throws MyGitIllegalArgumentException if the directory path is not absolute
     * @throws MyGitAlreadyInitializedException if the directory already contains .mygit file
     * @throws MyGitStateException if an internal error occurs during operations
     * @throws IOException if an error occurs during working with a filesystem
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void init(@NotNull Path directory)
            throws MyGitIllegalArgumentException, MyGitAlreadyInitializedException, MyGitStateException, IOException {
        if (!directory.isAbsolute()) {
            throw new MyGitIllegalArgumentException("path parameter should be an absolute");
        }
        final Path myGitPath = Paths.get(directory.toString(), ".mygit");
        if (myGitPath.toFile().exists()) {
            throw new MyGitAlreadyInitializedException();
        } else {
            Files.createDirectory(myGitPath);
            final Mapper mapper = new Mapper(myGitPath.toAbsolutePath().getParent(), new SHA1Hasher());
            Files.createFile(Paths.get(myGitPath.toString(), "HEAD"));
            mapper.setHeadStatus(new HeadStatus(Branch.TYPE, "master"));
            Files.createFile(Paths.get(myGitPath.toString(), "index"));
            Files.createDirectory(Paths.get(myGitPath.toString(), "objects"));
            Files.createDirectory(Paths.get(myGitPath.toString(), "branches"));
            final String commitHash = createInitialCommit(mapper);
            mapper.writeBranch("master", commitHash);
        }
    }

    @NotNull
    private static String createInitialCommit(@NotNull Mapper mapper) throws MyGitStateException, IOException  {
        final String treeHash = mapper.map(new Tree());
        final Commit primaryCommit = new Commit(treeHash);
        return mapper.map(primaryCommit);
    }

    /**
     * Constructs a handler in a given directory.
     *
     * @param currentDirectory a path to the current directory for a handler
     * @throws MyGitIllegalArgumentException if the directory path is not absolute
     * @throws MyGitStateException if the directory (or any of the parent directories) is not a MyGit repository
     */
    public MyGitHandler(@NotNull Path currentDirectory) throws MyGitIllegalArgumentException, MyGitStateException {
        if (!currentDirectory.isAbsolute()) {
            throw new MyGitIllegalArgumentException("parameter should be an absolute path");
        }
        this.currentDirectory = currentDirectory;
        final Path path = findMyGitPath(currentDirectory);
        if (path == null) {
            throw new MyGitStateException("Not a mygit repository (or any of the parent directories)");
        }
        myGitDirectory = path;
        mapper = new Mapper(myGitDirectory, new SHA1Hasher());
    }

    /**
     * Gets differences between current MyGit repository's HEAD state and the filesystem state.
     *
     * @return list of change objects
     * @throws MyGitStateException if an internal error occurs during operations
     * @throws IOException if an error occurs during working with a filesystem
     */
    @NotNull
    public List<Change> getHeadChanges() throws MyGitStateException, IOException {
        final Tree tree = mapper.getHeadTree();
        final Set<Path> indexedPaths = mapper.readIndexPaths();
        final List<Change> changes = getChangeList(tree, myGitDirectory, indexedPaths);
        changes.forEach(change -> change.relativizePath(myGitDirectory));
        return changes;
    }

    /**
     * Adds paths to the current index.
     *
     * @param arguments list of paths to add to the index
     * @throws MyGitIllegalArgumentException if the list contains incorrect/located outside MyGit repository paths
     * @throws MyGitStateException if an internal error occurs during operations
     * @throws IOException if an error occurs during working with a filesystem
     */
    public void addPathsToIndex(@NotNull List<String> arguments)
            throws MyGitIllegalArgumentException, MyGitStateException, IOException {
        final Function<Set<Path>, Consumer<Path>> action =
                paths -> (Consumer<Path>) path -> {
                    if (!paths.contains(path)) {
                        paths.add(path);
                    }
                };
        performUpdateToIndex(arguments, action);
    }

    /**
     * Removes paths from the current index.
     *
     * @param arguments list of paths to remove from the index
     * @throws MyGitIllegalArgumentException if the list contains incorrect/located outside MyGit repository paths
     * @throws MyGitStateException if an internal error occurs during operations
     * @throws IOException if an error occurs during working with a filesystem
     */
    public void resetIndexPaths(@NotNull List<String> arguments)
            throws MyGitStateException, MyGitIllegalArgumentException, IOException {
        final Function<Set<Path>, Consumer<Path>> action =
                paths -> (Consumer<Path>) path -> {
                    if (paths.contains(path)) {
                        paths.remove(path);
                    }
                };
        performUpdateToIndex(arguments, action);
    }

    /**
     * Removes all paths from the current index.
     *
     * @throws MyGitStateException if an internal error occurs during operations
     * @throws IOException if an error occurs during working with a filesystem
     */
    public void resetAllIndexPaths() throws MyGitStateException, IOException {
        mapper.writeIndexPaths(new HashSet<>());
    }

    /**
     * Gets the current HEAD state.
     *
     * @return a head state
     * @throws MyGitStateException if an internal error occurs during operations
     * @throws IOException if an error occurs during working with a filesystem
     */
    @NotNull
    public HeadStatus getHeadStatus() throws MyGitStateException, IOException {
        return mapper.getHeadStatus();
    }

    /**
     * Gets the logs from commit's which are reachable from the HEAD's commit.
     *
     * @return list of commit's logs
     * @throws MyGitStateException if an internal error occurs during operations
     * @throws IOException if an error occurs during working with a filesystem
     */
    @NotNull
    public List<CommitLog> getCommitsLogsHistory() throws MyGitStateException, IOException {
        final Commit headCommit = mapper.getHeadCommit();
        final TreeSet<Commit> commitTree = new TreeSet<>();
        traverseCommitsTree(headCommit, commitTree);
        final List<CommitLog> logsHistory = new ArrayList<>();
        for (Commit commit : commitTree) {
            final CommitLog log =
                    new CommitLog(mapper.getObjectHash(commit), commit.getMessage(),
                            commit.getAuthor(), commit.getDateCreated());
            logsHistory.add(log);
        }
        Collections.reverse(logsHistory);
        return logsHistory;
    }

    /**
     * Checkouts a revision and moves HEAD there.
     *
     * Replaces all files which differs from the HEAD's ones if such exist. Index should be empty before checking out.
     *
     * @param revisionName a name of revision
     * @throws MyGitMissingPrerequisitesException if the index is not empty
     * @throws MyGitIllegalArgumentException if the revision does not exist
     * @throws MyGitStateException if an internal error occurs during operations
     * @throws IOException if an error occurs during working with a filesystem
     */
    public void checkout(@NotNull String revisionName)
            throws MyGitStateException, MyGitMissingPrerequisitesException, MyGitIllegalArgumentException, IOException {
        if (!mapper.readIndexPaths().isEmpty()) {
            throw new MyGitMissingPrerequisitesException("staging area should be empty before a checkout operation");
        }
        final Commit fromCommit = mapper.getHeadCommit();
        String toCommitHash;
        String toHeadType;
        if (listBranches().contains(new Branch(revisionName))) {
            toCommitHash = mapper.getBranchCommitHash(revisionName);
            toHeadType = Branch.TYPE;
        } else {
            if (listCommitHashes().contains(revisionName)) {
                toCommitHash = revisionName;
                toHeadType = Commit.TYPE;
            } else {
                throw new MyGitIllegalArgumentException("there is no such revision -- " + revisionName);
            }
        }
        final Commit toCommit = mapper.readCommit(toCommitHash);
        mapper.moveFromCommitToCommit(fromCommit, toCommit);
        final HeadStatus toHeadStatus = new HeadStatus(toHeadType, revisionName);
        mapper.setHeadStatus(toHeadStatus);
    }

    /**
     * Perform a merge operation of HEAD and given branch.
     *
     * Chooses files based on their last modification's date -- older have a priority. Index should be empty before
     * merging and HEAD should not be in detached state.
     *
     * @param branch branch with which to merge HEAD
     * @throws MyGitMissingPrerequisitesException if the index is not empty or currently in a detached HEAD state
     * @throws MyGitIllegalArgumentException if there is no such branch or a user tries to merge branch with itself
     * @throws MyGitStateException if an internal error occurs during operations
     * @throws IOException if an error occurs during working with a filesystem
     */
    public void mergeHeadWithBranch(@NotNull String branch)
            throws MyGitMissingPrerequisitesException, MyGitStateException, IOException, MyGitIllegalArgumentException {
        final HeadStatus headStatus = mapper.getHeadStatus();
        if (headStatus.getName().equals(Commit.TYPE)) {
            throw new MyGitMissingPrerequisitesException("could not merge while you are in detached HEAD state");
        }
        if (!listBranches().contains(new Branch(branch))) {
            throw new MyGitIllegalArgumentException("there is no such branch -- " + branch);
        }
        if (headStatus.getName().equals(branch)) {
            throw new MyGitIllegalArgumentException("can not merge branch with itself");
        }
        if (!mapper.readIndexPaths().isEmpty()) {
            throw new MyGitMissingPrerequisitesException("staging area should be empty before a merge operation");
        }
        final String baseBranch = headStatus.getName();
        final Tree baseTree = mapper.getBranchTree(baseBranch);
        final Tree otherTree = mapper.getBranchTree(branch);

        final String mergeTreeHash = mergeTwoTrees(baseTree, otherTree);
        final List<String> parentsHashes = new ArrayList<>();
        parentsHashes.add(mapper.getBranchCommitHash(baseBranch));
        parentsHashes.add(mapper.getBranchCommitHash(branch));
        final Commit mergeCommit = new Commit(mergeTreeHash, "merge commit", parentsHashes);
        final String mergeCommitHash = mapper.map(mergeCommit);
        mapper.writeBranch(baseBranch, mergeCommitHash);

        checkout(baseBranch);
    }

    /**
     * Gets list of branches in MyGit repository.
     *
     * @return list of branches
     * @throws MyGitStateException if an internal error occurs during operations
     * @throws IOException if an error occurs during working with a filesystem
     */
    @NotNull
    public List<Branch> listBranches() throws MyGitStateException, IOException {
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

    /**
     * Creates a new branch with the given name.
     *
     * @param branchName branch name for a new branch
     * @throws MyGitIllegalArgumentException if the branch with the name already exists
     * @throws MyGitStateException if an internal error occurs during operations
     * @throws IOException if an error occurs during working with a filesystem
     */
    public void createBranch(@NotNull String branchName)
            throws MyGitStateException, IOException, MyGitIllegalArgumentException {
        if (doesBranchExists(branchName)) {
            throw new MyGitIllegalArgumentException("'" + branchName + "' branch already exists");
        }
        mapper.writeBranch(branchName, mapper.getHeadCommitHash());
    }

    /**
     * Removes the branch with the given name.
     *
     * @param branchName the name of the branch to delete
     * @throws MyGitIllegalArgumentException if there is no a branch with this name or
     * this branch is currently checked out
     * @throws MyGitStateException if an internal error occurs during operations
     * @throws IOException if an error occurs during working with a filesystem
     */
    public void deleteBranch(@NotNull String branchName)
            throws MyGitIllegalArgumentException, IOException, MyGitStateException {
        final HeadStatus headStatus = getHeadStatus();
        if (headStatus.getType().equals(Branch.TYPE) && headStatus.getName().equals(branchName)) {
            throw new MyGitIllegalArgumentException(
                    "cannot delete branch '" + branchName +
                    "' checked out at " + myGitDirectory);
        }
        if (!doesBranchExists(branchName)) {
            throw new MyGitIllegalArgumentException("'" + branchName + "' branch is missing");
        }
        final File branchFile = new File(myGitDirectory + "/.mygit/branches/" + branchName);
        Files.delete(branchFile.toPath());
    }

    /**
     * Commits all indexed files and moves head to the newly created commit.
     *
     * @param message commit's message
     * @throws MyGitStateException if an internal error occurs during operations
     * @throws IOException if an error occurs during working with a filesystem
     */
    public void commitWithMessage(@NotNull String message) throws MyGitStateException, IOException {
        final Tree tree = mapper.getHeadTree();
        final Set<Path> indexedPaths = mapper.readIndexPaths();
        final String rebuiltTreeHash = rebuildTree(tree, myGitDirectory, indexedPaths);
        final List<String> parentsHashes = new ArrayList<>();
        parentsHashes.add(mapper.getHeadCommitHash());
        final Commit commit = new Commit(rebuiltTreeHash, message, parentsHashes);
        final String commitHash = mapper.map(commit);
        mapper.moveHeadToCommitHash(commitHash);
        resetAllIndexPaths();
    }

    private String rebuildTree(@Nullable Tree tree, @NotNull Path prefixPath, @NotNull Set<Path> indexedPaths)
            throws MyGitStateException, IOException {
        final List<Path> filePaths =
                Files
                .list(prefixPath)
                .filter(path -> !isAbsolutePathRepresentsInternal(path))
                .collect(Collectors.toList());
        final Tree rebuiltTree = new Tree();
        final List<TreeEdge> childrenList = tree == null ? new ArrayList<>() : tree.getChildren();
        for (TreeEdge child : childrenList) {
            final Path childPath = Paths.get(prefixPath.toString(), child.getName());
            boolean contained = filePaths.contains(childPath);
            switch (child.getType()) {
                case Tree.TYPE:
                    final Tree childTree = mapper.readTree(child.getHash());
                    if (contained) {
                        filePaths.remove(childPath);
                        if (childPath.toFile().isDirectory()) {
                            final String childHash = rebuildTree(childTree, childPath, indexedPaths);
                            rebuiltTree.addChild(new TreeEdge(childHash, child.getName(), child.getType()));
                        } else {
                            rebuiltTree.addChild(updateBlobIfIndexed(child, childPath, indexedPaths));
                        }
                    } else if (!indexedPaths.contains(myGitDirectory.relativize(childPath))) {
                        rebuiltTree.addChild(child);
                    }
                    break;
                case Blob.TYPE:
                    final Blob childBlob = mapper.readBlob(child.getHash());
                    if (contained) {
                        filePaths.remove(childPath);
                        if (childPath.toFile().isDirectory()) {
                            if (indexedPaths.contains(myGitDirectory.relativize(childPath))) {
                                final String childHash = rebuildTree(null, childPath, indexedPaths);
                                rebuiltTree.addChild(new TreeEdge(childHash, child.getName(), Tree.TYPE));
                            } else {
                                rebuiltTree.addChild(child);
                            }
                        } else {
                            final byte[] committedContent = childBlob.getContent();
                            final byte[] currentContent = Files.readAllBytes(childPath);
                            if (Arrays.equals(committedContent, currentContent)) {
                                rebuiltTree.addChild(child);
                            } else {
                                rebuiltTree.addChild(updateBlobIfIndexed(child, childPath, indexedPaths));
                            }
                        }
                    } else if (!indexedPaths.contains(myGitDirectory.relativize(childPath))) {
                        rebuiltTree.addChild(child);
                    }
                    break;
                default:
                    throw new MyGitStateException("met an unknown type while traversing the tree -- " + child.getType());
            }
        }
        for (Path path : filePaths) {
            if (indexedPaths.contains(myGitDirectory.relativize(path))) {
                if (path.toFile().isDirectory()) {
                    final String treeHash = rebuildTree(null, path, indexedPaths);
                    rebuiltTree.addChild(new TreeEdge(treeHash, path.getFileName().toString(), Tree.TYPE));
                } else {
                    final String blobHash = mapper.createBlobFromPath(path);
                    rebuiltTree.addChild(new TreeEdge(blobHash, path.getFileName().toString(), Blob.TYPE));
                }
            }
        }
        return mapper.map(rebuiltTree);
    }

    @NotNull
    private TreeEdge updateBlobIfIndexed(@NotNull TreeEdge object, @NotNull Path path,
                                         @NotNull Set<Path> indexedPaths)
            throws MyGitStateException, IOException {
        if (indexedPaths.contains(myGitDirectory.relativize(path))) {
            final String blobHash = mapper.createBlobFromPath(path);
            return new TreeEdge(blobHash, object.getName(), Blob.TYPE);
        } else {
            return object;
        }
    }

    private boolean doesBranchExists(@NotNull String branchName) throws MyGitStateException, IOException {
        final List<Branch> branches = listBranches();
        return branches.contains(new Branch(branchName));
    }

    private void traverseCommitsTree(@NotNull Commit commit, @NotNull TreeSet<Commit> commitTree)
            throws MyGitStateException, IOException {
        if (!commitTree.contains(commit)) {
            commitTree.add(commit);
            for (String parentHash : commit.getParentsHashes()) {
                final Commit parentCommit = mapper.readCommit(parentHash);
                traverseCommitsTree(parentCommit, commitTree);
            }
        }
    }

    private void performUpdateToIndex(@NotNull List<String> arguments,
                                      @NotNull Function<Set<Path>, Consumer<Path>> action)
            throws MyGitStateException, MyGitIllegalArgumentException, IOException {
        final List<Path> argsPaths = convertStringsToPaths(arguments);
        final Set<Path> indexedPaths = mapper.readIndexPaths();
        final Consumer<Path> indexUpdater = action.apply(indexedPaths);
        argsPaths.forEach(indexUpdater);
        mapper.writeIndexPaths(indexedPaths);
    }

    @NotNull
    private List<Change> getChangeList(@Nullable Tree tree, @NotNull Path prefixPath, @NotNull Set<Path> indexedPaths)
            throws MyGitStateException, IOException {
        final List<Path> filePaths =
                Files
                        .list(prefixPath)
                        .filter(path -> !isAbsolutePathRepresentsInternal(path))
                        .collect(Collectors.toList());
        final List<Change> changes = new ArrayList<>();
        final List<TreeEdge> childrenList = tree == null ? new ArrayList<>() : tree.getChildren();
        for (TreeEdge child : childrenList) {
            final Path childPath = Paths.get(prefixPath.toString(), child.getName());
            boolean contained = filePaths.contains(childPath);
            switch (child.getType()) {
                case Tree.TYPE:
                    final Tree childTree = mapper.readTree(child.getHash());
                    if (contained) {
                        filePaths.remove(childPath);
                        if (childPath.toFile().isDirectory()) {
                            changes.addAll(getChangeList(childTree, childPath, indexedPaths));
                        } else {
                            if (indexedPaths.contains(myGitDirectory.relativize(childPath))) {
                                changes.add(new ChangeToBeCommitted(childPath, FileChangeType.MODIFICATION));
                                for (TreeEdge object : childTree.getChildren()) {
                                    final Path objectPath = Paths.get(childPath.toString(), object.getName());
                                    changes.add(new ChangeToBeCommitted(objectPath, FileChangeType.REMOVAL));
                                }
                            } else {
                                changes.add(new ChangeNotStagedForCommit(childPath, FileChangeType.MODIFICATION));
                                for (TreeEdge object : childTree.getChildren()) {
                                    final Path objectPath = Paths.get(childPath.toString(), object.getName());
                                    changes.add(new ChangeNotStagedForCommit(objectPath, FileChangeType.REMOVAL));
                                }
                            }
                        }
                    } else if (indexedPaths.contains(myGitDirectory.relativize(childPath))) {
                        changes.add(new ChangeToBeCommitted(childPath, FileChangeType.REMOVAL));
                    } else {
                        changes.add(new ChangeNotStagedForCommit(childPath, FileChangeType.REMOVAL));
                    }
                    break;
                case Blob.TYPE:
                    final Blob childBlob = mapper.readBlob(child.getHash());
                    if (contained) {
                        filePaths.remove(childPath);
                        if (childPath.toFile().isDirectory()) {
                            if (indexedPaths.contains(myGitDirectory.relativize(childPath))) {
                                changes.add(new ChangeToBeCommitted(childPath, FileChangeType.MODIFICATION));
                                changes.addAll(getChangeList(null, childPath, indexedPaths));
                            } else {
                                changes.add(new ChangeNotStagedForCommit(childPath, FileChangeType.MODIFICATION));
                            }
                        } else {
                            final byte[] committedContent = childBlob.getContent();
                            final byte[] currentContent = Files.readAllBytes(childPath);
                            if (!Arrays.equals(committedContent, currentContent)) {
                                if (indexedPaths.contains(myGitDirectory.relativize(childPath))) {
                                    changes.add(new ChangeToBeCommitted(childPath, FileChangeType.MODIFICATION));
                                } else {
                                    changes.add(new ChangeNotStagedForCommit(childPath, FileChangeType.MODIFICATION));
                                }
                            }
                        }
                    } else if (indexedPaths.contains(myGitDirectory.relativize(childPath))) {
                        changes.add(new ChangeToBeCommitted(childPath, FileChangeType.REMOVAL));
                    } else {
                        changes.add(new ChangeNotStagedForCommit(childPath, FileChangeType.REMOVAL));
                    }
                    break;
                default:
                    throw new MyGitStateException("met an unknown type while traversing the tree -- " + child.getType());
            }
        }
        for (Path path : filePaths) {
            if (indexedPaths.contains(myGitDirectory.relativize(path))) {
                changes.add(new ChangeToBeCommitted(path, FileChangeType.ADDITION));
                if (path.toFile().isDirectory()) {
                    changes.addAll(getChangeList(null, path, indexedPaths));
                }
            } else {
                changes.add(new UntrackedFile(path));
            }
        }
        return changes;
    }

    @NotNull
    private List<String> listCommitHashes() throws IOException, MyGitStateException {
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
            final Object object = mapper.readObject(objectHash);
            if (object instanceof Commit) {
                commitHashes.add(objectHash);
            }
        }
        return commitHashes;
    }

    @Nullable
    private Path findMyGitPath(@Nullable Path currentDirectory) {
        if (currentDirectory == null) {
            return null;
        }
        final Path possibleMyGitDirectory = Paths.get(currentDirectory.toString(), ".mygit");
        if (Files.exists(possibleMyGitDirectory)) {
            return currentDirectory;
        } else {
            return findMyGitPath(currentDirectory.getParent());
        }
    }

    @NotNull
    private List<Path> convertStringsToPaths(@NotNull List<String> args) throws MyGitIllegalArgumentException {
        final List<Path> paths = new ArrayList<>();
        for (String stringPath : args) {
            Path path;
            try {
                path = Paths.get(stringPath);
            } catch (InvalidPathException e) {
                throw new MyGitIllegalArgumentException("invalid path -- " + e.getMessage());
            }
            if (!path.isAbsolute()) {
                path = currentDirectory.resolve(path).normalize();
            }
            if (!path.startsWith(myGitDirectory)) {
                throw new MyGitIllegalArgumentException(
                        "files should be located in the mygit repository's directory, but an argument is " + path);
            }
            path = myGitDirectory.relativize(path);
            if (!pathContainsMyGitAsSubpath(path)) {
                paths.add(path);
            }
        }
        return paths;
    }

    @NotNull
    private String mergeTwoTrees(@NotNull Tree baseTree, @NotNull Tree otherTree)
            throws MyGitStateException, IOException {
        final Tree mergedTree = new Tree();
        final ListIterator<TreeEdge> baseIterator = baseTree.getChildren().listIterator();
        final ListIterator<TreeEdge> otherIterator = otherTree.getChildren().listIterator();
        while (baseIterator.hasNext() && otherIterator.hasNext()) {
            final TreeEdge baseTreeEdge = baseIterator.next();
            final TreeEdge otherTreeEdge = otherIterator.next();
            int comparison = baseTreeEdge.getName().compareTo(otherTreeEdge.getName());
            if (comparison == 0) {
                if (baseTreeEdge.isDirectory() && otherTreeEdge.isDirectory()) {
                    final Tree baseChildTree = mapper.readTree(baseTreeEdge.getName());
                    final Tree otherChildTree = mapper.readTree(otherTreeEdge.getName());
                    final String mergedTreeHash = mergeTwoTrees(baseChildTree, otherChildTree);
                    final TreeEdge mergedTreeEdge =
                            new TreeEdge(mergedTreeHash, baseTreeEdge.getName(), baseTreeEdge.getType());
                    mergedTree.addChild(mergedTreeEdge);
                } else {
                    mergedTree.addChild(
                            baseTreeEdge.getDateCreated().compareTo(otherTreeEdge.getDateCreated()) > 0
                            ? baseTreeEdge
                            : otherTreeEdge
                    );
                }
            } else if (comparison < 0) {
                mergedTree.addChild(baseTreeEdge);
                otherIterator.previous();
            } else {
                mergedTree.addChild(otherTreeEdge);
                baseIterator.previous();
            }
        }
        while (baseIterator.hasNext()) {
            mergedTree.addChild(baseIterator.next());
        }
        while (otherIterator.hasNext()) {
            mergedTree.addChild(otherIterator.next());
        }
        return mapper.map(mergedTree);
    }

    private boolean isAbsolutePathRepresentsInternal(@Nullable Path path) {
        return pathContainsMyGitAsSubpath(myGitDirectory.relativize(path));
    }

    private static boolean pathContainsMyGitAsSubpath(@Nullable Path path) {
        return path != null && (path.endsWith(".mygit") || pathContainsMyGitAsSubpath(path.getParent()));
    }
}
