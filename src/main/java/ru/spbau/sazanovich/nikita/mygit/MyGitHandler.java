package ru.spbau.sazanovich.nikita.mygit;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbau.sazanovich.nikita.mygit.exceptions.MyGitException;
import ru.spbau.sazanovich.nikita.mygit.exceptions.MyGitIllegalArgumentException;
import ru.spbau.sazanovich.nikita.mygit.exceptions.MyGitStateException;
import ru.spbau.sazanovich.nikita.mygit.logs.CommitLog;
import ru.spbau.sazanovich.nikita.mygit.logs.Status;
import ru.spbau.sazanovich.nikita.mygit.objects.Blob;
import ru.spbau.sazanovich.nikita.mygit.objects.Commit;
import ru.spbau.sazanovich.nikita.mygit.objects.Tree;
import ru.spbau.sazanovich.nikita.mygit.utils.Hasher;
import ru.spbau.sazanovich.nikita.mygit.utils.Mapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.spbau.sazanovich.nikita.mygit.objects.Tree.TreeObject;

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
    public List<Path> scanDirectory() throws MyGitStateException, IOException {
        final Tree tree = mapper.getHeadTree();
        final List<Path> headPaths = new ArrayList<>();
        traverse(tree, myGitDirectory, headPaths);
        final List<Path> presentedPaths =
                Files
                    .find(myGitDirectory, Integer.MAX_VALUE, (p, bfa) -> !containsMyGitAsSubpath(p))
                    .collect(Collectors.toList());
        final List<Path> indexPaths = mapper.readIndexPaths();
        for (Path path : headPaths) {
            System.out.println("HED: " + path);
        }
        for (Path path : indexPaths) {
            System.out.println("IND: " + path);
        }
        for (Path path : presentedPaths) {
            System.out.println("PRS: " + path);
        }
        return new ArrayList<>();
    }

    public void addPathsToIndex(@NotNull List<String> arguments)
            throws MyGitStateException, MyGitIllegalArgumentException, IOException {
        final Function<List<Path>, Consumer<Path>> action =
                paths -> (Consumer<Path>) path -> {
                    if (!paths.contains(path)) {
                        paths.add(path);
                    }
                };
        performUpdateToIndex(arguments, action);
    }

    public void resetPaths(@NotNull List<String> arguments)
            throws MyGitStateException, MyGitIllegalArgumentException, IOException {
        final Function<List<Path>, Consumer<Path>> action =
                paths -> (Consumer<Path>) path -> {
                    if (paths.contains(path)) {
                        paths.remove(path);
                    }
                };
        performUpdateToIndex(arguments, action);
    }
    
    @NotNull
    public Status getHeadStatus() throws MyGitStateException, IOException {
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
                                      @NotNull Function<List<Path>, Consumer<Path>> action)
            throws MyGitStateException, MyGitIllegalArgumentException, IOException {
        final List<Path> argsPaths = convertStringsToPaths(arguments);
        final List<Path> indexedPaths = mapper.readIndexPaths();
        final Consumer<Path> indexUpdater = action.apply(indexedPaths);
        argsPaths.forEach(indexUpdater);
        mapper.writeIndexPaths(indexedPaths);
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
            path = myGitDirectory.relativize(path);
            paths.add(path);
        }
        return paths;
    }

    private void traverse(@NotNull Tree tree, @NotNull Path prefixPath, @NotNull List<Path> paths)
            throws MyGitStateException, IOException {
        paths.add(prefixPath);
        for (TreeObject child : tree.getChildren()) {
            final Path childPath = Paths.get(prefixPath.toString(), child.getName());
            switch (child.getType()) {
                case Blob.TYPE:
                    paths.add(childPath);
                    break;
                case Tree.TYPE:
                    final Tree childTree = mapper.readTree(child.getSha());
                    traverse(childTree, childPath, paths);
                    break;
                default:
                    throw new MyGitStateException("met an unknown type while traversing the tree -- " + child.getType());
            }
        }
    }

    private static boolean containsMyGitAsSubpath(@Nullable Path path) {
        return path != null && (path.endsWith(".mygit") || containsMyGitAsSubpath(path.getParent()));
    }
}
