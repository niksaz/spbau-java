package ru.spbau.sazanovich.nikita.mygit.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Object which corresponds to a file in a filesystem. Its purpose is to store a content.
 */
@AllArgsConstructor
public class Blob implements Serializable {

    /**
     * Constant which is used through the library to identify Blob objects.
     */
    public static final String TYPE = "blob";

    @NotNull
    @Getter
    private final byte[] content;
}
