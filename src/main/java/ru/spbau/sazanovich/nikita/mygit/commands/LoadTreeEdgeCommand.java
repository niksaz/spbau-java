package ru.spbau.sazanovich.nikita.mygit.commands;

import org.jetbrains.annotations.NotNull;
import ru.spbau.sazanovich.nikita.mygit.MyGitStateException;
import ru.spbau.sazanovich.nikita.mygit.objects.Blob;
import ru.spbau.sazanovich.nikita.mygit.objects.Tree.TreeEdge;
import ru.spbau.sazanovich.nikita.mygit.utils.FileSystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Low-level command class which loads the file corresponding to the
 * {@link TreeEdge} to the given path.
 */
class LoadTreeEdgeCommand extends Command {

    @NotNull
    private final TreeEdge edge;
    @NotNull
    private final Path path;

    LoadTreeEdgeCommand(@NotNull TreeEdge edge, @NotNull Path path,
                        @NotNull InternalStateAccessor internalStateAccessor) {
        super(internalStateAccessor);
        this.edge = edge;
        this.path = path;
    }

    void perform() throws IOException, MyGitStateException {
        if (edge.isDirectory()) {
            if (Files.exists(path) && !Files.isDirectory(path)) {
                FileSystem.deleteFile(path);
            }
            if (!Files.exists(path)) {
                Files.createDirectory(path);
            }
        } else {
            if (Files.exists(path) && Files.isDirectory(path)) {
                FileSystem.deleteFile(path);
            }
            if (!Files.exists(path)) {
                Files.createFile(path);
            }
            final Blob childBlob = internalStateAccessor.readBlob(edge.getHash());
            Files.write(path, childBlob.getContent());
        }
    }
}
