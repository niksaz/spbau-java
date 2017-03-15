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

    /**
     * Constructs the object with given parameters.
     *
     * @param type type of the head (either Branch.TYPE or Commit.TYPE)
     * @param name info of the type's object (for Branch it is branch's name, for Commit it is commit's hash)
     */
    public HeadStatus(@NotNull String type, @NotNull String name) {
        this.type = type;
        this.name = name;
    }

    /**
     * Gets type of the head.
     *
     * @return type of the head
     */
    @NotNull
    public String getType() {
        return type;
    }

    /**
     * Gets info of the type's object.
     *
     * @return info of the type's object
     */
    @NotNull
    public String getName() {
        return name;
    }
}
