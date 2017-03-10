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

    public static String getHashFromFile(String path) {
        return bytesToHex(getByteHashFromFile(path));
    }

    public static String getHashFromObject(Object object) {
        return bytesToHex(getByteHashFromObject(object));
    }

    private static byte[] getByteHashFromFile(String path) {
        final MessageDigest messageDigest = getMessageDigest();
        try (FileInputStream fileInputStream = new FileInputStream(path);
             DigestInputStream digestInputStream = new DigestInputStream(fileInputStream, messageDigest)
        ) {
            final byte[] buffer = new byte[BUFFER_SIZE];
            //noinspection StatementWithEmptyBody
            while (digestInputStream.read(buffer) != -1) {
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            //TODO: real handling
        } catch (IOException e) {
            e.printStackTrace();
            e.printStackTrace();
            //TODO: real handling
        }
        return messageDigest.digest();
    }

    private static byte[] getByteHashFromObject(@NotNull Object object) {
        final MessageDigest messageDigest = getMessageDigest();
        final OutputStream nullOutputStream = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
            }
        };
        try (
             DigestOutputStream digestOutputStream = new DigestOutputStream(nullOutputStream, messageDigest);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(digestOutputStream)
        ) {
            objectOutputStream.writeObject(object);
        } catch (IOException e) {
            //TODO:
            e.printStackTrace();
        }
        return messageDigest.digest();
    }

    public static String bytesToHex(@NotNull byte[] bytes) {
        return DatatypeConverter.printHexBinary(bytes);
    }

    private static MessageDigest getMessageDigest() {
        try {
            return MessageDigest.getInstance(HASH_ALGORITHM);
        } catch (NoSuchAlgorithmException ignored) {
            throw new IllegalStateException();
        }
    }
    private Hasher() {}
}
