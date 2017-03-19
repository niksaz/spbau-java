package ru.spbau.sazanovich.nikita.mygit.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

/**
 * Information about a commit that is given to the user of library.
 */
@AllArgsConstructor
public class CommitLog {

    @NotNull
    @Getter
    private String revisionHash;
    @NotNull
    @Getter
    private String message;
    @NotNull
    @Getter
    private String author;
    @NotNull
    @Getter
    private Date dateCreated;
}
