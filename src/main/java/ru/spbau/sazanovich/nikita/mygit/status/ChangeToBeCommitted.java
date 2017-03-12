package ru.spbau.sazanovich.nikita.mygit.status;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * Change type which represents a modification of HEAD's current state that will be applied with next commit.
 */
public class ChangeToBeCommitted extends Change {

    @NotNull
    private FileChangeType fileChangeType;

    public ChangeToBeCommitted(@NotNull Path path, @NotNull FileChangeType fileChangeType) {
        super(path);
        this.fileChangeType = fileChangeType;
    }

    @NotNull
    public FileChangeType getFileChangeType() {
        return fileChangeType;
    }
}
