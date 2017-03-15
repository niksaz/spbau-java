package ru.spbau.sazanovich.nikita.mygit.objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Object which stores commit's associated information -- hash of the base {@link Tree} object,
 * message, author, date of creation and parent commits.
 */
public class Commit implements Serializable, Comparable<Commit> {

    /**
     * Constant which is used through the library to identify Commit objects.
     */
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

    /**
     * Constructs a commit with given treeHash, default message -- "repository initialized", empty parent's list,
     * current date and current OS user.
     *
     * @param treeHash hash of the base tree
     */
    public Commit(@NotNull String treeHash) {
        this(treeHash, "repository initialized", new ArrayList<>());
    }

    /**
     * Constructs a commit with given treeHash, message and parent's list. Uses current date and OS user as two others
     * parameters.
     *
     * @param treeHash hash of the base tree
     * @param message commit's message
     * @param parentsHashes list of parent commit's hashes
     */
    public Commit(@NotNull String treeHash, @NotNull String message, @NotNull List<String> parentsHashes) {
        this(treeHash, message, parentsHashes, getUsername(), new Date());
    }

    /**
     * Gets hash of the base tree.
     *
     * @return hash of the base tree
     */
    @NotNull
    public String getTreeHash() {
        return treeHash;
    }

    /**
     * Gets commit's message.
     *
     * @return commit's message
     */
    @NotNull
    public String getMessage() {
        return message;
    }

    /**
     * Gets author of the commit.
     *
     * @return author of the commit
     */
    @NotNull
    public String getAuthor() {
        return author;
    }

    /**
     * Gets date of the commit's creation.
     *
     * @return date of the commit's creation
     */
    @NotNull
    public Date getDateCreated() {
        return dateCreated;
    }

    /**
     * Gets list of parent commit's hashes.
     *
     * @return list of parent commit's hashes
     */
    @NotNull
    public List<String> getParentsHashes() {
        return parentsHashes;
    }

    /**
     * Compares this object to the other. They are considered equal iff
     * the other one is instance of Commit and all fields are equal.
     *
     * @param that object to which this one is compared
     * @return {@code true} if two object are equal; {@code false} otherwise
     */
    @Override
    public boolean equals(@Nullable  Object that) {
        return that instanceof Commit && compareTo(((Commit) that)) == 0;
    }

    /**
     * Hashes the object. Uses all fields during hashing.
     *
     * @return hash code
     */
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

    /**
     * Compares this commit to the other. Fields are compared in this order:
     * date, author, message, treeHash and parentsHashes.
     *
     * @param that commit to which this is compared
     * @return a negative integer, zero, or a positive integer as this object
     *          is less than, equal to, or greater than the specified object.
     */
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
        result = message.compareTo(that.message);
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

    private Commit(@NotNull String treeHash, @NotNull String message, @NotNull List<String> parentsHashes,
                   @NotNull String author, @NotNull Date date) {
        this.treeHash = treeHash;
        this.message = message;
        this.author = author;
        this.dateCreated = date;
        this.parentsHashes = parentsHashes;
    }

    @NotNull
    private static String getUsername() {
        return System.getProperty("user.name");
    }
}
