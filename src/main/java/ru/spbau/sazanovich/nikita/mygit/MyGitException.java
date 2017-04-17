package ru.spbau.sazanovich.nikita.mygit;

import org.jetbrains.annotations.NotNull;

/**
 * Base class for MyGit's exception
 */
public class MyGitException extends Exception {

    MyGitException(@NotNull String message) {
        super(message);
    }

    MyGitException(@NotNull String message, @NotNull Throwable cause) {
        super(message, cause);
    }
}
