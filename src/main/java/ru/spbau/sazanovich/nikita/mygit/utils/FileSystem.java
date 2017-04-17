package ru.spbau.sazanovich.nikita.mygit.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbau.sazanovich.nikita.mygit.MyGitIOException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Class which contains utility functions to interact with the filesystem.
 */
public final class FileSystem {

    /**
     * Removes a file at the given path if it exists; otherwise just does nothing.
     * If it's a directory it will delete all internal files.
     *
     * @param path a file's path to remove
     * @throws MyGitIOException if an I/O error occurs
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void deleteFile(@NotNull Path path) throws MyGitIOException {
        if (!Files.exists(path)) {
            return;
        }
        try {
            if (Files.isDirectory(path)) {
                FileSystem
                        .walk(path)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            } else {
                Files.delete(path);
            }
        } catch (IOException e) {
            throw new MyGitIOException("Could not delete file " + path, e);
        }
    }

    /**
     * Finds a directory with a given name in current directory or in direct parents' directories.
     *
     * @param directoryName name of a directory to find
     * @param currentDirectory directory from where the search will start
     * @return a first path where .mygit directory was found; otherwise {@code null}
     */
    @Nullable
    public static Path findFirstDirectoryAbove(@NotNull String directoryName, @Nullable Path currentDirectory) {
        if (currentDirectory == null) {
            return null;
        }
        final Path possibleMyGitDirectory = Paths.get(currentDirectory.toString(), directoryName);
        if (Files.exists(possibleMyGitDirectory)) {
            return currentDirectory;
        } else {
            return findFirstDirectoryAbove(directoryName, currentDirectory.getParent());
        }
    }

    /**
     * Checks whether a path contains a specified name as a subpath.
     *
     * @param path a path in which to find a name
     * @param name a name to find
     * @return {@code true} if the path contains name; {@code false} otherwise
     */
    public static boolean pathContainsNameAsSubpath(@Nullable Path path, @NotNull String name) {
        return path != null && (path.endsWith(name) || pathContainsNameAsSubpath(path.getParent(), name));
    }

    /**
     * Creates a specified directory.
     *
     * @param directory directory's path to create
     * @throws MyGitIOException if an I/O error occurs while creating the directory
     */
    public static void createDirectory(@NotNull Path directory) throws MyGitIOException {
        try {
            Files.createDirectory(directory);
        } catch (IOException e) {
            throw new MyGitIOException("Could not create directory " + directory, e);
        }
    }

    /**
     * Creates a specified file.
     *
     * @param file file's path to create
     * @throws MyGitIOException if an I/O error occurs while creating the file
     */
    public static void createFile(@NotNull Path file) throws MyGitIOException {
        try {
            Files.createFile(file);
        } catch (IOException e) {
            throw new MyGitIOException("Could not create file " + file, e);
        }
    }

    /**
     * Reads all file's bytes.
     *
     * @param file file's path to read
     * @return content of the file
     * @throws MyGitIOException if an I/O error occurs while reading the file
     */
    @NotNull
    public static byte[] readAllBytes(@NotNull Path file) throws MyGitIOException {
        try {
            return Files.readAllBytes(file);
        } catch (IOException e) {
            throw new MyGitIOException("Could not read file " + file, e);
        }
    }

    /**
     * Reads all file's lines.
     *
     * @param file file's path to read
     * @return content of the file
     * @throws MyGitIOException if an I/O error occurs while reading the file
     */
    @NotNull
    public static Stream<String> lines(@NotNull Path file) throws MyGitIOException {
        try {
            return Files.lines(file);
        } catch (IOException e) {
            throw new MyGitIOException("Could not get file's lines " + file, e);
        }
    }

    /**
     * Traverses directory.
     *
     * @param directory directory's path to traverse
     * @return inner paths
     * @throws MyGitIOException if an I/O error occurs while traversing the directory
     */
    @NotNull
    public static Stream<Path> walk(@NotNull Path directory) throws MyGitIOException {
        try {
            return Files.walk(directory);
        } catch (IOException e) {
            throw new MyGitIOException("Could not traverse directory " + directory, e);
        }
    }

    /**
     * Lists directory's content.
     *
     * @param directory directory's path to list
     * @return content
     * @throws MyGitIOException if an I/O error occurs while listing the directory
     */
    @NotNull
    public static Stream<Path> list(@NotNull Path directory) throws MyGitIOException {
        try {
            return Files.list(directory);
        } catch (IOException e) {
            throw new MyGitIOException("Could not list directory " + directory, e);
        }
    }

    /**
     * @param path path to write to
     * @param content byte to write to path
     * @throws MyGitIOException if an I/O error occurs while writing to the file
     */
    public static void write(@NotNull Path path, @NotNull byte[] content) throws MyGitIOException {
        try {
            Files.write(path, content);
        } catch (IOException e) {
            throw new MyGitIOException("Could not list write to " + path, e);
        }
    }

    private FileSystem() {}
}
