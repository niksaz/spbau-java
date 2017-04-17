package ru.spbau.sazanovich.nikita.mygit.commands;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbau.sazanovich.nikita.mygit.MyGitIOException;
import ru.spbau.sazanovich.nikita.mygit.MyGitStateException;
import ru.spbau.sazanovich.nikita.mygit.objects.Blob;
import ru.spbau.sazanovich.nikita.mygit.objects.FileDifference;
import ru.spbau.sazanovich.nikita.mygit.objects.FileDifferenceStageStatus;
import ru.spbau.sazanovich.nikita.mygit.objects.Tree;
import ru.spbau.sazanovich.nikita.mygit.utils.FileSystem;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.spbau.sazanovich.nikita.mygit.objects.FileDifferenceStageStatus.*;
import static ru.spbau.sazanovich.nikita.mygit.objects.FileDifferenceType.*;

/**
 * Command class which gets differences between current MyGit repository's HEAD state and the filesystem state.
 */
class StatusCommand extends Command {

    StatusCommand(@NotNull InternalStateAccessor internalStateAccessor) {
        super(internalStateAccessor);
    }

    @NotNull
    List<FileDifference> perform() throws MyGitStateException, MyGitIOException {
        internalStateAccessor.getLogger().trace("StatusCommand -- started");
        final Tree tree = internalStateAccessor.getHeadTree();
        final Set<Path> indexedPaths = internalStateAccessor.readIndexPaths();
        final List<FileDifference> differences =
                getTreeDifferenceList(tree, internalStateAccessor.getMyGitDirectory(), indexedPaths);
        internalStateAccessor.getLogger().trace("StatusCommand -- completed");
        return differences;
    }

    /**
     * Returns a list of differences in files between filesystem and MyGit's HEAD status.
     * The list contains relative paths.
     */
    @NotNull
    private List<FileDifference> getTreeDifferenceList(@Nullable Tree tree, @NotNull Path prefixPath,
                                                       @NotNull Set<Path> indexedPaths)
            throws MyGitStateException, MyGitIOException {
        final List<Path> filePaths =
                FileSystem
                        .list(prefixPath)
                        .filter(path -> !internalStateAccessor.isAbsolutePathRepresentsInternal(path))
                        .collect(Collectors.toList());
        final List<FileDifference> fileDifferenceList = new ArrayList<>();
        final List<Tree.TreeEdge> childrenList = tree == null ? new ArrayList<>() : tree.getChildren();
        for (Tree.TreeEdge child : childrenList) {
            final Path childPath = Paths.get(prefixPath.toString(), child.getName());
            if (filePaths.contains(childPath)) {
                filePaths.remove(childPath);
            }
            addChildDifferenceToList(child, childPath, indexedPaths, fileDifferenceList);
        }
        for (Path path : filePaths) {
            final Path relativePath = internalStateAccessor.relativizeWithMyGitDirectory(path);
            if (indexedPaths.contains(relativePath)) {
                fileDifferenceList.add(new FileDifference(relativePath, ADDITION, TO_BE_COMMITTED));
                if (Files.isDirectory(path)) {
                    fileDifferenceList.addAll(getTreeDifferenceList(null, path, indexedPaths));
                }
            } else {
                fileDifferenceList.add(new FileDifference(relativePath, ADDITION, UNTRACKED));
            }
        }
        return fileDifferenceList;
    }

    private void addChildDifferenceToList(@NotNull Tree.TreeEdge child, @NotNull Path path,
                                          @NotNull Set<Path> indexedPaths,
                                          @NotNull List<FileDifference> differences)
            throws MyGitStateException, MyGitIOException {
        final Path relativePath = internalStateAccessor.relativizeWithMyGitDirectory(path);
        final FileDifferenceStageStatus status =
                indexedPaths.contains(relativePath) ? TO_BE_COMMITTED : NOT_STAGED_FOR_COMMIT;
        if (child.isDirectory()) {
            final Tree childTree = internalStateAccessor.readTree(child.getHash());
            if (Files.exists(path)) {
                if (Files.isDirectory(path)) {
                    differences.addAll(getTreeDifferenceList(childTree, path, indexedPaths));
                } else {
                    differences.add(new FileDifference(relativePath, MODIFICATION, status));
                    for (Tree.TreeEdge object : childTree.getChildren()) {
                        Path objectPath = Paths.get(path.toString(), object.getName());
                        objectPath = internalStateAccessor.relativizeWithMyGitDirectory(objectPath);
                        differences.add(new FileDifference(objectPath, REMOVAL, status));
                    }
                }
            } else {
                differences.add(new FileDifference(relativePath, REMOVAL, status));
            }
            return;
        }
        if (child.isFile()) {
            final Blob childBlob = internalStateAccessor.readBlob(child.getHash());
            if (Files.exists(path)) {
                if (Files.isDirectory(path)) {
                    differences.add(new FileDifference(relativePath, MODIFICATION, status));
                    if (indexedPaths.contains(relativePath)) {
                        differences.addAll(getTreeDifferenceList(null, path, indexedPaths));
                    }
                } else {
                    final byte[] committedContent = childBlob.getContent();
                    final byte[] currentContent = FileSystem.readAllBytes(path);
                    if (!Arrays.equals(committedContent, currentContent)) {
                        differences.add(new FileDifference(relativePath, MODIFICATION, status));
                    }
                }
            } else {
                differences.add(new FileDifference(relativePath, REMOVAL, status));
            }
            return;
        }
        throw new MyGitStateException("met an unknown type while traversing the tree -- " + child.getType());
    }
}
