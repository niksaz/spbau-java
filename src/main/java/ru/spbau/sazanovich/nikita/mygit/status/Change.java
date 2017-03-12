package ru.spbau.sazanovich.nikita.mygit.status;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * Basic class for all changed occurred in the MyGit repository.
 */
public class Change {

    @NotNull
    private Path path;

    Change(@NotNull Path path) {
        this.path = path;
    }

    @NotNull
    public Path getPath() {
        return path;
    }

    public void relativizePath(@NotNull Path otherPath) {
        path = otherPath.relativize(path);
    }
}
