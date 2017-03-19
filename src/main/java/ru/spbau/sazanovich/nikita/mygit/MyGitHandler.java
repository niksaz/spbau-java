package ru.spbau.sazanovich.nikita.mygit;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbau.sazanovich.nikita.mygit.objects.*;
import ru.spbau.sazanovich.nikita.mygit.objects.Tree.TreeEdge;
import ru.spbau.sazanovich.nikita.mygit.utils.SHA1Hasher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.spbau.sazanovich.nikita.mygit.objects.FileDifferenceStageStatus.*;
import static ru.spbau.sazanovich.nikita.mygit.objects.FileDifferenceType.*;

/**
 * Class which should be instantiated by a user to interact with the library. Handles command and delegating internal
 * representation changes to {@link InternalStateAccessor}.
 */
public class MyGitHandler {

    @NotNull
    private final Path myGitDirectory;
    @NotNull
    private final Path currentDirectory;
    @NotNull
    private final InternalStateAccessor internalStateAccessor;

    /**
     * Initialize MyGit repository in a given directory.
     *
     * @param directory an absolute path to a directory to initialize MyGit in
     * @throws MyGitIllegalArgumentException    if the directory path is not absolute
     * @throws MyGitAlreadyInitializedException if the directory already contains .mygit file
     * @throws MyGitStateException              if an internal error occurs during operations
     * @throws IOException                      if an error occurs during working with a filesystem
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void init(@NotNull Path directory)
            throws MyGitIllegalArgumentException, MyGitAlreadyInitializedException, MyGitStateException, IOException {
        if (!directory.isAbsolute()) {
            throw new MyGitIllegalArgumentException("path parameter should be an absolute");
        }
        InternalStateAccessor.init(directory);
    }

    /**
     * Constructs a handler in a given directory.
     *
     * @param currentDirectory a path to the current directory for a handler
     * @throws MyGitIllegalArgumentException if the directory path is not absolute
     * @throws MyGitStateException           if the directory (or any of the parent directories) is not a MyGit repository
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
        internalStateAccessor = new InternalStateAccessor(myGitDirectory, new SHA1Hasher());
    }

    /**
     * Gets differences between current MyGit repository's HEAD state and the filesystem state.
     *
     * @return list of differences between MyGit repository's HEAD state and the filesystem state
     * @throws MyGitStateException if an internal error occurs during operations
     * @throws IOException         if an error occurs during working with a filesystem
     */
    @NotNull
    public List<FileDifference> getHeadDifferences() throws MyGitStateException, IOException {
        final Tree tree = internalStateAccessor.getHeadTree();
        final Set<Path> indexedPaths = internalStateAccessor.readIndexPaths();
        return getTreeDifferenceList(tree, myGitDirectory, indexedPaths);
    }

    /**
     * Adds paths to the current index.
     *
     * @param arguments list of paths to add to the index
     * @throws MyGitIllegalArgumentException if the list contains incorrect/located outside MyGit repository paths
     * @throws MyGitStateException           if an internal error occurs during operations
     * @throws IOException                   if an error occurs during working with a filesystem
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
     * @throws MyGitStateException           if an internal error occurs during operations
     * @throws IOException                   if an error occurs during working with a filesystem
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

    private void performUpdateToIndex(@NotNull List<String> arguments,
                                      @NotNull Function<Set<Path>, Consumer<Path>> action)
            throws MyGitStateException, MyGitIllegalArgumentException, IOException {
        final List<Path> argsPaths = convertStringsToPaths(arguments);
        final Set<Path> indexedPaths = internalStateAccessor.readIndexPaths();
        final Consumer<Path> indexUpdater = action.apply(indexedPaths);
        argsPaths.forEach(indexUpdater);
        internalStateAccessor.writeIndexPaths(indexedPaths);
    }

    /**
     * Removes all paths from the current index.
     *
     * @throws MyGitStateException if an internal error occurs during operations
     * @throws IOException         if an error occurs during working with a filesystem
     */
    public void resetAllIndexPaths() throws MyGitStateException, IOException {
        internalStateAccessor.writeIndexPaths(new HashSet<>());
    }

    /**
     * Gets the current HEAD state.
     *
     * @return a head state
     * @throws MyGitStateException if an internal error occurs during operations
     * @throws IOException         if an error occurs during working with a filesystem
     */
    @NotNull
    public HeadStatus getHeadStatus() throws MyGitStateException, IOException {
        return internalStateAccessor.getHeadStatus();
    }

    /**
     * Gets the logs from commit's which are reachable from the HEAD's commit.
     *
     * @return list of commit's logs
     * @throws MyGitStateException if an internal error occurs during operations
     * @throws IOException         if an error occurs during working with a filesystem
     */
    @NotNull
    public List<CommitLog> getCommitsLogsHistory() throws MyGitStateException, IOException {
        final Commit headCommit = internalStateAccessor.getHeadCommit();
        final TreeSet<Commit> commitTree = new TreeSet<>();
        traverseCommitsTree(headCommit, commitTree);
        final List<CommitLog> logsHistory = new ArrayList<>();
        for (Commit commit : commitTree) {
            final CommitLog log =
                    new CommitLog(internalStateAccessor.getObjectHash(commit), commit.getMessage(),
                            commit.getAuthor(), commit.getDateCreated());
            logsHistory.add(log);
        }
        Collections.reverse(logsHistory);
        return logsHistory;
    }

    /**
     * Checkouts a revision and moves HEAD there.
     * <p>
     * Replaces all files which differs from the HEAD's ones if such exist. Index should be empty before checking out.
     *
     * @param revisionName a name of revision
     * @throws MyGitMissingPrerequisitesException if the index is not empty
     * @throws MyGitIllegalArgumentException      if the revision does not exist
     * @throws MyGitStateException                if an internal error occurs during operations
     * @throws IOException                        if an error occurs during working with a filesystem
     */
    public void checkout(@NotNull String revisionName)
            throws MyGitStateException, MyGitMissingPrerequisitesException, MyGitIllegalArgumentException, IOException {
        if (!internalStateAccessor.readIndexPaths().isEmpty()) {
            throw new MyGitMissingPrerequisitesException("staging area should be empty before a checkout operation");
        }
        final Commit fromCommit = internalStateAccessor.getHeadCommit();
        String toCommitHash;
        String toHeadType;
        if (listBranches().contains(new Branch(revisionName))) {
            toCommitHash = internalStateAccessor.getBranchCommitHash(revisionName);
            toHeadType = Branch.TYPE;
        } else {
            if (listCommitHashes().contains(revisionName)) {
                toCommitHash = revisionName;
                toHeadType = Commit.TYPE;
            } else {
                throw new MyGitIllegalArgumentException("there is no such revision -- " + revisionName);
            }
        }
        final Commit toCommit = internalStateAccessor.readCommit(toCommitHash);
        internalStateAccessor.moveFromCommitToCommit(fromCommit, toCommit);
        final HeadStatus toHeadStatus = new HeadStatus(toHeadType, revisionName);
        internalStateAccessor.setHeadStatus(toHeadStatus);
    }

    /**
     * Perform a merge operation of HEAD and given branch.
     * <p>
     * Chooses files based on their last modification's date -- older have a priority. Index should be empty before
     * merging and HEAD should not be in detached state.
     *
     * @param branch branch with which to merge HEAD
     * @throws MyGitMissingPrerequisitesException if the index is not empty or currently in a detached HEAD state
     * @throws MyGitIllegalArgumentException      if there is no such branch or a user tries to merge branch with itself
     * @throws MyGitStateException                if an internal error occurs during operations
     * @throws IOException                        if an error occurs during working with a filesystem
     */
    public void mergeHeadWithBranch(@NotNull String branch)
            throws MyGitMissingPrerequisitesException, MyGitStateException, IOException, MyGitIllegalArgumentException {
        final HeadStatus headStatus = internalStateAccessor.getHeadStatus();
        if (headStatus.getName().equals(Commit.TYPE)) {
            throw new MyGitMissingPrerequisitesException("could not merge while you are in detached HEAD state");
        }
        if (!listBranches().contains(new Branch(branch))) {
            throw new MyGitIllegalArgumentException("there is no such branch -- " + branch);
        }
        if (headStatus.getName().equals(branch)) {
            throw new MyGitIllegalArgumentException("can not merge branch with itself");
        }
        if (!internalStateAccessor.readIndexPaths().isEmpty()) {
            throw new MyGitMissingPrerequisitesException("staging area should be empty before a merge operation");
        }
        final String baseBranch = headStatus.getName();
        final Tree baseTree = internalStateAccessor.getBranchTree(baseBranch);
        final Tree otherTree = internalStateAccessor.getBranchTree(branch);

        final String mergeTreeHash = mergeTwoTrees(baseTree, otherTree);
        final List<String> parentsHashes = new ArrayList<>();
        parentsHashes.add(internalStateAccessor.getBranchCommitHash(baseBranch));
        parentsHashes.add(internalStateAccessor.getBranchCommitHash(branch));
        final Commit mergeCommit = new Commit(mergeTreeHash, "merge commit", parentsHashes);
        final String mergeCommitHash = internalStateAccessor.map(mergeCommit);
        internalStateAccessor.writeBranch(baseBranch, mergeCommitHash);

        checkout(baseBranch);
    }

    /**
     * Gets list of branches in MyGit repository.
     *
     * @return list of branches
     * @throws MyGitStateException if an internal error occurs during operations
     * @throws IOException         if an error occurs during working with a filesystem
     */
    @NotNull
    public List<Branch> listBranches() throws MyGitStateException, IOException {
        return internalStateAccessor.listBranches();
    }

    /**
     * Creates a new branch with the given name.
     *
     * @param branchName branch name for a new branch
     * @throws MyGitIllegalArgumentException if the branch with the name already exists
     * @throws MyGitStateException           if an internal error occurs during operations
     * @throws IOException                   if an error occurs during working with a filesystem
     */
    public void createBranch(@NotNull String branchName)
            throws MyGitStateException, IOException, MyGitIllegalArgumentException {
        if (doesBranchExists(branchName)) {
            throw new MyGitIllegalArgumentException("'" + branchName + "' branch already exists");
        }
        internalStateAccessor.writeBranch(branchName, internalStateAccessor.getHeadCommitHash());
    }

    /**
     * Removes the branch with the given name.
     *
     * @param branchName the name of the branch to delete
     * @throws MyGitIllegalArgumentException if there is no a branch with this name or
     *                                       this branch is currently checked out
     * @throws MyGitStateException           if an internal error occurs during operations
     * @throws IOException                   if an error occurs during working with a filesystem
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
        internalStateAccessor.deleteBranch(branchName);
    }

    /**
     * Commits all indexed files and moves head to the newly created commit.
     *
     * @param message commit's message
     * @throws MyGitStateException if an internal error occurs during operations
     * @throws IOException         if an error occurs during working with a filesystem
     */
    public void commitWithMessage(@NotNull String message) throws MyGitStateException, IOException {
        final Tree tree = internalStateAccessor.getHeadTree();
        final Set<Path> indexedPaths = internalStateAccessor.readIndexPaths();
        final String rebuiltTreeHash = rebuildTree(tree, myGitDirectory, indexedPaths);
        final List<String> parentsHashes = new ArrayList<>();
        parentsHashes.add(internalStateAccessor.getHeadCommitHash());
        final Commit commit = new Commit(rebuiltTreeHash, message, parentsHashes);
        final String commitHash = internalStateAccessor.map(commit);
        internalStateAccessor.moveHeadToCommitHash(commitHash);
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
            if (filePaths.contains(childPath)) {
                filePaths.remove(childPath);
            }
            final TreeEdge rebuiltTreeEdge = rebuildTreeEdge(child, childPath, indexedPaths);
            if (rebuiltTreeEdge != null) {
                rebuiltTree.addChild(rebuiltTreeEdge);
            }
        }
        for (Path path : filePaths) {
            if (indexedPaths.contains(myGitDirectory.relativize(path))) {
                if (Files.isDirectory(path)) {
                    final String treeHash = rebuildTree(null, path, indexedPaths);
                    rebuiltTree.addChild(new TreeEdge(treeHash, path.getFileName().toString(), Tree.TYPE));
                } else {
                    final String blobHash = internalStateAccessor.createBlobFromPath(path);
                    rebuiltTree.addChild(new TreeEdge(blobHash, path.getFileName().toString(), Blob.TYPE));
                }
            }
        }
        return internalStateAccessor.map(rebuiltTree);
    }

    @Nullable
    private TreeEdge rebuildTreeEdge(@NotNull TreeEdge child, @NotNull Path path,
                                     @NotNull Set<Path> indexedPaths)
            throws MyGitStateException, IOException {
        final boolean isPresentInFilesystem = Files.exists(path);
        switch (child.getType()) {
            case Tree.TYPE:
                final Tree childTree = internalStateAccessor.readTree(child.getHash());
                if (isPresentInFilesystem) {
                    if (path.toFile().isDirectory()) {
                        final String childHash = rebuildTree(childTree, path, indexedPaths);
                        return new TreeEdge(childHash, child.getName(), child.getType());
                    } else {
                        return updateBlobIfIndexed(child, path, indexedPaths);
                    }
                } else if (!indexedPaths.contains(myGitDirectory.relativize(path))) {
                    return child;
                }
                break;
            case Blob.TYPE:
                final Blob childBlob = internalStateAccessor.readBlob(child.getHash());
                if (isPresentInFilesystem) {
                    if (path.toFile().isDirectory()) {
                        if (indexedPaths.contains(myGitDirectory.relativize(path))) {
                            final String childHash = rebuildTree(null, path, indexedPaths);
                            return new TreeEdge(childHash, child.getName(), Tree.TYPE);
                        } else {
                            return child;
                        }
                    } else {
                        final byte[] committedContent = childBlob.getContent();
                        final byte[] currentContent = Files.readAllBytes(path);
                        if (Arrays.equals(committedContent, currentContent)) {
                            return child;
                        } else {
                            return updateBlobIfIndexed(child, path, indexedPaths);
                        }
                    }
                } else if (!indexedPaths.contains(myGitDirectory.relativize(path))) {
                    return child;
                }
                break;
            default:
                throw new MyGitStateException("met an unknown type while traversing the tree -- " + child.getType());
        }
        return null;
    }

    @NotNull
    private TreeEdge updateBlobIfIndexed(@NotNull TreeEdge object, @NotNull Path path,
                                         @NotNull Set<Path> indexedPaths)
            throws MyGitStateException, IOException {
        if (indexedPaths.contains(myGitDirectory.relativize(path))) {
            final String blobHash = internalStateAccessor.createBlobFromPath(path);
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
                final Commit parentCommit = internalStateAccessor.readCommit(parentHash);
                traverseCommitsTree(parentCommit, commitTree);
            }
        }
    }

    /**
     * Returns a list of differences in files between filesystem and MyGit's HEAD status.
     * The list contains relative paths.
     */
    @NotNull
    private List<FileDifference> getTreeDifferenceList(@Nullable Tree tree, @NotNull Path prefixPath, @NotNull Set<Path> indexedPaths)
            throws MyGitStateException, IOException {
        final List<Path> filePaths =
                Files
                        .list(prefixPath)
                        .filter(path -> !isAbsolutePathRepresentsInternal(path))
                        .collect(Collectors.toList());
        final List<FileDifference> fileDifferenceList = new ArrayList<>();
        final List<TreeEdge> childrenList = tree == null ? new ArrayList<>() : tree.getChildren();
        for (TreeEdge child : childrenList) {
            final Path childPath = Paths.get(prefixPath.toString(), child.getName());
            if (filePaths.contains(childPath)) {
                filePaths.remove(childPath);
            }
            addChildDifferenceToList(child, childPath, indexedPaths, fileDifferenceList);
        }
        for (Path path : filePaths) {
            final Path relativePath = myGitDirectory.relativize(path);
            if (indexedPaths.contains(relativePath)) {
                fileDifferenceList.add(new FileDifference(relativePath, ADDITION, TO_BE_COMMITTED));
                if (Files.isDirectory(path)) {
                    fileDifferenceList.addAll(getTreeDifferenceList(null, path, indexedPaths));
                }
            } else {
                fileDifferenceList.add(new FileDifference(relativePath, ADDITION, UNTRACKED));
            }
        }
        return fileDifferenceList;
    }

    private void addChildDifferenceToList(@NotNull TreeEdge child, @NotNull Path path,
                                          @NotNull Set<Path> indexedPaths,
                                          @NotNull List<FileDifference> differences)
            throws MyGitStateException, IOException {
        final Path relativePath = myGitDirectory.relativize(path);
        final boolean isPresentInFilesystem = Files.exists(path);
        if (child.getType().equals(Tree.TYPE)) {
            final Tree childTree = internalStateAccessor.readTree(child.getHash());
            if (isPresentInFilesystem) {
                if (Files.isDirectory(path)) {
                    differences.addAll(getTreeDifferenceList(childTree, path, indexedPaths));
                } else {
                    if (indexedPaths.contains(relativePath)) {
                        differences.add(new FileDifference(relativePath, MODIFICATION, TO_BE_COMMITTED));
                        for (TreeEdge object : childTree.getChildren()) {
                            Path objectPath = Paths.get(path.toString(), object.getName());
                            objectPath = myGitDirectory.relativize(objectPath);
                            differences.add(new FileDifference(objectPath, REMOVAL, TO_BE_COMMITTED));
                        }
                    } else {
                        differences.add(new FileDifference(relativePath, MODIFICATION, NOT_STAGED_FOR_COMMIT));
                        for (TreeEdge object : childTree.getChildren()) {
                            Path objectPath = Paths.get(path.toString(), object.getName());
                            objectPath = myGitDirectory.relativize(objectPath);
                            differences.add(new FileDifference(objectPath, REMOVAL, NOT_STAGED_FOR_COMMIT));
                        }
                    }
                }
            } else if (indexedPaths.contains(relativePath)) {
                differences.add(new FileDifference(relativePath, REMOVAL, TO_BE_COMMITTED));
            } else {
                differences.add(new FileDifference(relativePath, REMOVAL, NOT_STAGED_FOR_COMMIT));
            }
            return;
        }
        if (child.getType().equals(Blob.TYPE)) {
            final Blob childBlob = internalStateAccessor.readBlob(child.getHash());
            if (isPresentInFilesystem) {
                if (Files.isDirectory(path)) {
                    if (indexedPaths.contains(relativePath)) {
                        differences.add(new FileDifference(relativePath, MODIFICATION, TO_BE_COMMITTED));
                        differences.addAll(getTreeDifferenceList(null, path, indexedPaths));
                    } else {
                        differences.add(new FileDifference(relativePath, MODIFICATION, NOT_STAGED_FOR_COMMIT));
                    }
                } else {
                    final byte[] committedContent = childBlob.getContent();
                    final byte[] currentContent = Files.readAllBytes(path);
                    if (!Arrays.equals(committedContent, currentContent)) {
                        if (indexedPaths.contains(relativePath)) {
                            differences.add(new FileDifference(relativePath, MODIFICATION, TO_BE_COMMITTED));
                        } else {
                            differences.add(new FileDifference(relativePath, MODIFICATION, NOT_STAGED_FOR_COMMIT));
                        }
                    }
                }
            } else if (indexedPaths.contains(relativePath)) {
                differences.add(new FileDifference(relativePath, REMOVAL, TO_BE_COMMITTED));
            } else {
                differences.add(new FileDifference(relativePath, REMOVAL, NOT_STAGED_FOR_COMMIT));
            }
            return;
        }
        throw new MyGitStateException("met an unknown type while traversing the tree -- " + child.getType());
    }

    @NotNull
    private List<String> listCommitHashes() throws IOException, MyGitStateException {
        return internalStateAccessor.listCommitHashes();
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
                    final Tree baseChildTree = internalStateAccessor.readTree(baseTreeEdge.getName());
                    final Tree otherChildTree = internalStateAccessor.readTree(otherTreeEdge.getName());
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
        return internalStateAccessor.map(mergedTree);
    }

    private boolean isAbsolutePathRepresentsInternal(@Nullable Path path) {
        return pathContainsMyGitAsSubpath(myGitDirectory.relativize(path));
    }

    private static boolean pathContainsMyGitAsSubpath(@Nullable Path path) {
        return path != null && (path.endsWith(".mygit") || pathContainsMyGitAsSubpath(path.getParent()));
    }
}
