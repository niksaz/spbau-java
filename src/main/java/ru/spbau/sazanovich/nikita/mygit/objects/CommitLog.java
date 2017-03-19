package ru.spbau.sazanovich.nikita.mygit.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

/**
 * Information about a commit that is given to the user of library.
 */
@AllArgsConstructor
@Getter
public class CommitLog {

    @NotNull
    private String revisionHash;
    @NotNull
    private String message;
    @NotNull
    private String author;
    @NotNull
    private Date dateCreated;
}
