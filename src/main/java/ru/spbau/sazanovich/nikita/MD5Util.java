package ru.spbau.sazanovich.nikita;

import org.jetbrains.annotations.NotNull;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;

class MD5Util {

    private static final String HASH_ALGORITHM = "MD5";
    private static final int BUFFER_SIZE = 4096;

    private final ForkJoinPool pool;

    /**
     * Creates a single-threaded hasher.
     */
    public MD5Util() {
        pool = null;
    }

    /**
     * Creates a parallel hasher with specified number of threads.
     *
     * @param threadsInPool size of the pool
     */
    public MD5Util(int threadsInPool) {
        pool = new ForkJoinPool(threadsInPool);
    }

    @NotNull
    public String getHashFromFile(@NotNull String filePathString) throws IllegalArgumentException, IOException {
        return DatatypeConverter.printHexBinary(getHashBytesFromFile(filePathString));
    }

    @NotNull
    public byte[] getHashBytesFromFile(@NotNull String filePathString) throws IllegalArgumentException, IOException {
        final Path path;
        try {
            path = Paths.get(filePathString);
        } catch (InvalidPathException e) {
            throw new IllegalArgumentException(filePathString + " is not a valid path", e);
        }
        final MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance(HASH_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Could not create " + HASH_ALGORITHM + " MessageDigest", e);
        }
        if (Files.isDirectory(path)) {
            digestDirectory(path, messageDigest);
        } else {
            digestFile(path, messageDigest);
        }
        return messageDigest.digest();
    }

    private void digestDirectory(Path path, MessageDigest messageDigest) throws IOException {
        final List<Path> filePaths = Files.list(path).collect(Collectors.toList());
        messageDigest.update(path.getFileName().toString().getBytes());
        if (pool == null) {
            for (Path filePath : filePaths) {
                messageDigest.update(getHashBytesFromFile(filePath.toString()));
            }
        } else {
            final ArrayList<RecursiveTask<byte[]>> tasks = new ArrayList<>();
            for (Path filePath : filePaths) {
                final RecursiveTask<byte[]> task = new RecursiveTask<byte[]>() {
                    @Override
                    protected byte[] compute() {
                        try {
                            return getHashBytesFromFile(filePath.toString());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                };
                tasks.add(task);
                pool.execute(task);
            }
            for (RecursiveTask<byte[]> task : tasks) {
                try {
                    messageDigest.update(task.get());
                } catch (Exception e) {
                    throw new IllegalStateException("Error in ForkJoinPool during execution", e);
                }
            }
        }
    }

    private void digestFile(Path path, MessageDigest messageDigest) throws IOException {
        final byte[] buffer = new byte[BUFFER_SIZE];
        try (InputStream fileInputStream = Files.newInputStream(path);
             DigestInputStream inputStream = new DigestInputStream(fileInputStream, messageDigest)
        ) {
            int bytesRead;
            do {
                bytesRead = inputStream.read(buffer);
            } while (bytesRead != -1);
        }
    }
}