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

    /**
     * Consider two branches equal if they have the same names.
     *
     * @param that other object
     * @return {@code true} if object are considered equal; {@code false} otherwise
     */
    @Override
    public boolean equals(Object that) {
        return that instanceof Branch && name.equals(((Branch) that).name);
    }

    /**
     * Computes hash code as name's hash code.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
