package ru.spbau.sazanovich.nikita.mygit;

/**
 * Exception which is occurred during mygit work with OS filesystem.
 */
public class MyGitAlreadyInitializedException extends MyGitException {

    /**
     * Constructs an exception with default message {@code "mygit repository is already created"}.
     */
    public MyGitAlreadyInitializedException() {
        super("mygit repository is already created");
    }
}
