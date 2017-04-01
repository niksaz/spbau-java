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
 * Command class which adds paths to the current index.
 */
class StageCommand extends Command {

    @NotNull
    private List<String> arguments;

    StageCommand(@NotNull List<String> arguments, @NotNull InternalStateAccessor internalStateAccessor) {
        super(internalStateAccessor);
        this.arguments = arguments;
    }

    void perform() throws MyGitIllegalArgumentException, IOException, MyGitStateException {
        final Function<Set<Path>, Consumer<Path>> action =
                paths -> (Consumer<Path>) path -> {
                    if (!paths.contains(path)) {
                        paths.add(path);
                    }
                };
        new IndexUpdateCommand(arguments, action, internalStateAccessor).perform();
    }
}
