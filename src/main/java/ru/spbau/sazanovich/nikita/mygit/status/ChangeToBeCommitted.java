package ru.spbau.sazanovich.nikita.mygit.status;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * Change type which represents a modification of HEAD's current state that will be applied with next commit.
 */
public class ChangeToBeCommitted extends Change {

    @NotNull
    private FileChangeType fileChangeType;

    /**
     * Constructs a change with given path and {@link FileChangeType}.
     *
     * @param path path to the change's file
     * @param fileChangeType change type of the file
     */
    public ChangeToBeCommitted(@NotNull Path path, @NotNull FileChangeType fileChangeType) {
        super(path);
        this.fileChangeType = fileChangeType;
    }

    /**
     * Gets change type of the file.
     *
     * @return change type of the file
     */
    @NotNull
    public FileChangeType getFileChangeType() {
        return fileChangeType;
    }
}
