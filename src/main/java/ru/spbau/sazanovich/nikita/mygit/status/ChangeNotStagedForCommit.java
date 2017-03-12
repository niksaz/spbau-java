package ru.spbau.sazanovich.nikita.mygit.status;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * Change type which represents a modification of HEAD's current state that will not be applied with next commit.
 */
public class ChangeNotStagedForCommit extends Change {

    @NotNull
    private FileChangeType fileChangeType;

    public ChangeNotStagedForCommit(@NotNull Path path, @NotNull FileChangeType fileChangeType) {
        super(path);
        this.fileChangeType = fileChangeType;
    }

    @NotNull
    public FileChangeType getFileChangeType() {
        return fileChangeType;
    }
}
