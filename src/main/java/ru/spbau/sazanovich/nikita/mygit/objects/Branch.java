package ru.spbau.sazanovich.nikita.mygit.objects;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * Class to represent branches info given to the user.
 */
@EqualsAndHashCode
@AllArgsConstructor
public class Branch {

    /**
     * Constant which is used through the library to identify Branch objects.
     */
    public static final String TYPE = "branch";

    @NotNull
    @Getter
    private final String name;
}
