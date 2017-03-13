package ru.spbau.sazanovich.nikita.mygit;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbau.sazanovich.nikita.mygit.exceptions.MyGitException;
import ru.spbau.sazanovich.nikita.mygit.exceptions.MyGitIllegalArgumentException;
import ru.spbau.sazanovich.nikita.mygit.exceptions.MyGitMissingPrerequisites;
import ru.spbau.sazanovich.nikita.mygit.exceptions.MyGitStateException;
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
import java.nio.file.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public void resetPaths(@NotNull List<String> arguments)
            throws MyGitStateException, MyGitIllegalArgumentException, IOException {
        final Function<Set<Path>, Consumer<Path>> action =
                paths -> (Consumer<Path>) path -> {
                    if (paths.contains(path)) {
                        paths.remove(path);
                    }
                };
        performUpdateToIndex(arguments, action);
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

    public boolean checkout(@NotNull String revisionName)
            throws MyGitStateException, IOException, MyGitMissingPrerequisites, MyGitIllegalArgumentException {
        if (!mapper.readIndexPaths().isEmpty()) {
            throw new MyGitMissingPrerequisites("staging area should be empty before a checkout operation");
        }
        final HeadStatus headStatus = mapper.getHeadStatus();
        String fromCommitHash;
        if (headStatus.getType().equals(Branch.TYPE)) {
            fromCommitHash = mapper.getBranchCommitHash(headStatus.getName());
        } else {
            fromCommitHash = headStatus.getName();
        }
        String toCommitHash;
        if (listBranches().contains(new Branch(revisionName))) {
            toCommitHash = mapper.getBranchCommitHash(revisionName);
        } else {
            if (listCommitHashes().contains(revisionName)) {
                toCommitHash = revisionName;
            } else {
                throw new MyGitIllegalArgumentException("there is no such revision -- " + revisionName);
            }
        }
        if (fromCommitHash.equals(toCommitHash)) {
            return false;
        } else {
            final Commit fromCommit = mapper.readCommit(fromCommitHash);
            final Commit toCommit = mapper.readCommit(toCommitHash);
            mapper.moveFromCommitToCommit(fromCommit, toCommit);
            return true;
        }
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
        final HeadStatus headStatus = mapper.getHeadStatus();
        if (headStatus.getType().equals(Branch.TYPE)) {
            final String branchCommitHash = mapper.getBranchCommitHash(headStatus.getName());
            mapper.writeBranch(branchName, branchCommitHash);
        } else {
            mapper.writeBranch(branchName, headStatus.getName());
        }
    }

    public void deleteBranch(@NotNull String branchName)
            throws MyGitIllegalArgumentException, IOException, MyGitStateException {
        if (!doesBranchExists(branchName)) {
            throw new MyGitIllegalArgumentException("'" + branchName + "' branch is missing");
        }
        final File branchFile = new File(myGitDirectory + "/.mygit/branches/" + branchName);
        Files.delete(branchFile.toPath());
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
        final File directory = prefixPath.toFile();
        final File[] arrayOfFiles = directory.listFiles();
        if (arrayOfFiles == null) {
            throw new IOException("cannot read directory -- " + directory);
        }
        final List<Path> filePaths =
                Arrays
                .stream(arrayOfFiles)
                .map(File::toPath)
                .filter(path -> !isAbsolutePathRepresentsInternal(path))
                .collect(Collectors.toList());

        final List<Change> changes = new ArrayList<>();
        final List<TreeObject> childrenList = tree == null ? new ArrayList<>() : tree.getChildren();
        // TODO: CHECK THIS ONE MORE TIME
        for (TreeObject child : childrenList) {
            final Path childPath = Paths.get(prefixPath.toString(), child.getName());
            final File childFile = childPath.toFile();
            boolean contained = filePaths.contains(childPath);
            switch (child.getType()) {
                case Tree.TYPE:
                    final Tree childTree = mapper.readTree(child.getSha());
                    if (contained) {
                        filePaths.remove(childPath);
                        if (childFile.isDirectory()) {
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
                        if (childFile.isDirectory()) {
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

    private boolean isAbsolutePathRepresentsInternal(@Nullable Path path) {
        return pathContainsMyGitAsSubpath(myGitDirectory.relativize(path));
    }

    private static boolean pathContainsMyGitAsSubpath(@Nullable Path path) {
        return path != null && (path.endsWith(".mygit") || pathContainsMyGitAsSubpath(path.getParent()));
    }
}
