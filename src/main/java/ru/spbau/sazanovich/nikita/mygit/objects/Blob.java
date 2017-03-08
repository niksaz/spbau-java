package ru.spbau.sazanovich.nikita.mygit.objects;

/**
 * Object which corresponds to a file in a filesystem. Its purpose is to store a content.
 */
public class Blob extends GitObject {

    private static final String TYPE = "blob";

    private String name;

    public Blob(String name) {
        super(TYPE);
        this.name = name;
    }
}
