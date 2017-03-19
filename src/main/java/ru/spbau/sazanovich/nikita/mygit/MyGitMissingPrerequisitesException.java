package ru.spbau.sazanovich.nikita.mygit;

/**
 * Calling command when you are in a state where performing the command is not possible.
 */
public class MyGitMissingPrerequisitesException extends MyGitException {

    /**
     * Constructs an exception with specified message.
     *
     * @param message non-null message to attach to an exception
     */
    public MyGitMissingPrerequisitesException(String message) {
        super(message);
    }
}
