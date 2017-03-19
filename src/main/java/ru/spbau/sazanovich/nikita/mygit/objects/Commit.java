package ru.spbau.sazanovich.nikita.mygit.objects;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
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
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Commit implements Serializable, Comparable<Commit> {

    /**
     * Constant which is used through the library to identify Commit objects.
     */
    public static final String TYPE = "commit";

    @NotNull
    @Getter
    private final String treeHash;
    @NotNull
    @Getter
    private final String message;
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    @NotNull
    @Getter
    private final List<String> parentsHashes;
    @NotNull
    @Getter
    private final String author;
    @NotNull
    @Getter
    private final Date dateCreated;


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

    @NotNull
    private static String getUsername() {
        return System.getProperty("user.name");
    }
}
