package ru.spbau.sazanovich.nikita.mygit;

import org.jetbrains.annotations.NotNull;

/**
 * Exception which represents an incorrect usage of mygit.
 */
public class MyGitStateException extends MyGitException {

    /**
     * Constructs an exception with specified message.
     *
     * @param message a message to attach to an exception
     */
    public MyGitStateException(@NotNull String message) {
        super(message);
    }
}
