package ru.spbau.sazanovich.nikita.mygit.utils;

import org.jetbrains.annotations.NotNull;
import ru.spbau.sazanovich.nikita.mygit.exceptions.MyGitStateException;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Hashes files using SHA1 algorithm. Used to map files stored in a repository.
 */
public class Hasher {

    private static final String HASH_ALGORITHM = "SHA-1";

    private static final int FIRST_PART_ENDS = 2;
    private static final int HASH_LENGTH = 40;

    @NotNull
    public static String getHashFromObject(@NotNull Object object) throws IOException {
        return bytesToHex(getByteHashFromObject(object));
    }

    @NotNull
    private static byte[] getByteHashFromObject(@NotNull Object object) throws IOException {
        final MessageDigest messageDigest = getMessageDigest();
        final OutputStream nullOutputStream = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
            }
        };
        try (DigestOutputStream digestOutputStream = new DigestOutputStream(nullOutputStream, messageDigest);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(digestOutputStream)
        ) {
            objectOutputStream.writeObject(object);
        }
        return messageDigest.digest();
    }

    @NotNull
    private static String bytesToHex(@NotNull byte[] bytes) {
        return DatatypeConverter.printHexBinary(bytes);
    }

    @NotNull
    private static MessageDigest getMessageDigest() {
        try {
            return MessageDigest.getInstance(HASH_ALGORITHM);
        } catch (NoSuchAlgorithmException ignored) {
            throw new IllegalStateException("there is no " + HASH_ALGORITHM + " algorithm");
        }
    }

    /**
     * Used during internal storage -- splitting on a directory name and a file name.
     */
    static class HashParts {

        @NotNull
        private String first;
        @NotNull
        private String last;

        HashParts(@NotNull String hash) throws MyGitStateException {
            if (hash.length() != HASH_LENGTH) {
                throw new MyGitStateException("hash length isn't " + HASH_LENGTH);
            }
            first = hash.substring(0, FIRST_PART_ENDS);
            last = hash.substring(FIRST_PART_ENDS);
        }

        @NotNull
        String getFirst() {
            return first;
        }

        @NotNull
        String getLast() {
            return last;
        }
    }

    private Hasher() {}
}
