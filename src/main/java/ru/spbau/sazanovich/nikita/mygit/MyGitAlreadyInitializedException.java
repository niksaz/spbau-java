package ru.spbau.sazanovich.nikita.mygit;

/**
 * Exception which occurs when trying to initialize MyGit in a directory which already contains MyGit repository.
 */
public class MyGitAlreadyInitializedException extends MyGitException {

    /**
     * Constructs an exception with default message {@code "mygit repository is already created"}.
     */
    public MyGitAlreadyInitializedException() {
        super("mygit repository is already created");
    }
}
