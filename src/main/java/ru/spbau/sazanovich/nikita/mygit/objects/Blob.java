package ru.spbau.sazanovich.nikita.mygit.objects;

/**
 * Object which corresponds to a file in a filesystem. Its purpose is to store a content.
 */
public class Blob {

    public static final String TYPE = "blob";

    private byte[] content;
}
