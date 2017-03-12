package ru.spbau.sazanovich.nikita.mygit.objects;

import org.jetbrains.annotations.NotNull;

/**
 * Class to represent branches info given to the user.
 */
public class Branch {

    public static final String TYPE = "branch";

    @NotNull
    private String name;

    public Branch(@NotNull String name) {
        this.name = name;
    }

    @NotNull
    public String getName() {
        return name;
    }
}
