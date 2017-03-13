package ru.spbau.sazanovich.nikita.mygit.objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Object which stores commit's message and reference to base {@link Tree} object.
 */
public class Commit implements Serializable, Comparable<Commit> {

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
    private List<String> parentsHashes;

    public Commit(@NotNull String treeHash) {
        this(treeHash, "repository initialized", new ArrayList<>());
    }

    public Commit(@NotNull String treeHash, @NotNull String message, @NotNull List<String> parentsHashes) {
        this(treeHash, message, parentsHashes, getUsername(), new Date());
    }

    private Commit(@NotNull String treeHash, @NotNull String message, @NotNull List<String> parentsHashes,
                   @NotNull String author, @NotNull Date date) {
        this.treeHash = treeHash;
        this.message = message;
        this.author = author;
        this.dateCreated = date;
        this.parentsHashes = parentsHashes;
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
    public List<String> getParentsHashes() {
        return parentsHashes;
    }

    @NotNull
    private static String getUsername() {
        return System.getProperty("user.name");
    }

    @Override
    public boolean equals(@Nullable  Object obj) {
        return obj instanceof Commit && compareTo(((Commit) obj)) == 0;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + treeHash.hashCode();
        result = 31 * result + message.hashCode();
        result = 31 * result + author.hashCode();
        result = 31 * result + dateCreated.hashCode();
        result = 31 * result + parentsHashes.hashCode();
        return result;
    }

    @Override
    public int compareTo(@NotNull Commit that) {
        int result = dateCreated.compareTo(that.dateCreated);
        if (result != 0) {
            return result;
        }
        result = author.compareTo(that.author);
        if (result != 0) {
            return result;
        }
        result = message.compareTo(that.author);
        if (result != 0) {
            return result;
        }
        result = treeHash.compareTo(that.treeHash);
        if (result != 0) {
            return result;
        }
        result = Integer.valueOf(parentsHashes.size()).compareTo(that.parentsHashes.size());
        if (result != 0) {
            return result;
        }
        for (int i = 0; i < parentsHashes.size(); i++) {
            result = parentsHashes.get(i).compareTo(that.parentsHashes.get(i));
            if (result != 0) {
                return result;
            }
        }
        return 0;
    }

}
