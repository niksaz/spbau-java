package ru.spbau.sazanovich.nikita.mygit.utils;

import org.jetbrains.annotations.NotNull;

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
public class SHA1Hasher {

    private static final String HASH_ALGORITHM = "SHA-1";

    /**
     * Computes SHA-1 hash for a given object.
     *
     * @param object object which hash should be computed
     * @return string representation of sha-1 hash
     * @throws IOException if an exception occurs in a hasher
     */
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

    private SHA1Hasher() {}
}
