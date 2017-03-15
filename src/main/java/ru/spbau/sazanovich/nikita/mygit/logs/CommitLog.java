package ru.spbau.sazanovich.nikita.mygit.logs;

import org.jetbrains.annotations.NotNull;

import java.util.Date;

/**
 * Information about a commit that is given to the user of library.
 */
public class CommitLog {

    @NotNull
    private String revisionHash;
    @NotNull
    private String message;
    @NotNull
    private String author;
    @NotNull
    private Date dateCreated;

    /**
     * Creates CommitLog with provided parameters.
     *
     * @param revisionHash hash of the corresponding commit file
     * @param message a message provided by the user
     * @param author an author of the commit
     * @param dateCreated date of the commit's creation
     */
    public CommitLog(@NotNull String revisionHash, @NotNull String message,
                     @NotNull String author, @NotNull Date dateCreated) {
        this.revisionHash = revisionHash;
        this.message = message;
        this.author = author;
        this.dateCreated = dateCreated;
    }

    /**
     * Gets hash of the corresponding commit file.
     *
     * @return hash of the corresponding commit file
     */
    @NotNull
    public String getRevisionHash() {
        return revisionHash;
    }

    /**
     * Gets a message provided by the user.
     *
     * @return a message provided by the user
     */
    @NotNull
    public String getMessage() {
        return message;
    }

    /**
     * Gets an author of the commit.
     *
     * @return an author of the commit
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
}
