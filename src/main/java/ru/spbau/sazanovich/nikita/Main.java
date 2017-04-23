package ru.spbau.sazanovich.nikita;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Provides console access to a MD5 hasher.
 */
public class Main {

    private static final int NUMBER_OF_THREADS = 4;

    public static void main(@NotNull String[] args) {
        if (args.length == 0) {
            System.out.println("You should enter at least one file path");
            System.out.println("So... We will compute a hash of the current directory");
            final Path currentDirectory = Paths.get(System.getProperty("user.dir"));
            args = new String[1];
            args[0] = currentDirectory.toString();
            computeHashes(args);
        } else {
            computeHashes(args);
        }
    }

    /**
     * Computes hashes of files in single-threaded and concurrent environments.
     *
     * @param paths String[] array with path to files
     */
    public static void computeHashes(@NotNull String[] paths) {
        final MD5Util hasher = new MD5Util();
        final MD5Util concurrentHasher = new MD5Util(NUMBER_OF_THREADS);
        for (String path : paths) {
            System.out.println(path + ":");
            try {
                long start = System.currentTimeMillis();
                System.out.println(hasher.getHashFromFile(path));
                System.out.printf("Computed in single-threaded environment in %d msecs\n",
                        System.currentTimeMillis() - start);

                start = System.currentTimeMillis();
                System.out.println(concurrentHasher.getHashFromFile(path));
                System.out.printf("Computed in concurrent environment in %d msecs\n",
                        System.currentTimeMillis() - start);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
