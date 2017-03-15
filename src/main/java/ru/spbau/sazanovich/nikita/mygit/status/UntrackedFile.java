package ru.spbau.sazanovich.nikita.mygit.status;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * Change type which represents a file that is not present in HEAD's current state.
 */
public class UntrackedFile extends Change {

    /**
     * Constructs a change with given path.
     *
     * @param path path to the change's file
     */
    public UntrackedFile(@NotNull Path path) {
        super(path);
    }
}
