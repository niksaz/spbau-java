package ru.spbau.sazanovich.nikita.mygit.exceptions;

/**
 * Exception which is occurred during mygit work with OS filesystem.
 */
public class MyGitAlreadyInitialized extends MyGitException {

    public MyGitAlreadyInitialized() {
        super("mygit repository is already created");
    }
}
