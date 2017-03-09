package ru.spbau.sazanovich.nikita.mygit;

import ru.spbau.sazanovich.nikita.mygit.exceptions.MyGitException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class MyGitHandler {

    private final Path myGitDirectory;

    public MyGitHandler() throws MyGitException {
        myGitDirectory = findMyGitPath(Paths.get(".").toAbsolutePath());
        if (myGitDirectory == null) {
            throw new MyGitException("Not a mygit repository (or any of the parent directories): .mygit");
        }
    }

    private Path findMyGitPath(Path currentDirectory) {
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

    public ArrayList<Path> scanDirectory() throws IOException {
        return Files
                .find(myGitDirectory, Integer.MAX_VALUE, (p, bfa) -> !containsMyGitAsSubpath(p))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static boolean containsMyGitAsSubpath(Path path) {
        return path != null && (path.endsWith(".mygit") || containsMyGitAsSubpath(path.getParent()));
    }
}
