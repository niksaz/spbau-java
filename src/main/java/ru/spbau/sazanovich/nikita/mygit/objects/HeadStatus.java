package ru.spbau.sazanovich.nikita.mygit.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the status of MyGit repository's HEAD.
 */
@AllArgsConstructor
@Getter
public class HeadStatus {

    @NotNull
    private String type;
    @NotNull
    private String name;
}
