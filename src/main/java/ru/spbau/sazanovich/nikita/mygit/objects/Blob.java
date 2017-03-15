package ru.spbau.sazanovich.nikita.mygit.objects;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Object which corresponds to a file in a filesystem. Its purpose is to store a content.
 */
public class Blob implements Serializable {

    /**
     * Constant which is used through the library to identify Blob objects.
     */
    public static final String TYPE = "blob";

    @NotNull
    private byte[] content;

    /**
     * Constructs an object with given content.
     *
     * @param content byte content of an associated file
     */
    public Blob(@NotNull byte[] content) {
        this.content = content;
    }

    /**
     * Gets byte content of an associated file.
     *
     * @return byte content of an associated file
     */
    @NotNull
    public byte[] getContent() {
        return content;
    }
}
