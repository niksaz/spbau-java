package ru.spbau.sazanovich.nikita.mygit.commands;

import org.jetbrains.annotations.NotNull;
import ru.spbau.sazanovich.nikita.mygit.MyGitIOException;
import ru.spbau.sazanovich.nikita.mygit.MyGitStateException;

import java.util.HashSet;

/**
 * Command class which removes all paths from the current index.
 */
class UnstageAllCommand extends Command {

    UnstageAllCommand(@NotNull InternalStateAccessor internalStateAccessor) {
        super(internalStateAccessor);
    }

    void perform() throws MyGitIOException, MyGitStateException {
        internalStateAccessor.getLogger().trace("UnstageAllCommand -- started");
        internalStateAccessor.writeIndexPaths(new HashSet<>());
        internalStateAccessor.getLogger().trace("UnstageAllCommand -- completed");
    }
}
