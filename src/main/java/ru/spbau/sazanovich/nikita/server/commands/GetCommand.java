package ru.spbau.sazanovich.nikita.server.commands;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Class which represents a command to read file's content.
 */
public class GetCommand extends Command {

    private final String path;

    public GetCommand(@NotNull String path) {
        this.path = path;
    }

    @NotNull
    @Override
    public byte[] execute() throws UnsuccessfulCommandExecution {
        return get(path);
    }

    @NotNull
    private byte[] get(@NotNull String path) throws UnsuccessfulCommandExecution {
        final Path file;
        try {
            file = Paths.get(path);
        } catch (InvalidPathException e) {
            throw new UnsuccessfulCommandExecution("Invalid path", e);
        }
        if (Files.isDirectory(file)) {
            throw new UnsuccessfulCommandExecution("Should be a file");
        }
        try {
            return Files.readAllBytes(file);
        } catch (IOException e) {
            throw new UnsuccessfulCommandExecution("IO error while listing directory", e);
        }
    }
}
