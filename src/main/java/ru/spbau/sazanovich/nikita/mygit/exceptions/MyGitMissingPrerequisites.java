package ru.spbau.sazanovich.nikita.mygit.exceptions;

/**
 * Calling command when you are in a state where performing the command is not possible.
 */
public class MyGitMissingPrerequisites extends MyGitException {

    public MyGitMissingPrerequisites(String message) {
        super(message);
    }
}
