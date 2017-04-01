package ru.spbau.sazanovich.nikita.mygit.commands;

import org.jetbrains.annotations.NotNull;
import ru.spbau.sazanovich.nikita.mygit.MyGitIllegalArgumentException;
import ru.spbau.sazanovich.nikita.mygit.MyGitStateException;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Base class for commands working with index.
 * Allows to combine current index files and given ones with the provided action.
 */
class IndexUpdateCommand extends Command {

    @NotNull
    private List<String> arguments;
    @NotNull
    private Function<Set<Path>, Consumer<Path>> action;

    IndexUpdateCommand(@NotNull List<String> arguments, @NotNull Function<Set<Path>, Consumer<Path>> action,
                       @NotNull InternalStateAccessor internalStateAccessor) {
        super(internalStateAccessor);
        this.arguments = arguments;
        this.action = action;
    }

    void perform() throws MyGitStateException, MyGitIllegalArgumentException, IOException {
        final List<Path> argsPaths = convertStringsToPaths(arguments);
        final Set<Path> indexedPaths = internalStateAccessor.readIndexPaths();
        final Consumer<Path> indexUpdater = action.apply(indexedPaths);
        argsPaths.forEach(indexUpdater);
        internalStateAccessor.writeIndexPaths(indexedPaths);
    }

    @NotNull
    private List<Path> convertStringsToPaths(@NotNull List<String> args) throws MyGitIllegalArgumentException {
        final List<Path> paths = new ArrayList<>();
        for (String stringPath : args) {
            Path path;
            try {
                path = Paths.get(stringPath);
            } catch (InvalidPathException e) {
                throw new MyGitIllegalArgumentException("invalid path -- " + e.getMessage());
            }
            if (!path.isAbsolute()) {
                path = internalStateAccessor.getCurrentDirectory().resolve(path).normalize();
            }
            if (!path.startsWith(internalStateAccessor.getMyGitDirectory())) {
                throw new MyGitIllegalArgumentException(
                        "files should be located in the mygit repository's directory, but an argument is " + path);
            }
            path = internalStateAccessor.relativizeWithMyGitDirectory(path);
            if (!InternalStateAccessor.pathContainsMyGitAsSubpath(path)) {
                paths.add(path);
            }
        }
        return paths;
    }
}
