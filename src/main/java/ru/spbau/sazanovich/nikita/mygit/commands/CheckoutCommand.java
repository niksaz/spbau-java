package ru.spbau.sazanovich.nikita.mygit.commands;

import org.jetbrains.annotations.NotNull;
import ru.spbau.sazanovich.nikita.mygit.MyGitIllegalArgumentException;
import ru.spbau.sazanovich.nikita.mygit.MyGitMissingPrerequisitesException;
import ru.spbau.sazanovich.nikita.mygit.MyGitStateException;
import ru.spbau.sazanovich.nikita.mygit.objects.*;
import ru.spbau.sazanovich.nikita.mygit.objects.Tree.TreeEdge;
import ru.spbau.sazanovich.nikita.mygit.utils.FileSystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Command class which checkouts a revision and moves HEAD there.
 *
 * Replaces all files which differs from the HEAD's ones if such exist. Index should be empty before checking out.
 */
class CheckoutCommand extends Command {

    @NotNull
    private final String revisionName;

    CheckoutCommand(@NotNull String revisionName, @NotNull InternalStateAccessor internalStateAccessor) {
        super(internalStateAccessor);
        this.revisionName = revisionName;
    }

    void perform()
            throws MyGitStateException, MyGitMissingPrerequisitesException, MyGitIllegalArgumentException, IOException {
        internalStateAccessor.getLogger().trace("CheckoutCommand -- started with revision=" + revisionName);
        if (!internalStateAccessor.readIndexPaths().isEmpty()) {
            throw new MyGitMissingPrerequisitesException("staging area should be empty before a checkout operation");
        }
        final Commit fromCommit = internalStateAccessor.getHeadCommit();
        String toCommitHash;
        String toHeadType;
        final List<Branch> branchList = new ListBranchesCommand(internalStateAccessor).perform();
        if (branchList.contains(new Branch(revisionName))) {
            toCommitHash = internalStateAccessor.getBranchCommitHash(revisionName);
            toHeadType = Branch.TYPE;
        } else {
            if (internalStateAccessor.listCommitHashes().contains(revisionName)) {
                toCommitHash = revisionName;
                toHeadType = Commit.TYPE;
            } else {
                throw new MyGitIllegalArgumentException("there is no such revision -- " + revisionName);
            }
        }
        final Commit toCommit = internalStateAccessor.readCommit(toCommitHash);
        moveFromCommitToCommit(fromCommit, toCommit);
        final HeadStatus toHeadStatus = new HeadStatus(toHeadType, revisionName);
        internalStateAccessor.setHeadStatus(toHeadStatus);
        internalStateAccessor.getLogger().trace("CheckoutCommand -- completed");
    }

    private void moveFromCommitToCommit(@NotNull Commit fromCommit, Commit toCommit)
            throws MyGitStateException, IOException {
        final Tree fromTree = internalStateAccessor.readTree(fromCommit.getTreeHash());
        final Tree toTree = internalStateAccessor.readTree(toCommit.getTreeHash());
        deleteFilesFromTree(fromTree, internalStateAccessor.getMyGitDirectory());
        loadFilesFromTree(toTree, internalStateAccessor.getMyGitDirectory());
    }

    private void loadFilesFromTree(@NotNull Tree tree, @NotNull Path path) throws MyGitStateException, IOException {
        for (TreeEdge edge : tree.getChildren()) {
            final Path childPath = Paths.get(path.toString(), edge.getName());
            new LoadTreeEdgeCommand(edge, childPath, internalStateAccessor).perform();
            if (edge.isDirectory()) {
                final Tree childTree = internalStateAccessor.readTree(edge.getHash());
                loadFilesFromTree(childTree, childPath);
            }
        }
    }

    private void deleteFilesFromTree(@NotNull Tree tree, @NotNull Path path) throws MyGitStateException, IOException {
        for (TreeEdge edge : tree.getChildren()) {
            final Path edgeFile = Paths.get(path.toString(), edge.getName());
            if (!Files.exists(edgeFile)) {
                continue;
            }
            if (edge.isDirectory() && Files.isDirectory(edgeFile)) {
                deleteFilesFromTree(internalStateAccessor.readTree(edge.getHash()), edgeFile);
                if (Files.list(edgeFile).count() == 0) {
                    FileSystem.deleteFile(edgeFile);
                }
            }
            else {
                FileSystem.deleteFile(edgeFile);
            }
        }
    }
}
