package ru.spbau.sazanovich.nikita.mygit;

import org.jetbrains.annotations.NotNull;

/**
 * Exception which is occurred during mygit work with OS filesystem.
 */
public class MyGitIOException extends MyGitException {

    /**
     * Constructs an exception with specified message.
     *
     * @param message a message to attach to an exception
     * @param cause a cause of an exception
     */
    public MyGitIOException(@NotNull String message, @NotNull Throwable cause) {
        super(message, cause);
    }
}
