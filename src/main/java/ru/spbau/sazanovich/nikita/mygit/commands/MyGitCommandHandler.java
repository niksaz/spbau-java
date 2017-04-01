package ru.spbau.sazanovich.nikita.mygit.commands;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbau.sazanovich.nikita.mygit.MyGitAlreadyInitializedException;
import ru.spbau.sazanovich.nikita.mygit.MyGitIllegalArgumentException;
import ru.spbau.sazanovich.nikita.mygit.MyGitMissingPrerequisitesException;
import ru.spbau.sazanovich.nikita.mygit.MyGitStateException;
import ru.spbau.sazanovich.nikita.mygit.objects.*;
import ru.spbau.sazanovich.nikita.mygit.objects.Tree.TreeEdge;
import ru.spbau.sazanovich.nikita.mygit.utils.SHA1Hasher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Class which should be instantiated by a user to interact with the library. Handles command and delegating internal
 * representation changes to {@link InternalStateAccessor}.
 */
public class MyGitCommandHandler {

    @NotNull
    private final Path myGitDirectory;
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
    public static void init(@NotNull Path directory)
            throws MyGitIllegalArgumentException, MyGitAlreadyInitializedException, MyGitStateException, IOException {
        new InitCommand(directory).perform();
    }

    /**
     * Constructs a handler in a given directory.
     *
     * @param currentDirectory a path to the current directory for a handler
     * @throws MyGitIllegalArgumentException if the directory path is not absolute
     * @throws MyGitStateException           if the directory (or any of the parent directories) is not a MyGit repository
     */
    public MyGitCommandHandler(@NotNull Path currentDirectory) throws MyGitIllegalArgumentException, MyGitStateException {
        if (!currentDirectory.isAbsolute()) {
            throw new MyGitIllegalArgumentException("parameter should be an absolute path");
        }
        final Path path = InternalStateAccessor.findMyGitDirectoryPath(currentDirectory);
        if (path == null) {
            throw new MyGitStateException("Not a mygit repository (or any of the parent directories)");
        }
        myGitDirectory = path;
        internalStateAccessor = new InternalStateAccessor(myGitDirectory, currentDirectory, new SHA1Hasher());
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
        return new StatusCommand(internalStateAccessor).perform();
    }

    /**
     * Adds paths to the current index.
     *
     * @param arguments list of paths to add to the index
     * @throws MyGitIllegalArgumentException if the list contains incorrect/located outside MyGit repository paths
     * @throws MyGitStateException           if an internal error occurs during operations
     * @throws IOException                   if an error occurs during working with a filesystem
     */
    public void stagePaths(@NotNull List<String> arguments)
            throws MyGitIllegalArgumentException, MyGitStateException, IOException {
        new StageCommand(arguments, internalStateAccessor).perform();
    }

    /**
     * Removes paths from the current index.
     *
     * @param arguments list of paths to remove from the index
     * @throws MyGitIllegalArgumentException if the list contains incorrect/located outside MyGit repository paths
     * @throws MyGitStateException           if an internal error occurs during operations
     * @throws IOException                   if an error occurs during working with a filesystem
     */
    public void unstagePaths(@NotNull List<String> arguments)
            throws MyGitStateException, MyGitIllegalArgumentException, IOException {
        new UnstageCommand(arguments, internalStateAccessor).perorm();
    }

    /**
     * Removes all paths from the current index.
     *
     * @throws MyGitStateException if an internal error occurs during operations
     * @throws IOException         if an error occurs during working with a filesystem
     */
    public void unstageAllPaths() throws MyGitStateException, IOException {
        new UnstageAllCommand(internalStateAccessor).perform();
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
        unstageAllPaths();
    }

    private String rebuildTree(@Nullable Tree tree, @NotNull Path prefixPath, @NotNull Set<Path> indexedPaths)
            throws MyGitStateException, IOException {
        final List<Path> filePaths =
                Files
                        .list(prefixPath)
                        .filter(path -> !internalStateAccessor.isAbsolutePathRepresentsInternal(path))
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

    @NotNull
    private List<String> listCommitHashes() throws IOException, MyGitStateException {
        return internalStateAccessor.listCommitHashes();
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
}
