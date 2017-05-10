package ru.spbau.sazanovich.nikita.xunit.runner;

/**
 * Shows that test class does not meet requirements for test classes.
 */
class IllegalTestClassException extends Exception {

    IllegalTestClassException(String message) {
        super(message);
    }

    IllegalTestClassException(String message, Throwable cause) {
        super(message, cause);
    }
}
