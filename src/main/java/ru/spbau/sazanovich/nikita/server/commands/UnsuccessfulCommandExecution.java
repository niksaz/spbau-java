package ru.spbau.sazanovich.nikita.server.commands;

/**
 * Exception which is thrown when a {@link Command} has an error during execution.
 */
public class UnsuccessfulCommandExecution extends Exception {

    public UnsuccessfulCommandExecution() {
    }

    UnsuccessfulCommandExecution(String message) {
        super(message);
    }

    UnsuccessfulCommandExecution(String message, Throwable cause) {
        super(message, cause);
    }
}
