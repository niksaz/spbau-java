package ru.spbau.sazanovich.nikita.mygit.logs;

import org.jetbrains.annotations.NotNull;

/**
 * Represents the status of MyGit repository's HEAD.
 */
public class HeadStatus {

    @NotNull
    private String type;
    @NotNull
    private String name;

    public HeadStatus(@NotNull String type, @NotNull String name) {
        this.type = type;
        this.name = name;
    }

    @NotNull
    public String getType() {
        return type;
    }

    @NotNull
    public String getName() {
        return name;
    }
}
