package ru.spbau.sazanovich.nikita.mygit;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbau.sazanovich.nikita.mygit.exceptions.MyGitException;
import ru.spbau.sazanovich.nikita.mygit.exceptions.MyGitStateException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class MyGitHandler {

    @NotNull
    private final Path myGitDirectory;

    public MyGitHandler() throws MyGitException {
        final Path path = findMyGitPath(Paths.get(".").toAbsolutePath());
        if (path == null) {
            throw new MyGitStateException("Not a mygit repository (or any of the parent directories): .mygit");
        }
        myGitDirectory = path;
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
    public ArrayList<Path> scanDirectory() throws IOException {
        return Files
                .find(myGitDirectory, Integer.MAX_VALUE, (p, bfa) -> !containsMyGitAsSubpath(p))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static boolean containsMyGitAsSubpath(@Nullable Path path) {
        return path != null && (path.endsWith(".mygit") || containsMyGitAsSubpath(path.getParent()));
    }
}
