package ru.spbau.sazanovich.nikita.console;

/**
 * Throw by the command parser if the command is not supported by the library.
 */
public class CommandNotSupportedException extends Exception {

    public CommandNotSupportedException(String message) {
        super(message);
    }
}
