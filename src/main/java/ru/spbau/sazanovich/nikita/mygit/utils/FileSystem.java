package ru.spbau.sazanovich.nikita.mygit.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

/**
 * Class which contains utility functions to interact with the filesystem.
 */
public final class FileSystem {

    /**
     * Removes a file at the given path if it exists; otherwise just does nothing.
     * If it's a directory it will delete all internal files.
     *
     * @param path a file's path to remove
     * @throws IOException if an I/O error occurs
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void deleteFile(@NotNull Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }
        if (Files.isDirectory(path)) {
            Files
                    .walk(path)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } else {
            Files.delete(path);
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
        return path != null && (path.endsWith(".mygit") || pathContainsNameAsSubpath(path.getParent(), name));
    }

    private FileSystem() {}
}
