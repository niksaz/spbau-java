package ru.spbau.sazanovich.nikita.mygit.status;

/**
 * Enum which represents a {@link FileDifference FileDifference's} type of difference of a filesystem file version
 * compared to the HEAD's MyGit one.
 */
public enum FileDifferenceType {
    /**
     * Represents a new file.
     */
    ADDITION,
    /**
     * Represents a deletion of a file.
     */
    REMOVAL,
    /**
     * Represents file's content modification.
     */
    MODIFICATION
}
