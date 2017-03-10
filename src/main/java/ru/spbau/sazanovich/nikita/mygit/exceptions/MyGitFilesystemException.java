package ru.spbau.sazanovich.nikita.mygit.exceptions;

/**
 * Exception which is occurred during mygit work with OS filesystem.
 */
public class MyGitFilesystemException extends MyGitException {

    public MyGitFilesystemException(String message) {
        super(message);
    }
}
