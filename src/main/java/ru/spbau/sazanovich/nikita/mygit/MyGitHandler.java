package ru.spbau.sazanovich.nikita.mygit;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbau.sazanovich.nikita.mygit.exceptions.MyGitException;
import ru.spbau.sazanovich.nikita.mygit.exceptions.MyGitStateException;
import ru.spbau.sazanovich.nikita.mygit.objects.Blob;
import ru.spbau.sazanovich.nikita.mygit.objects.Tree;
import ru.spbau.sazanovich.nikita.mygit.utils.Mapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ru.spbau.sazanovich.nikita.mygit.objects.Tree.*;

public class MyGitHandler {

    @NotNull
    private final Path myGitDirectory;
    @NotNull
    private final Mapper mapper;

    public MyGitHandler() throws MyGitException {
        final Path path = findMyGitPath(Paths.get(".").toAbsolutePath());
        if (path == null) {
            throw new MyGitStateException("Not a mygit repository (or any of the parent directories): .mygit");
        }
        myGitDirectory = path;
        mapper = new Mapper(myGitDirectory);
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
    public List<Path> scanDirectory() throws MyGitStateException, IOException {
        final Tree tree = mapper.getHeadTree();
        final List<Path> headPaths = new ArrayList<>();
        traverse(tree, myGitDirectory, headPaths);
        final List<Path> presentedPaths =
                Files
                    .find(myGitDirectory, Integer.MAX_VALUE, (p, bfa) -> !containsMyGitAsSubpath(p))
                    .collect(Collectors.toList());
        for (Path path : headPaths) {
            System.out.println("HEAD: " + path);
        }
        for (Path path : presentedPaths) {
            System.out.println("PRES: " + path);
        }
        return new ArrayList<>();
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
                    throw new MyGitStateException("met an unknown type while traversing the tree: " + child.getType());
            }
        }
    }

    private static boolean containsMyGitAsSubpath(@Nullable Path path) {
        return path != null && (path.endsWith(".mygit") || containsMyGitAsSubpath(path.getParent()));
    }
}
