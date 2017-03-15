package ru.spbau.sazanovich.nikita.mygit.exceptions;

import org.jetbrains.annotations.NotNull;

/**
 * Exception indicating that the user of the library passed illegal argument.
 */
public class MyGitIllegalArgumentException extends MyGitException {

    /**
     * Constructs an exception with specified message.
     *
     * @param message non-null message to attach to an exception
     */
    public MyGitIllegalArgumentException(@NotNull String message) {
        super(message);
    }
}
