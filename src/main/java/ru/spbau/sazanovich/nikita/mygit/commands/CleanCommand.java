package ru.spbau.sazanovich.nikita.mygit.commands;

import org.jetbrains.annotations.NotNull;
import ru.spbau.sazanovich.nikita.mygit.MyGitIOException;
import ru.spbau.sazanovich.nikita.mygit.MyGitStateException;
import ru.spbau.sazanovich.nikita.mygit.objects.FileDifference;
import ru.spbau.sazanovich.nikita.mygit.objects.FileDifferenceStageStatus;
import ru.spbau.sazanovich.nikita.mygit.utils.FileSystem;

import java.nio.file.Path;
import java.util.List;

/**
 * Command class which removes all files which are untracked by MyGit.
 */
class CleanCommand extends Command {

    CleanCommand(@NotNull InternalStateAccessor internalStateAccessor) {
        super(internalStateAccessor);
    }

    void perform() throws MyGitStateException, MyGitIOException {
        internalStateAccessor.getLogger().trace("CleanCommand -- started");
        final List<FileDifference> differences = new StatusCommand(internalStateAccessor).perform();
        final List<FileDifference> untrackedDiffs =
                FileDifferenceStageStatus.filterBy(differences, FileDifferenceStageStatus.UNTRACKED);
        for (FileDifference diff : untrackedDiffs) {
            final Path file = internalStateAccessor.getMyGitDirectory().resolve(diff.getPath());
            FileSystem.deleteFile(file);
        }
        internalStateAccessor.getLogger().trace("CleanCommand -- completed");
    }
}
