package ru.spbau.sazanovich.nikita.mygit.commands;

import org.jetbrains.annotations.NotNull;
import ru.spbau.sazanovich.nikita.mygit.MyGitAlreadyInitializedException;
import ru.spbau.sazanovich.nikita.mygit.MyGitIOException;
import ru.spbau.sazanovich.nikita.mygit.MyGitIllegalArgumentException;
import ru.spbau.sazanovich.nikita.mygit.MyGitStateException;

import java.nio.file.Path;

/**
 * Command class which initializes MyGit repository in a given directory.
 */
class InitCommand {

    @NotNull
    private final Path directory;

    InitCommand(@NotNull Path directory) {
        this.directory = directory;
    }

    void perform() throws MyGitIllegalArgumentException, MyGitAlreadyInitializedException,
                          MyGitStateException, MyGitIOException {
        if (!directory.isAbsolute()) {
            throw new MyGitIllegalArgumentException("path parameter should be an absolute");
        }
        final InternalStateAccessor accessor = InternalStateAccessor.init(directory);
        accessor.getLogger().trace("InitCommand -- completed in " + directory);
    }
}
