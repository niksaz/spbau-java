package ru.spbau.sazanovich.nikita.mygit.commands;

import org.jetbrains.annotations.NotNull;
import ru.spbau.sazanovich.nikita.mygit.MyGitStateException;

import java.io.IOException;
import java.util.HashSet;

/**
 * Command class which removes all paths from the current index.
 */
class UnstageAllCommand extends Command {

    UnstageAllCommand(@NotNull InternalStateAccessor internalStateAccessor) {
        super(internalStateAccessor);
    }

    void perform() throws IOException, MyGitStateException {
        internalStateAccessor.getLogger().trace("UnstageAllCommand -- started");
        internalStateAccessor.writeIndexPaths(new HashSet<>());
        internalStateAccessor.getLogger().trace("UnstageAllCommand -- completed");
    }
}
