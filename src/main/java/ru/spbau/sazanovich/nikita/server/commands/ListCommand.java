package ru.spbau.sazanovich.nikita.server.commands;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class which represents a command to list directory's content.
 */
public class ListCommand extends Command {

    private final String path;

    public ListCommand(@NotNull String path) {
        this.path = path;
    }

    @Override
    @NotNull
    public byte[] execute() throws UnsuccessfulCommandExecution {
        final List<Path> paths = list(path);
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
             DataOutputStream outputStream = new DataOutputStream(byteStream)
        ) {
            outputStream.writeInt(paths.size());
            for (Path path : paths) {
                System.out.println("FOUND " + path);
                outputStream.writeUTF(path.getFileName().toString());
            }
            outputStream.flush();
            return byteStream.toByteArray();
        } catch (IOException e) {
            throw new UnsuccessfulCommandExecution("IO error while transforming response into bytes", e);
        }
    }

    @NotNull
    private List<Path> list(@NotNull String path) throws UnsuccessfulCommandExecution {
        final Path directory;
        try {
            directory = Paths.get(path);
        } catch (InvalidPathException e) {
            throw new UnsuccessfulCommandExecution("Invalid path", e);
        }
        if (!Files.isDirectory(directory)) {
            throw new UnsuccessfulCommandExecution("Should be a directory");
        }
        try {
            return Files.list(directory).collect(Collectors.toList());
        } catch (IOException e) {
            throw new UnsuccessfulCommandExecution("IO error while listing directory", e);
        }
    }
}
