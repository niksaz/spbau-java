package ru.spbau.sazanovich.nikita.mygit.commands;

import org.jetbrains.annotations.NotNull;
import ru.spbau.sazanovich.nikita.mygit.MyGitIllegalArgumentException;
import ru.spbau.sazanovich.nikita.mygit.MyGitStateException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Command class which removes paths from the current index.
 */
class UnstageCommand extends Command {

    @NotNull
    private final List<String> arguments;

    UnstageCommand(@NotNull List<String> arguments, @NotNull InternalStateAccessor internalStateAccessor) {
        super(internalStateAccessor);
        this.arguments = arguments;
    }

    void perform() throws MyGitIllegalArgumentException, IOException, MyGitStateException {
        final Function<Set<Path>, Consumer<Path>> action =
                paths -> (Consumer<Path>) path -> {
                    if (paths.contains(path)) {
                        paths.remove(path);
                    }
                };
        new IndexUpdateCommand(arguments, action, internalStateAccessor).perform();
    }
}
