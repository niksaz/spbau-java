package ru.spbau.sazanovich.nikita.mygit.status;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * Basic class for all changed occurred in the MyGit repository.
 */
public class Change {

    @NotNull
    private Path path;

    /**
     * Gets path to the change's file.
     *
     * @return path to the change's file
     */
    @NotNull
    public Path getPath() {
        return path;
    }

    /**
     * Relativizes the change's path against the other path.
     *
     * @param otherPath the path to relativize against this change's path
     */
    public void relativizePath(@NotNull Path otherPath) {
        path = otherPath.relativize(path);
    }

    Change(@NotNull Path path) {
        this.path = path;
    }
}
