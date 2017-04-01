package ru.spbau.sazanovich.nikita.mygit.commands;

import org.jetbrains.annotations.NotNull;

/**
 * Command class which resets file's state to the corresponding HEAD's one.
 * Given file should be present in the filesystem.
 */
class ResetCommand extends Command {

    @NotNull
    private final String stringPath;

    ResetCommand(@NotNull String stringPath, @NotNull InternalStateAccessor internalStateAccessor) {
        super(internalStateAccessor);
        this.stringPath = stringPath;
    }

    void perform() {
    }
}
