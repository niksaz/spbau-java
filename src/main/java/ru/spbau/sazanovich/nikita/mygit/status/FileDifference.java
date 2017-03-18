package ru.spbau.sazanovich.nikita.mygit.status;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * Basic class for all differences between MyGit repository's HEAD state and a filesystem state.
 */
public class FileDifference {

    @NotNull
    private Path path;
    @NotNull
    private final FileDifferenceType fileDifferenceType;
    @NotNull
    private final FileDifferenceStageStatus fileDifferenceStageStatus;

    /**
     * Constructs a difference that was found at the given path, representing the given difference type
     * and it is in a given state in MyGit's stage area.
     *
     * @param path a path where the difference was found
     * @param fileDifferenceType a type of the difference
     * @param fileDifferenceStageStatus a difference's state in MyGit's stage area
     */
    public FileDifference(@NotNull Path path, @NotNull FileDifferenceType fileDifferenceType,
                          @NotNull FileDifferenceStageStatus fileDifferenceStageStatus) {
        this.path = path;
        this.fileDifferenceType = fileDifferenceType;
        this.fileDifferenceStageStatus = fileDifferenceStageStatus;
    }

    /**
     * Gets a path where the difference was found.
     *
     * @return a path where the difference was found
     */
    @NotNull
    public Path getPath() {
        return path;
    }

    /**
     * Gets a type of the difference.
     *
     * @return a type of the difference
     */
    @NotNull
    public FileDifferenceStageStatus getStageStatus() {
        return fileDifferenceStageStatus;
    }

    /**
     * Gets a difference's state in MyGit's stage area.
     *
     * @return a difference's state in MyGit's stage area
     */
    @NotNull
    public FileDifferenceType getType() {
        return fileDifferenceType;
    }
}
