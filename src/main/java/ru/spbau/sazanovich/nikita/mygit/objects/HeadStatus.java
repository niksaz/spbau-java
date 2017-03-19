package ru.spbau.sazanovich.nikita.mygit.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the status of MyGit repository's HEAD.
 */
@AllArgsConstructor
public class HeadStatus {

    @NotNull
    @Getter
    private String type;
    @NotNull
    @Getter
    private String name;
}
