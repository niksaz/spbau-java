package ru.spbau.sazanovich.nikita.mygit.commands;

import org.jetbrains.annotations.NotNull;
import ru.spbau.sazanovich.nikita.mygit.MyGitIllegalArgumentException;
import ru.spbau.sazanovich.nikita.mygit.MyGitStateException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

/**
 * Command class which removes paths from the current index.
 */
class UnstageCommand extends Command {

    @NotNull
    private final String stringPath;

    UnstageCommand(@NotNull String stringPath, @NotNull InternalStateAccessor internalStateAccessor) {
        super(internalStateAccessor);
        this.stringPath = stringPath;
    }

    void perform() throws MyGitIllegalArgumentException, IOException, MyGitStateException {
        final Path path = internalStateAccessor.convertStringToPathRelativeToMyGitDirectory(stringPath);
        if (path == null) {
            return;
        }
        final Set<Path> indexedPaths = internalStateAccessor.readIndexPaths();
        if (indexedPaths.contains(path)) {
            indexedPaths.remove(path);
        }
        internalStateAccessor.writeIndexPaths(indexedPaths);
    }
}
