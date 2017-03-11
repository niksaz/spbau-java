package ru.spbau.sazanovich.nikita.mygit.exceptions;

/**
 * Exception indicating that the user of the library passed illegal argument.
 */
public class MyGitIllegalArgumentException extends MyGitException {

    public MyGitIllegalArgumentException(String message) {
        super(message);
    }
}
