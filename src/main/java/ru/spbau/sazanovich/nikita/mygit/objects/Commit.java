package ru.spbau.sazanovich.nikita.mygit.objects;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Object which stores commit's message and reference to base {@link Tree} object.
 */
public class Commit implements Serializable {

    private String message;
    private String author;
    private Date dateCreated;
    private List<Commit> parents;
}
