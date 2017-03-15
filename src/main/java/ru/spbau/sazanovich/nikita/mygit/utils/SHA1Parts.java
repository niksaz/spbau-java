package ru.spbau.sazanovich.nikita.mygit.utils;

import org.jetbrains.annotations.NotNull;
import ru.spbau.sazanovich.nikita.mygit.exceptions.MyGitIllegalArgumentException;

/**
 * Used during internal storage. Splitting on a directory name and a file name:
 * first 2 chars goes to the first part, others -- to the last part.
 */
public class SHA1Parts {

    private static final int FIRST_PART_ENDS = 2;
    private static final int HASH_LENGTH = 40;

    @NotNull
    private String first;
    @NotNull
    private String last;

    /**
     * Constructs hash parts from the given string.
     *
     * @param hash string which represents sha-1 hash
     * @throws MyGitIllegalArgumentException if the string's length isn't 40 characters
     */
    public SHA1Parts(@NotNull String hash) throws MyGitIllegalArgumentException {
        if (hash.length() != HASH_LENGTH) {
            throw new MyGitIllegalArgumentException("hash length isn't " + HASH_LENGTH);
        }
        first = hash.substring(0, FIRST_PART_ENDS);
        last = hash.substring(FIRST_PART_ENDS);
    }

    /**
     * Gets first part of the hash.
     *
     * @return first part of the hash
     */
    @NotNull
    public String getFirst() {
        return first;
    }

    /**
     * Gets last part of the hash.
     *
     * @return last part of the hash
     */
    @NotNull
    public String getLast() {
        return last;
    }
}