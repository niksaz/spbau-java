package ru.spbau.sazanovich.nikita.mygit.commands;

import org.jetbrains.annotations.NotNull;
import ru.spbau.sazanovich.nikita.mygit.MyGitStateException;
import ru.spbau.sazanovich.nikita.mygit.objects.HeadStatus;

import java.io.IOException;

/**
 * Command class which gets the current HEAD state.
 */
class HeadStatusCommand extends Command {

    HeadStatusCommand(@NotNull InternalStateAccessor internalStateAccessor) {
        super(internalStateAccessor);
    }

    HeadStatus perform() throws MyGitStateException, IOException {
        return internalStateAccessor.getHeadStatus();
    }
}
