package ru.spbau.sazanovich.nikita.mygit.commands;

import org.jetbrains.annotations.NotNull;
import ru.spbau.sazanovich.nikita.mygit.MyGitStateException;
import ru.spbau.sazanovich.nikita.mygit.objects.FileDifference;
import ru.spbau.sazanovich.nikita.mygit.objects.FileDifferenceStageStatus;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Command class which removes all files which are untracked by MyGit.
 */
class CleanCommand extends Command {

    CleanCommand(@NotNull InternalStateAccessor internalStateAccessor) {
        super(internalStateAccessor);
    }

    void perform() throws MyGitStateException, IOException {
        final List<FileDifference> differences = new StatusCommand(internalStateAccessor).perform();
        final List<FileDifference> untrackedDiffs =
                FileDifferenceStageStatus.filterBy(differences, FileDifferenceStageStatus.UNTRACKED);
        for (FileDifference diff : untrackedDiffs) {
            final Path file = internalStateAccessor.getMyGitDirectory().resolve(diff.getPath());
            InternalStateAccessor.deleteFile(file);
        }
    }
}
