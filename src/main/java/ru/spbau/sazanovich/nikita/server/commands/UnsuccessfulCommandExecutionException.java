package ru.spbau.sazanovich.nikita.server.commands;

/**
 * Exception which is thrown when a {@link Command} has an error during execution.
 */
public class UnsuccessfulCommandExecutionException extends Exception {

    /**
     * Constructs an exception without a message and a cause.
     */
    public UnsuccessfulCommandExecutionException() {
        super();
    }

    /**
     * Constructs an exception with a given message.
     *
     * @param message message to attach to
     */
    UnsuccessfulCommandExecutionException(String message) {
        super(message);
    }

    /**
     * Constructs an exception with a given message and a cause.
     *
     * @param message message to attach to
     * @param cause cause to attach to
     */
    UnsuccessfulCommandExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
