package ru.spbau.sazanovich.nikita.mygit.exceptions;

/**
 * Exception which represents an incorrect usage of mygit.
 */
public class MyGitStateException extends MyGitException {

    public MyGitStateException(String message) {
        super(message);
    }
}
