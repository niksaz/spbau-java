package ru.spbau.sazanovich.nikita.mygit.commands;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import ru.spbau.sazanovich.nikita.mygit.MyGitAlreadyInitializedException;
import ru.spbau.sazanovich.nikita.mygit.MyGitIllegalArgumentException;
import ru.spbau.sazanovich.nikita.mygit.MyGitStateException;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Class which performs init operation. Initializes MyGit in a given directory.
 */
@AllArgsConstructor(access = AccessLevel.PACKAGE)
class InitCommand {

    private Path directory;

    void perform()
            throws MyGitIllegalArgumentException, MyGitAlreadyInitializedException, MyGitStateException, IOException {
        if (!directory.isAbsolute()) {
            throw new MyGitIllegalArgumentException("path parameter should be an absolute");
        }
        InternalStateAccessor.init(directory);
    }
}
