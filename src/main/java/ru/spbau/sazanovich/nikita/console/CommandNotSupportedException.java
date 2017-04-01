package ru.spbau.sazanovich.nikita.console;

/**
 * Throw by the command parser if the command is not supported by the library.
 */
class CommandNotSupportedException extends Exception {

    CommandNotSupportedException(String message) {
        super(message);
    }
}
