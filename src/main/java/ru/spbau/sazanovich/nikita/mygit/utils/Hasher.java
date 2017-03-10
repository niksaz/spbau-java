package ru.spbau.sazanovich.nikita.mygit.utils;

import org.jetbrains.annotations.NotNull;

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
    private static final int BUFFER_SIZE = 8192;

    @NotNull
    public static String getHashFromFile(@NotNull String path) throws IOException {
        return bytesToHex(getByteHashFromFile(path));
    }

    @NotNull
    public static String getHashFromObject(@NotNull Object object) throws IOException {
        return bytesToHex(getByteHashFromObject(object));
    }

    @NotNull
    private static byte[] getByteHashFromFile(@NotNull String path) throws IOException {
        final MessageDigest messageDigest = getMessageDigest();
        try (FileInputStream fileInputStream = new FileInputStream(path);
             DigestInputStream digestInputStream = new DigestInputStream(fileInputStream, messageDigest)
        ) {
            final byte[] buffer = new byte[BUFFER_SIZE];
            //noinspection StatementWithEmptyBody
            while (digestInputStream.read(buffer) != -1) {
            }
        }
        return messageDigest.digest();
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
    private Hasher() {}
}
