package ru.spbau.sazanovich.nikita.mygit;

import org.jetbrains.annotations.NotNull;

/**
 * Calling command when you are in a state where performing the command is not possible.
 */
public class MyGitMissingPrerequisitesException extends MyGitException {

    /**
     * Constructs an exception with specified message.
     *
     * @param message a message to attach to an exception
     */
    public MyGitMissingPrerequisitesException(@NotNull String message) {
        super(message);
    }
}
