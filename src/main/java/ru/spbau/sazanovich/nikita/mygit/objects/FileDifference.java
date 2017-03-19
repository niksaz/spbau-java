package ru.spbau.sazanovich.nikita.mygit.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * Basic class for all differences between MyGit repository's HEAD state and a filesystem state.
 */
@AllArgsConstructor
@Getter
public class FileDifference {

    @NotNull
    private Path path;
    @NotNull
    private final FileDifferenceType type;
    @NotNull
    private final FileDifferenceStageStatus stageStatus;
}
