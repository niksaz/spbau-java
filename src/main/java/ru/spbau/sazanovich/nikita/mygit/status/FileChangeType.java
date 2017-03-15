package ru.spbau.sazanovich.nikita.mygit.status;

/**
 * Enum which represents different types of modifications made to files.
 */
public enum FileChangeType {
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
