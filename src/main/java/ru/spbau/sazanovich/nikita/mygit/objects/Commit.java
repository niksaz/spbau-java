package ru.spbau.sazanovich.nikita.mygit.objects;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.spbau.sazanovich.nikita.mygit.MyGitIOException;
import ru.spbau.sazanovich.nikita.mygit.utils.MyGitHasher;

import java.io.IOException;
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
@Getter
public class Commit implements Serializable, Comparable<Commit> {

    /**
     * Constant which is used through the library to identify Commit objects.
     */
    public static final String TYPE = "commit";

    @NotNull
    private final String treeHash;
    @NotNull
    private final String message;
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    @NotNull
    private final List<String> parentsHashes;
    @NotNull
    private final String author;
    @NotNull
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

    /**
     * Extracts commit's information to the library's user.
     *
     * @param hasher hasher to perform operation with
     * @return log object
     * @throws MyGitIOException if an error occurred while hashing
     */
    @NotNull
    public CommitLog extractLogInfo(@NotNull MyGitHasher hasher) throws MyGitIOException {
        try {
            return new CommitLog(hasher.getHashFromObject(this), getMessage(), getAuthor(), getDateCreated());
        } catch (IOException e) {
            throw new MyGitIOException("Error while hashing object", e);
        }
    }

    /**
     * Information about a commit that is given to the user of library.
     */
    @AllArgsConstructor
    @Getter
    static public class CommitLog {

        @NotNull
        private String revisionHash;
        @NotNull
        private String message;
        @NotNull
        private String author;
        @NotNull
        private Date dateCreated;
    }

    @NotNull
    private static String getUsername() {
        return System.getProperty("user.name");
    }
}
