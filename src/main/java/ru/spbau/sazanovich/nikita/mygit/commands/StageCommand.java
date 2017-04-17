package ru.spbau.sazanovich.nikita.mygit.commands;

import org.jetbrains.annotations.NotNull;
import ru.spbau.sazanovich.nikita.mygit.MyGitIOException;
import ru.spbau.sazanovich.nikita.mygit.MyGitIllegalArgumentException;
import ru.spbau.sazanovich.nikita.mygit.MyGitStateException;

import java.nio.file.Path;
import java.util.Set;

/**
 * Command class which adds paths to the current index.
 */
class StageCommand extends Command {

    @NotNull
    private final String stringPath;

    StageCommand(@NotNull String stringPath, @NotNull InternalStateAccessor internalStateAccessor) {
        super(internalStateAccessor);
        this.stringPath = stringPath;
    }

    void perform() throws MyGitIllegalArgumentException, MyGitIOException, MyGitStateException {
        internalStateAccessor.getLogger().trace("StageCommand -- started with path=" + stringPath);
        final Path path = internalStateAccessor.convertStringToPathRelativeToMyGitDirectory(stringPath);
        if (path == null) {
            return;
        }
        final Set<Path> indexedPaths = internalStateAccessor.readIndexPaths();
        if (!indexedPaths.contains(path)) {
            indexedPaths.add(path);
        }
        internalStateAccessor.writeIndexPaths(indexedPaths);
        internalStateAccessor.getLogger().trace("StageCommand -- completed");
    }
}
