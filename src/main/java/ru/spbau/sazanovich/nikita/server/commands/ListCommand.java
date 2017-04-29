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
        List<FileInfo> infoList = list();
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
             DataOutputStream outputStream = new DataOutputStream(byteStream)
        ) {
            outputStream.writeInt(infoList.size());
            for (FileInfo info : infoList) {
                info.writeTo(outputStream);
            }
            outputStream.flush();
            return byteStream.toByteArray();
        } catch (IOException e) {
            throw new UnsuccessfulCommandExecutionException("IO error while transforming response into bytes", e);
        }
    }

    /**
     * Converts bytes to list of {@link FileInfo} which were retrieved from a server. Inverse for {@link #execute()}.
     *
     * @param content bytes to convert
     * @return list of file's info
     * @throws IOException if some I/O error occurs
     */
    @NotNull
    public static List<FileInfo> fromBytes(@NotNull byte[] content) throws IOException {
        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(content);
             DataInputStream inputStream = new DataInputStream(byteStream)
        ) {
            int size = inputStream.readInt();
            List<FileInfo> files = new LinkedList<>();
            while (size > 0) {
                size--;
                files.add(FileInfo.readFrom(inputStream));
            }
            return files;
        }
    }

    @NotNull
    List<FileInfo> list() throws UnsuccessfulCommandExecutionException {
        final Path directory;
        try {
            directory = Paths.get(path);
        } catch (InvalidPathException e) {
            throw new UnsuccessfulCommandExecutionException("Invalid path", e);
        }
        if (!Files.isDirectory(directory) || !Files.exists(directory)) {
            throw new UnsuccessfulCommandExecutionException("Should be a present directory");
        }
        try {
            return Files.list(directory).map(FileInfo::new).collect(Collectors.toList());
        } catch (IOException e) {
            throw new UnsuccessfulCommandExecutionException("IO error while listing directory", e);
        }
    }
}
