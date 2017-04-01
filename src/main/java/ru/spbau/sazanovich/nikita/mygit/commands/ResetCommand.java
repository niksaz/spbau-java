package ru.spbau.sazanovich.nikita.mygit.commands;

import org.jetbrains.annotations.NotNull;
import ru.spbau.sazanovich.nikita.mygit.MyGitIllegalArgumentException;
import ru.spbau.sazanovich.nikita.mygit.MyGitStateException;
import ru.spbau.sazanovich.nikita.mygit.objects.Tree.TreeEdge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Command class which resets file's state to the corresponding HEAD's one.
 * Given file should be present in the filesystem.
 */
class ResetCommand extends Command {

    @NotNull
    private final String stringPath;

    ResetCommand(@NotNull String stringPath, @NotNull InternalStateAccessor internalStateAccessor) {
        super(internalStateAccessor);
        this.stringPath = stringPath;
    }

    void perform() throws MyGitIllegalArgumentException, MyGitStateException, IOException {
        final Path path = internalStateAccessor.convertStringToPathRelativeToMyGitDirectory(stringPath);
        if (path == null) {
            return;
        }
        final Path completePath = internalStateAccessor.getMyGitDirectory().resolve(path);
        if (!Files.exists(completePath)) {
            throw new MyGitIllegalArgumentException(stringPath + " should be present in the filesystem");
        }
        final TreeEdge edge = internalStateAccessor.findElementInHeadTree(path);
        if (edge == null) {
            throw new MyGitIllegalArgumentException(stringPath + " should be present in the HEAD's version");
        }
        internalStateAccessor.loadTreeEdge(edge, completePath);
    }
}
