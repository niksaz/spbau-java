package ru.spbau.sazanovich.nikita.mygit.objects;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Object which corresponds to a file in a filesystem. Its purpose is to store a content.
 */
public class Blob implements Serializable {

    public static final String TYPE = "blob";

    @NotNull
    private byte[] content;

    public Blob(@NotNull byte[] content) {
        this.content = content;
    }

    @NotNull
    public byte[] getContent() {
        return content;
    }
}
