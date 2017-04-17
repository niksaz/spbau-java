package ru.spbau.sazanovich.nikita.mygit.utils;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.spbau.sazanovich.nikita.mygit.MyGitIOException;
import ru.spbau.sazanovich.nikita.mygit.MyGitIllegalArgumentException;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Hashes objects using SHA-1 algorithm.
 */
public class SHA1Hasher implements MyGitHasher {

    private static final String HASH_ALGORITHM = "SHA-1";

    /**
     * Computes SHA-1 hash for a given object.
     *
     * @param object object which hash should be computed
     * @return string representation of sha-1 hash
     * @throws IOException if an exception occurs in a hasher
     */
    @NotNull
    @Override
    public String getHashFromObject(@NotNull Object object) throws IOException {
        return bytesToHex(getByteHashFromObject(object));
    }

    /**
     * Splits previously gained by {@link #getByteHashFromObject(Object) getByteHashFromObject} into 2 first and
     * 38 last characters.
     *
     * @param hash hash gained by {@link #getByteHashFromObject(Object) getByteHashFromObject}
     * @return split hash
     * @throws MyGitIllegalArgumentException if the string's length isn't 40 characters
     */
    @NotNull
    @Override
    public HashParts splitHash(@NotNull String hash) throws MyGitIllegalArgumentException {
        return new SHA1Parts(hash);
    }

    /**
     * Splitting on a directory name and a file name:
     * first 2 chars goes to the first part, 38 others -- to the last part.
     */
    private class SHA1Parts implements HashParts {

        private static final int FIRST_PART_ENDS = 2;
        private static final int HASH_LENGTH = 40;

        @NotNull
        private String first;
        @NotNull
        private String last;

        /**
         * Constructs hash parts from the given string.
         *
         * @param hash string which represents sha-1 hash
         * @throws MyGitIllegalArgumentException if the string's length isn't 40 characters
         */
        SHA1Parts(@NotNull String hash) throws MyGitIllegalArgumentException {
            if (hash.length() != HASH_LENGTH) {
                throw new MyGitIllegalArgumentException("hash length isn't " + HASH_LENGTH);
            }
            first = hash.substring(0, FIRST_PART_ENDS);
            last = hash.substring(FIRST_PART_ENDS);
        }

        /**
         * Gets first part of the hash.
         *
         * @return first part of the hash
         */
        @Override
        @NotNull
        public String getFirst() {
            return first;
        }

        /**
         * Gets last part of the hash.
         *
         * @return last part of the hash
         */
        @Override
        @NotNull
        public String getLast() {
            return last;
        }
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
}
