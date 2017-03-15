package ru.spbau.sazanovich.nikita.mygit.exceptions;

/**
 * Exception which represents an incorrect usage of mygit.
 */
public class MyGitStateException extends MyGitException {

    /**
     * Constructs an exception with specified message.
     *
     * @param message non-null message to attach to an exception
     */
    public MyGitStateException(String message) {
        super(message);
    }
}
