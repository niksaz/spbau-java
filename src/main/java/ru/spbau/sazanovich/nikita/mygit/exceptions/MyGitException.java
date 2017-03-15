package ru.spbau.sazanovich.nikita.mygit.exceptions;

import org.jetbrains.annotations.NotNull;

/**
 * Base class for MyGit's exception
 */
public class MyGitException extends Exception {

    MyGitException(@NotNull String message) {
        super(message);
    }
}
