package ru.spbau.sazanovich.nikita.mygit.commands;

import org.jetbrains.annotations.NotNull;

/**
 * Base class for MyGit commands which require access to internal state.
 */
abstract class Command {

    @NotNull
    InternalStateAccessor internalStateAccessor;

    Command(@NotNull InternalStateAccessor internalStateAccessor) {
        this.internalStateAccessor = internalStateAccessor;
    }
}
