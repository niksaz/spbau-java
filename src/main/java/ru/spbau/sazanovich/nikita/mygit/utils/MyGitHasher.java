package ru.spbau.sazanovich.nikita.mygit.utils;

import org.jetbrains.annotations.NotNull;
import ru.spbau.sazanovich.nikita.mygit.exceptions.MyGitIllegalArgumentException;

import java.io.IOException;

/**
 * An object that may be used as a hashing tool for mapping objects in MyGit repository.
 */
public interface MyGitHasher {

    @NotNull
    String getHashFromObject(@NotNull Object object) throws IOException;

    @NotNull
    HashParts splitHash(@NotNull String hash) throws MyGitIllegalArgumentException;

    /**
     * Object which represents a split hash into first part (directory name) and last part (file name).
     */
    interface HashParts {

        /**
         * Gets first part of the hash.
         *
         * @return first part of the hash
         */
        @NotNull
        String getFirst();

        /**
         * Gets last part of the hash.
         *
         * @return last part of the hash
         */
        @NotNull
        String getLast();
    }
}
