package ru.spbau.sazanovich.nikita.mygit.exceptions;

/**
 * Calling command when you are in a state where performing the command is not possible.
 */
public class MyGitMissingPrerequisites extends MyGitException {

    /**
     * Constructs an exception with specified message.
     *
     * @param message non-null message to attach to an exception
     */
    public MyGitMissingPrerequisites(String message) {
        super(message);
    }
}
