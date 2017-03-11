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

    public CommitLog(@NotNull String revisionHash, @NotNull String message,
                     @NotNull String author, @NotNull Date dateCreated) {
        this.revisionHash = revisionHash;
        this.message = message;
        this.author = author;
        this.dateCreated = dateCreated;
    }

    @NotNull
    public String getRevisionHash() {
        return revisionHash;
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
}
