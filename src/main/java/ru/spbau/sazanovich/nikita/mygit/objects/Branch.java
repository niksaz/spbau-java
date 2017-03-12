package ru.spbau.sazanovich.nikita.mygit.objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    @Override
    public boolean equals(@Nullable Object that) {
        return that instanceof Branch && name.equals(((Branch) that).getName());
    }
}
