package ru.spbau.sazanovich.nikita.mygit.commands;

import org.jetbrains.annotations.NotNull;
import ru.spbau.sazanovich.nikita.mygit.MyGitIllegalArgumentException;
import ru.spbau.sazanovich.nikita.mygit.MyGitStateException;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Command class which removes a file from the filesystem and adds the removal to the index.
 */
class RemoveCommand extends Command {

    @NotNull
    private final String stringPath;

    RemoveCommand(@NotNull String stringPath, @NotNull InternalStateAccessor internalStateAccessor) {
        super(internalStateAccessor);
        this.stringPath = stringPath;
    }

    void perform() throws MyGitIllegalArgumentException, IOException, MyGitStateException {
        final Path path = internalStateAccessor.convertStringToPathRelativeToMyGitDirectory(stringPath);
        if (path == null) {
            return;
        }
        final Path completePath = internalStateAccessor.getMyGitDirectory().resolve(path);
        InternalStateAccessor.deleteFile(completePath);
        new StageCommand(stringPath, internalStateAccessor).perform();
    }

}
