package ru.spbau.sazanovich.nikita.mygit.commands;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbau.sazanovich.nikita.mygit.MyGitStateException;
import ru.spbau.sazanovich.nikita.mygit.objects.Blob;
import ru.spbau.sazanovich.nikita.mygit.objects.FileDifference;
import ru.spbau.sazanovich.nikita.mygit.objects.Tree;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.spbau.sazanovich.nikita.mygit.objects.FileDifferenceStageStatus.NOT_STAGED_FOR_COMMIT;
import static ru.spbau.sazanovich.nikita.mygit.objects.FileDifferenceStageStatus.TO_BE_COMMITTED;
import static ru.spbau.sazanovich.nikita.mygit.objects.FileDifferenceStageStatus.UNTRACKED;
import static ru.spbau.sazanovich.nikita.mygit.objects.FileDifferenceType.ADDITION;
import static ru.spbau.sazanovich.nikita.mygit.objects.FileDifferenceType.MODIFICATION;
import static ru.spbau.sazanovich.nikita.mygit.objects.FileDifferenceType.REMOVAL;

/**
 * Command class which gets differences between current MyGit repository's HEAD state and the filesystem state.
 */
class StatusCommand extends Command {

    StatusCommand(@NotNull InternalStateAccessor internalStateAccessor) {
        super(internalStateAccessor);
    }

    @NotNull
    List<FileDifference> perform() throws MyGitStateException, IOException {
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
    private List<FileDifference> getTreeDifferenceList(@Nullable Tree tree, @NotNull Path prefixPath, @NotNull Set<Path> indexedPaths)
            throws MyGitStateException, IOException {
        final List<Path> filePaths =
                Files
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
            throws MyGitStateException, IOException {
        final Path relativePath = internalStateAccessor.relativizeWithMyGitDirectory(path);
        final boolean isPresentInFilesystem = Files.exists(path);
        if (child.getType().equals(Tree.TYPE)) {
            final Tree childTree = internalStateAccessor.readTree(child.getHash());
            if (isPresentInFilesystem) {
                if (Files.isDirectory(path)) {
                    differences.addAll(getTreeDifferenceList(childTree, path, indexedPaths));
                } else {
                    if (indexedPaths.contains(relativePath)) {
                        differences.add(new FileDifference(relativePath, MODIFICATION, TO_BE_COMMITTED));
                        for (Tree.TreeEdge object : childTree.getChildren()) {
                            Path objectPath = Paths.get(path.toString(), object.getName());
                            objectPath = internalStateAccessor.relativizeWithMyGitDirectory(objectPath);
                            differences.add(new FileDifference(objectPath, REMOVAL, TO_BE_COMMITTED));
                        }
                    } else {
                        differences.add(new FileDifference(relativePath, MODIFICATION, NOT_STAGED_FOR_COMMIT));
                        for (Tree.TreeEdge object : childTree.getChildren()) {
                            Path objectPath = Paths.get(path.toString(), object.getName());
                            objectPath = internalStateAccessor.relativizeWithMyGitDirectory(objectPath);
                            differences.add(new FileDifference(objectPath, REMOVAL, NOT_STAGED_FOR_COMMIT));
                        }
                    }
                }
            } else if (indexedPaths.contains(relativePath)) {
                differences.add(new FileDifference(relativePath, REMOVAL, TO_BE_COMMITTED));
            } else {
                differences.add(new FileDifference(relativePath, REMOVAL, NOT_STAGED_FOR_COMMIT));
            }
            return;
        }
        if (child.getType().equals(Blob.TYPE)) {
            final Blob childBlob = internalStateAccessor.readBlob(child.getHash());
            if (isPresentInFilesystem) {
                if (Files.isDirectory(path)) {
                    if (indexedPaths.contains(relativePath)) {
                        differences.add(new FileDifference(relativePath, MODIFICATION, TO_BE_COMMITTED));
                        differences.addAll(getTreeDifferenceList(null, path, indexedPaths));
                    } else {
                        differences.add(new FileDifference(relativePath, MODIFICATION, NOT_STAGED_FOR_COMMIT));
                    }
                } else {
                    final byte[] committedContent = childBlob.getContent();
                    final byte[] currentContent = Files.readAllBytes(path);
                    if (!Arrays.equals(committedContent, currentContent)) {
                        if (indexedPaths.contains(relativePath)) {
                            differences.add(new FileDifference(relativePath, MODIFICATION, TO_BE_COMMITTED));
                        } else {
                            differences.add(new FileDifference(relativePath, MODIFICATION, NOT_STAGED_FOR_COMMIT));
                        }
                    }
                }
            } else if (indexedPaths.contains(relativePath)) {
                differences.add(new FileDifference(relativePath, REMOVAL, TO_BE_COMMITTED));
            } else {
                differences.add(new FileDifference(relativePath, REMOVAL, NOT_STAGED_FOR_COMMIT));
            }
            return;
        }
        throw new MyGitStateException("met an unknown type while traversing the tree -- " + child.getType());
    }
}
