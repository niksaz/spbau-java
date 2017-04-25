package ru.spbau.sazanovich.nikita.server.commands;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class which represents a command to list directory's content.
 * <1: Int> <path: String>
 */
public class ListCommand extends Command {

    /**
     * Command's code for network communication.
     */
    public static final int CODE = 1;

    @NotNull
    private final String path;

    /**
     * Constructs a command with a given path.
     *
     * @param path directory to list
     */
    public ListCommand(@NotNull String path) {
        this.path = path;
    }

    /**
     * Returns directory's files. Format follows the pattern:
     * <size: Int> (<name: String> <is_dir: Boolean>)*
     *
     * @return directory's files byte representation
     * @throws UnsuccessfulCommandExecutionException if the command can not be executed
     */
    @Override
    @NotNull
    public byte[] execute() throws UnsuccessfulCommandExecutionException {
        List<Path> paths = list(path);
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
             DataOutputStream outputStream = new DataOutputStream(byteStream)
        ) {
            outputStream.writeInt(paths.size());
            for (Path path : paths) {
                outputStream.writeUTF(path.getFileName().toString());
            }
            outputStream.flush();
            return byteStream.toByteArray();
        } catch (IOException e) {
            throw new UnsuccessfulCommandExecutionException("IO error while transforming response into bytes", e);
        }
    }

    /**
     * Converts bytes to list of filenames which were retrieved from a server. Inverse for {@link #execute()}.
     *
     * @param content bytes to convert
     * @return list of filenames
     * @throws IOException if some I/O error occurs
     */
    @NotNull
    public static List<String> fromBytes(@NotNull byte[] content) throws IOException {
        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(content);
             DataInputStream inputStream = new DataInputStream(byteStream)
        ) {
            int size = inputStream.readInt();
            List<String> names = new LinkedList<>();
            while (size > 0) {
                size--;
                names.add(inputStream.readUTF());
            }
            return names;
        }
    }

    @NotNull
    private List<Path> list(@NotNull String path) throws UnsuccessfulCommandExecutionException {
        final Path directory;
        try {
            directory = Paths.get(path);
        } catch (InvalidPathException e) {
            throw new UnsuccessfulCommandExecutionException("Invalid path", e);
        }
        if (!Files.isDirectory(directory)) {
            throw new UnsuccessfulCommandExecutionException("Should be a directory");
        }
        try {
            return Files.list(directory).collect(Collectors.toList());
        } catch (IOException e) {
            throw new UnsuccessfulCommandExecutionException("IO error while listing directory", e);
        }
    }
}
