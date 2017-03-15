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
import ru.spbau.sazanovich.nikita.mygit.objects.Tree.TreeObject;
import ru.spbau.sazanovich.nikita.mygit.status.*;
import ru.spbau.sazanovich.nikita.mygit.utils.Hasher;
import ru.spbau.sazanovich.nikita.mygit.utils.Mapper;

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

public class MyGitHandler {

    @NotNull
    private final Path myGitDirectory;

    @NotNull
    private final Mapper mapper;

    public MyGitHandler() throws MyGitException {
        final Path path = findMyGitPath(Paths.get("").toAbsolutePath());
        if (path == null) {
            throw new MyGitStateException("Not a mygit repository (or any of the parent directories)");
        }
        myGitDirectory = path;
        mapper = new Mapper(myGitDirectory);
    }

    @NotNull
    public Path getMyGitDirectory() {
        return myGitDirectory;
    }

    @NotNull
    public List<Change> getHeadChanges() throws MyGitStateException, IOException {
        final Tree tree = mapper.getHeadTree();
        final Set<Path> indexedPaths = mapper.readIndexPaths();
        final List<Change> changes = getChangeList(tree, myGitDirectory, indexedPaths);
        changes.forEach(change -> change.relativizePath(myGitDirectory));
        return changes;
    }

    public void addPathsToIndex(@NotNull List<String> arguments)
            throws MyGitStateException, MyGitIllegalArgumentException, IOException {
        final Function<Set<Path>, Consumer<Path>> action =
                paths -> (Consumer<Path>) path -> {
                    if (!paths.contains(path)) {
                        paths.add(path);
                    }
                };
        performUpdateToIndex(arguments, action);
    }

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


    public void resetAllIndexPaths() throws MyGitStateException, IOException {
        mapper.writeIndexPaths(new HashSet<>());
    }

    @NotNull
    public HeadStatus getHeadStatus() throws MyGitStateException, IOException {
        return mapper.getHeadStatus();
    }

    @NotNull
    public List<CommitLog> getLogsHistory() throws MyGitStateException, IOException {
        final Commit headCommit = mapper.getHeadCommit();
        final TreeSet<Commit> commitTree = new TreeSet<>();
        traverseCommitsTree(headCommit, commitTree);
        final List<CommitLog> logsHistory = new ArrayList<>();
        for (Commit commit : commitTree) {
            final CommitLog log =
                    new CommitLog(Hasher.getHashFromObject(commit), commit.getMessage(),
                            commit.getAuthor(), commit.getDateCreated());
            logsHistory.add(log);
        }
        Collections.reverse(logsHistory);
        return logsHistory;
    }

    public void checkout(@NotNull String revisionName)
            throws MyGitStateException, MyGitMissingPrerequisites, MyGitIllegalArgumentException, IOException {
        if (!mapper.readIndexPaths().isEmpty()) {
            throw new MyGitMissingPrerequisites("staging area should be empty before a checkout operation");
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

    public void mergeHeadWithBranch(@NotNull String otherBranch)
            throws MyGitMissingPrerequisites, MyGitStateException, IOException, MyGitIllegalArgumentException {
        final HeadStatus headStatus = mapper.getHeadStatus();
        if (headStatus.getName().equals(Commit.TYPE)) {
            throw new MyGitMissingPrerequisites("could not merge while you are in HEAD detached state");
        }
        if (!listBranches().contains(new Branch(otherBranch))) {
            throw new MyGitIllegalArgumentException("there is no such branch -- " + otherBranch);
        }
        if (headStatus.getName().equals(otherBranch)) {
            throw new MyGitIllegalArgumentException("can not merge branch with itself");
        }
        if (!mapper.readIndexPaths().isEmpty()) {
            throw new MyGitMissingPrerequisites("staging area should be empty before a merge operation");
        }
        final String baseBranch = headStatus.getName();
        final Tree baseTree = mapper.getBranchTree(baseBranch);
        final Tree otherTree = mapper.getBranchTree(otherBranch);

        final String mergeTreeHash = mergeTwoTrees(baseTree, otherTree);
        final List<String> parentsHashes = new ArrayList<>();
        parentsHashes.add(mapper.getBranchCommitHash(baseBranch));
        parentsHashes.add(mapper.getBranchCommitHash(otherBranch));
        final Commit mergeCommit = new Commit(mergeTreeHash, "merge commit", parentsHashes);
        final String mergeCommitHash = mapper.map(mergeCommit);
        mapper.writeBranch(baseBranch, mergeCommitHash);

        checkout("master");
    }

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

    public void createBranch(@NotNull String branchName)
            throws MyGitStateException, IOException, MyGitIllegalArgumentException {
        if (doesBranchExists(branchName)) {
            throw new MyGitIllegalArgumentException("'" + branchName + "' branch already exists");
        }
        mapper.writeBranch(branchName, mapper.getHeadCommitHash());
    }

    public void deleteBranch(@NotNull String branchName)
            throws MyGitIllegalArgumentException, IOException, MyGitStateException {
        if (!doesBranchExists(branchName)) {
            throw new MyGitIllegalArgumentException("'" + branchName + "' branch is missing");
        }
        final File branchFile = new File(myGitDirectory + "/.mygit/branches/" + branchName);
        Files.delete(branchFile.toPath());
    }

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
        final List<TreeObject> childrenList = tree == null ? new ArrayList<>() : tree.getChildren();
        for (TreeObject child : childrenList) {
            final Path childPath = Paths.get(prefixPath.toString(), child.getName());
            boolean contained = filePaths.contains(childPath);
            switch (child.getType()) {
                case Tree.TYPE:
                    final Tree childTree = mapper.readTree(child.getSha());
                    if (contained) {
                        filePaths.remove(childPath);
                        if (childPath.toFile().isDirectory()) {
                            final String childHash = rebuildTree(childTree, childPath, indexedPaths);
                            rebuiltTree.addChild(new TreeObject(childHash, child.getName(), child.getType()));
                        } else {
                            rebuiltTree.addChild(updateBlobIfIndexed(child, childPath, indexedPaths));
                        }
                    } else if (!indexedPaths.contains(childPath)) {
                        rebuiltTree.addChild(child);
                    }
                    break;
                case Blob.TYPE:
                    final Blob childBlob = mapper.readBlob(child.getSha());
                    if (contained) {
                        filePaths.remove(childPath);
                        if (childPath.toFile().isDirectory()) {
                            if (indexedPaths.contains(childPath)) {
                                final String childHash = rebuildTree(null, childPath, indexedPaths);
                                rebuiltTree.addChild(new TreeObject(childHash, child.getName(), Tree.TYPE));
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
                    } else if (!indexedPaths.contains(childPath)) {
                        rebuiltTree.addChild(child);
                    }
                    break;
                default:
                    throw new MyGitStateException("met an unknown type while traversing the tree -- " + child.getType());
            }
        }
        for (Path path : filePaths) {
            if (indexedPaths.contains(path)) {
                if (path.toFile().isDirectory()) {
                    final String treeHash = rebuildTree(null, path, indexedPaths);
                    rebuiltTree.addChild(new TreeObject(treeHash, path.getFileName().toString(), Tree.TYPE));
                } else {
                    final String blobHash = mapper.createBlobFromPath(path);
                    rebuiltTree.addChild(new TreeObject(blobHash, path.getFileName().toString(), Blob.TYPE));
                }
            }
        }
        return mapper.map(rebuiltTree);
    }

    @NotNull
    private TreeObject updateBlobIfIndexed(@NotNull TreeObject object, @NotNull Path path,
                                           @NotNull Set<Path> indexedPaths)
            throws MyGitStateException, IOException {
        if (indexedPaths.contains(path)) {
            final String blobHash = mapper.createBlobFromPath(path);
            return new TreeObject(blobHash, object.getName(), Blob.TYPE);
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
        final List<TreeObject> childrenList = tree == null ? new ArrayList<>() : tree.getChildren();
        for (TreeObject child : childrenList) {
            final Path childPath = Paths.get(prefixPath.toString(), child.getName());
            boolean contained = filePaths.contains(childPath);
            switch (child.getType()) {
                case Tree.TYPE:
                    final Tree childTree = mapper.readTree(child.getSha());
                    if (contained) {
                        filePaths.remove(childPath);
                        if (childPath.toFile().isDirectory()) {
                            changes.addAll(getChangeList(childTree, childPath, indexedPaths));
                        } else {
                            if (indexedPaths.contains(childPath)) {
                                changes.add(new ChangeToBeCommitted(childPath, FileChangeType.MODIFICATION));
                                for (TreeObject object : childTree.getChildren()) {
                                    final Path objectPath = Paths.get(childPath.toString(), object.getName());
                                    changes.add(new ChangeToBeCommitted(objectPath, FileChangeType.REMOVAL));
                                }
                            } else {
                                changes.add(new ChangeNotStagedForCommit(childPath, FileChangeType.MODIFICATION));
                                for (TreeObject object : childTree.getChildren()) {
                                    final Path objectPath = Paths.get(childPath.toString(), object.getName());
                                    changes.add(new ChangeNotStagedForCommit(objectPath, FileChangeType.REMOVAL));
                                }
                            }
                        }
                    } else if (indexedPaths.contains(childPath)) {
                        changes.add(new ChangeToBeCommitted(childPath, FileChangeType.REMOVAL));
                    } else {
                        changes.add(new ChangeNotStagedForCommit(childPath, FileChangeType.REMOVAL));
                    }
                    break;
                case Blob.TYPE:
                    final Blob childBlob = mapper.readBlob(child.getSha());
                    if (contained) {
                        filePaths.remove(childPath);
                        if (childPath.toFile().isDirectory()) {
                            if (indexedPaths.contains(childPath)) {
                                changes.add(new ChangeToBeCommitted(childPath, FileChangeType.MODIFICATION));
                                changes.addAll(getChangeList(null, childPath, indexedPaths));
                            } else {
                                changes.add(new ChangeNotStagedForCommit(childPath, FileChangeType.MODIFICATION));
                            }
                        } else {
                            final byte[] committedContent = childBlob.getContent();
                            final byte[] currentContent = Files.readAllBytes(childPath);
                            if (!Arrays.equals(committedContent, currentContent)) {
                                if (indexedPaths.contains(childPath)) {
                                    changes.add(new ChangeToBeCommitted(childPath, FileChangeType.MODIFICATION));
                                } else {
                                    changes.add(new ChangeNotStagedForCommit(childPath, FileChangeType.MODIFICATION));
                                }
                            }
                        }
                    } else if (indexedPaths.contains(childPath)) {
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
            if (indexedPaths.contains(path)) {
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
            path = path.toAbsolutePath();
            if (!path.startsWith(myGitDirectory)) {
                throw new MyGitIllegalArgumentException(
                        "files should be located in the mygit repository's directory, but an argument is " + path);
            }
            if (!isAbsolutePathRepresentsInternal(path)) {
                paths.add(path);
            }
        }
        return paths;
    }

    @NotNull
    private String mergeTwoTrees(@NotNull Tree baseTree, @NotNull Tree otherTree)
            throws MyGitStateException, IOException {
        final Tree mergedTree = new Tree();
        final ListIterator<TreeObject> baseIterator = baseTree.getChildren().listIterator();
        final ListIterator<TreeObject> otherIterator = otherTree.getChildren().listIterator();
        while (baseIterator.hasNext() && otherIterator.hasNext()) {
            final TreeObject baseTreeObject = baseIterator.next();
            final TreeObject otherTreeObject = otherIterator.next();
            int comparison = baseTreeObject.getName().compareTo(otherTreeObject.getName());
            if (comparison == 0) {
                if (baseTreeObject.isDirectory() && otherTreeObject.isDirectory()) {
                    final Tree baseChildTree = mapper.readTree(baseTreeObject.getName());
                    final Tree otherChildTree = mapper.readTree(otherTreeObject.getName());
                    final String mergedTreeHash = mergeTwoTrees(baseChildTree, otherChildTree);
                    final TreeObject mergedTreeObject =
                            new TreeObject(mergedTreeHash, baseTreeObject.getName(), baseTreeObject.getType());
                    mergedTree.addChild(mergedTreeObject);
                } else {
                    mergedTree.addChild(
                            baseTreeObject.getDateCreated().compareTo(otherTreeObject.getDateCreated()) <= 0
                            ? baseTreeObject
                            : otherTreeObject
                    );
                }
            } else if (comparison < 0) {
                mergedTree.addChild(baseTreeObject);
                otherIterator.previous();
            } else {
                mergedTree.addChild(otherTreeObject);
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
