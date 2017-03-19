package ru.spbau.sazanovich.nikita.mygit.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * Basic class for all differences between MyGit repository's HEAD state and a filesystem state.
 */
@AllArgsConstructor
public class FileDifference {

    @NotNull
    @Getter
    private Path path;
    @NotNull
    @Getter
    private final FileDifferenceType type;
    @NotNull
    @Getter
    private final FileDifferenceStageStatus stageStatus;
}
