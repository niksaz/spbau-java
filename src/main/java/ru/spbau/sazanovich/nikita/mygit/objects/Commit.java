package ru.spbau.sazanovich.nikita.mygit.objects;

import java.util.Date;

/**
 * Object which stores commit's message and reference to base {@link Tree} object.
 */
public class Commit extends GitObject {

    private static final String TYPE = "commit";

    private String message;
    private String author;
    private Date dateCreated;

    public Commit(String message) {
        super(TYPE);
        this.message = message;
    }
}
