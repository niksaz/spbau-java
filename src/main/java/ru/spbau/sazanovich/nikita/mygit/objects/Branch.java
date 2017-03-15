package ru.spbau.sazanovich.nikita.mygit.objects;

import org.jetbrains.annotations.NotNull;

/**
 * Class to represent branches info given to the user.
 */
public class Branch {

    /**
     * Constant which is used through the library to identify Branch objects.
     */
    public static final String TYPE = "branch";

    @NotNull
    private String name;

    /**
     * Constructs an object with given name.
     *
     * @param name name of the branch
     */
    public Branch(@NotNull String name) {
        this.name = name;
    }

    /**
     * Gets name of the branch.
     *
     * @return name of the branch
     */
    @NotNull
    public String getName() {
        return name;
    }
}
