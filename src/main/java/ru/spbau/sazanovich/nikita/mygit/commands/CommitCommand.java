package ru.spbau.sazanovich.nikita.mygit.commands;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbau.sazanovich.nikita.mygit.MyGitStateException;
import ru.spbau.sazanovich.nikita.mygit.objects.Blob;
import ru.spbau.sazanovich.nikita.mygit.objects.Commit;
import ru.spbau.sazanovich.nikita.mygit.objects.Tree;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Command class which commits all indexed files and moves head to the newly created commit.
 * New commit's message should be provided.
 */
class CommitCommand extends Command {

    @NotNull
    private final String message;

    CommitCommand(@NotNull String message, @NotNull InternalStateAccessor internalStateAccessor) {
        super(internalStateAccessor);
        this.message = message;
    }

    void perform() throws MyGitStateException, IOException {
        internalStateAccessor.getLogger().trace("CommitCommand -- started with message=" + message);
        final Tree tree = internalStateAccessor.getHeadTree();
        final Set<Path> indexedPaths = internalStateAccessor.readIndexPaths();
        final String rebuiltTreeHash = rebuildTree(tree, internalStateAccessor.getMyGitDirectory(), indexedPaths);
        final List<String> parentsHashes = new ArrayList<>();
        parentsHashes.add(internalStateAccessor.getHeadCommitHash());
        final Commit commit = new Commit(rebuiltTreeHash, message, parentsHashes);
        final String commitHash = internalStateAccessor.map(commit);
        internalStateAccessor.moveHeadToCommitHash(commitHash);
        new UnstageAllCommand(internalStateAccessor).perform();
        internalStateAccessor.getLogger().trace("CommitCommand -- completed");
    }

    private String rebuildTree(@Nullable Tree tree, @NotNull Path prefixPath, @NotNull Set<Path> indexedPaths)
            throws MyGitStateException, IOException {
        final List<Path> filePaths =
                Files
                        .list(prefixPath)
                        .filter(path -> !internalStateAccessor.isAbsolutePathRepresentsInternal(path))
                        .collect(Collectors.toList());
        final Tree rebuiltTree = new Tree();
        final List<Tree.TreeEdge> childrenList = tree == null ? new ArrayList<>() : tree.getChildren();
        for (Tree.TreeEdge child : childrenList) {
            final Path childPath = Paths.get(prefixPath.toString(), child.getName());
            if (filePaths.contains(childPath)) {
                filePaths.remove(childPath);
            }
            final Tree.TreeEdge rebuiltTreeEdge = rebuildTreeEdge(child, childPath, indexedPaths);
            if (rebuiltTreeEdge != null) {
                rebuiltTree.addChild(rebuiltTreeEdge);
            }
        }
        for (Path path : filePaths) {
            if (indexedPaths.contains(internalStateAccessor.relativizeWithMyGitDirectory(path))) {
                if (Files.isDirectory(path)) {
                    final String treeHash = rebuildTree(null, path, indexedPaths);
                    rebuiltTree.addChild(new Tree.TreeEdge(treeHash, path.getFileName().toString(), Tree.TYPE));
                } else {
                    final String blobHash = internalStateAccessor.createBlobFromPath(path);
                    rebuiltTree.addChild(new Tree.TreeEdge(blobHash, path.getFileName().toString(), Blob.TYPE));
                }
            }
        }
        return internalStateAccessor.map(rebuiltTree);
    }

    @Nullable
    private Tree.TreeEdge rebuildTreeEdge(@NotNull Tree.TreeEdge child, @NotNull Path path,
                                          @NotNull Set<Path> indexedPaths)
            throws MyGitStateException, IOException {
        final boolean isPresentInFilesystem = Files.exists(path);
        switch (child.getType()) {
            case Tree.TYPE:
                final Tree childTree = internalStateAccessor.readTree(child.getHash());
                if (isPresentInFilesystem) {
                    if (path.toFile().isDirectory()) {
                        final String childHash = rebuildTree(childTree, path, indexedPaths);
                        return new Tree.TreeEdge(childHash, child.getName(), child.getType());
                    } else {
                        return updateBlobIfIndexed(child, path, indexedPaths);
                    }
                } else if (!indexedPaths.contains(internalStateAccessor.relativizeWithMyGitDirectory(path))) {
                    return child;
                }
                break;
            case Blob.TYPE:
                final Blob childBlob = internalStateAccessor.readBlob(child.getHash());
                if (isPresentInFilesystem) {
                    if (path.toFile().isDirectory()) {
                        if (indexedPaths.contains(internalStateAccessor.relativizeWithMyGitDirectory(path))) {
                            final String childHash = rebuildTree(null, path, indexedPaths);
                            return new Tree.TreeEdge(childHash, child.getName(), Tree.TYPE);
                        } else {
                            return child;
                        }
                    } else {
                        final byte[] committedContent = childBlob.getContent();
                        final byte[] currentContent = Files.readAllBytes(path);
                        if (Arrays.equals(committedContent, currentContent)) {
                            return child;
                        } else {
                            return updateBlobIfIndexed(child, path, indexedPaths);
                        }
                    }
                } else if (!indexedPaths.contains(internalStateAccessor.relativizeWithMyGitDirectory(path))) {
                    return child;
                }
                break;
            default:
                throw new MyGitStateException("met an unknown type while traversing the tree -- " + child.getType());
        }
        return null;
    }

    @NotNull
    private Tree.TreeEdge updateBlobIfIndexed(@NotNull Tree.TreeEdge object, @NotNull Path path,
                                              @NotNull Set<Path> indexedPaths)
            throws MyGitStateException, IOException {
        if (indexedPaths.contains(internalStateAccessor.relativizeWithMyGitDirectory(path))) {
            final String blobHash = internalStateAccessor.createBlobFromPath(path);
            return new Tree.TreeEdge(blobHash, object.getName(), Blob.TYPE);
        } else {
            return object;
        }
    }
}
