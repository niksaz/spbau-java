package ru.spbau.sazanovich.nikita.server.commands;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Class which represents a command to read file's content.
 * <2: Int> <path: String>
 */
public class GetCommand extends Command {

    /**
     * Command's code for network communication.
     */
    public static final int CODE = 2;

    @NotNull
    private final String path;

    /**
     * Constructs a command with a given path.
     *
     * @param path file to get
     */
    public GetCommand(@NotNull String path) {
        this.path = path;
    }

    /**
     * Returns file's byte content.
     *
     * @return file's content
     * @throws UnsuccessfulCommandExecutionException if the command can not be executed
     */
    @Override
    @NotNull
    public byte[] execute() throws UnsuccessfulCommandExecutionException {
        return get();
    }

    @NotNull
    byte[] get() throws UnsuccessfulCommandExecutionException {
        final Path file;
        try {
            file = Paths.get(path);
        } catch (InvalidPathException e) {
            throw new UnsuccessfulCommandExecutionException("Invalid path", e);
        }
        if (Files.isDirectory(file)) {
            throw new UnsuccessfulCommandExecutionException("Should be a file");
        }
        try {
            return Files.readAllBytes(file);
        } catch (IOException e) {
            throw new UnsuccessfulCommandExecutionException("IO error while listing directory", e);
        }
    }
}
