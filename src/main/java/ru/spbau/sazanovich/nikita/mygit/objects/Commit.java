package ru.spbau.sazanovich.nikita.mygit.objects;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Object which stores commit's message and reference to base {@link Tree} object.
 */
public class Commit implements Serializable {

    public static final String TYPE = "commit";

    @NotNull
    private String treeHash;
    @NotNull
    private String message;
    @NotNull
    private String author;
    @NotNull
    private Date dateCreated;
    @NotNull
    private List<Commit> parents;

    public Commit(@NotNull String hash) {
        this(hash, "repository initialized", getUsername(), new Date(), new ArrayList<>());
    }

    public Commit(@NotNull String hash, @NotNull String message, @NotNull String author,
                  @NotNull Date date, @NotNull List<Commit> parents) {
        this.treeHash = hash;
        this.message = message;
        this.author = author;
        this.dateCreated = date;
        this.parents = parents;
    }

    @NotNull
    public String getTreeHash() {
        return treeHash;
    }

    @NotNull
    public String getMessage() {
        return message;
    }

    @NotNull
    public String getAuthor() {
        return author;
    }

    @NotNull
    public Date getDateCreated() {
        return dateCreated;
    }

    @NotNull
    public List<Commit> getParents() {
        return parents;
    }

    @NotNull
    private static String getUsername() {
        return System.getProperty("user.name");
    }
}
