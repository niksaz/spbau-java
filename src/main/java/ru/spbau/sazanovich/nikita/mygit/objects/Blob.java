package ru.spbau.sazanovich.nikita.mygit.objects;

import java.io.Serializable;

/**
 * Object which corresponds to a file in a filesystem. Its purpose is to store a content.
 */
public class Blob implements Serializable {

    public static final String TYPE = "blob";

    private byte[] content;

    public byte[] getContent() {
        return content;
    }
}
