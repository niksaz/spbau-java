package ru.spbau.sazanovich.nikita.mygit.objects;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Enum representing {@link FileDifference FileDifference's} status in a MyGit stage area.
 */
public enum FileDifferenceStageStatus {
    /**
     * MyGit HEAD's version of a file will change to a filesystem's one with a next commit.
     */
    TO_BE_COMMITTED,
    /**
     * MyGit HEAD's version of a file will not change to a filesystem's one with a next commit.
     */
    NOT_STAGED_FOR_COMMIT,
    /**
     * There is not such file in MyGit's HEAD state.
     */
    UNTRACKED;

    @NotNull
    public static List<FileDifference> filterBy(
            @NotNull List<FileDifference> list, @NotNull FileDifferenceStageStatus status) {
        return list
                .stream()
                .filter(diff -> diff.getStageStatus().equals(status))
                .collect(Collectors.toList());
    }
}
