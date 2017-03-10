package ru.spbau.sazanovich.nikita.mygit.utils;

import java.io.*;

/**
 * Utils method to serialize Java {@link java.io.Serializable} classes.
 */
public class Mapper {

    private Mapper() {}

    public static String map(Object object) throws IOException {
        final String hash = Hasher.getHashFromObject(object);
        final String directoryName = ".mygit/objects/" + hash.substring(0, 2);
        final String fileName = directoryName + "/" + hash.substring(2);
        boolean createdSuccessfully = new File(directoryName).mkdir();
        final File objectFile = new File(fileName);
        createdSuccessfully = objectFile.createNewFile();
        try (FileOutputStream fileOutputStream = new FileOutputStream(objectFile);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)
        ) {
            objectOutputStream.writeObject(object);
        }
        System.out.println(hash);
        return hash;
    }
}
