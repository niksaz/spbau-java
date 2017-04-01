package ru.spbau.sazanovich.nikita.mygit.commands;

import org.jetbrains.annotations.NotNull;
import ru.spbau.sazanovich.nikita.mygit.MyGitStateException;
import ru.spbau.sazanovich.nikita.mygit.objects.HeadStatus;

import java.io.IOException;

/**
 * Command class which gets the current HEAD state.
 */
class HeadStatusCommand {

    @NotNull
    private final InternalStateAccessor internalStateAccessor;

    HeadStatusCommand(@NotNull InternalStateAccessor internalStateAccessor) {
        this.internalStateAccessor = internalStateAccessor;
    }

    HeadStatus perform() throws MyGitStateException, IOException {
        return internalStateAccessor.getHeadStatus();
    }
}
