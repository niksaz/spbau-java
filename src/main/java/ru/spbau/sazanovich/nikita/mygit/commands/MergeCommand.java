package ru.spbau.sazanovich.nikita.mygit.commands;

import org.jetbrains.annotations.NotNull;
import ru.spbau.sazanovich.nikita.mygit.MyGitIllegalArgumentException;
import ru.spbau.sazanovich.nikita.mygit.MyGitMissingPrerequisitesException;
import ru.spbau.sazanovich.nikita.mygit.MyGitStateException;
import ru.spbau.sazanovich.nikita.mygit.objects.Branch;
import ru.spbau.sazanovich.nikita.mygit.objects.Commit;
import ru.spbau.sazanovich.nikita.mygit.objects.HeadStatus;
import ru.spbau.sazanovich.nikita.mygit.objects.Tree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Command class which performs a merge operation of HEAD and given branch.
 *
 * Chooses files based on their last modification's date -- older have a priority. Index should be empty before
 * merging and HEAD should not be in detached state.
 */
class MergeCommand extends Command {

    @NotNull
    private String branch;

    MergeCommand(@NotNull String branch, @NotNull InternalStateAccessor internalStateAccessor) {
        super(internalStateAccessor);
        this.branch = branch;
    }

    void perform()
            throws MyGitStateException, IOException, MyGitMissingPrerequisitesException, MyGitIllegalArgumentException {
        final HeadStatus headStatus = internalStateAccessor.getHeadStatus();
        if (headStatus.getName().equals(Commit.TYPE)) {
            throw new MyGitMissingPrerequisitesException("could not merge while you are in detached HEAD state");
        }
        final List<Branch> branchList = new ListBranchesCommand(internalStateAccessor).perform();
        if (!branchList.contains(new Branch(branch))) {
            throw new MyGitIllegalArgumentException("there is no such branch -- " + branch);
        }
        if (headStatus.getName().equals(branch)) {
            throw new MyGitIllegalArgumentException("can not merge branch with itself");
        }
        if (!internalStateAccessor.readIndexPaths().isEmpty()) {
            throw new MyGitMissingPrerequisitesException("staging area should be empty before a merge operation");
        }
        final String baseBranch = headStatus.getName();
        final Tree baseTree = internalStateAccessor.getBranchTree(baseBranch);
        final Tree otherTree = internalStateAccessor.getBranchTree(branch);

        final String mergeTreeHash = mergeTwoTrees(baseTree, otherTree);
        final List<String> parentsHashes = new ArrayList<>();
        parentsHashes.add(internalStateAccessor.getBranchCommitHash(baseBranch));
        parentsHashes.add(internalStateAccessor.getBranchCommitHash(branch));
        final Commit mergeCommit = new Commit(mergeTreeHash, "merge commit", parentsHashes);
        final String mergeCommitHash = internalStateAccessor.map(mergeCommit);
        internalStateAccessor.writeBranch(baseBranch, mergeCommitHash);

        new CheckoutCommand(baseBranch, internalStateAccessor).perform();
    }

    @NotNull
    private String mergeTwoTrees(@NotNull Tree baseTree, @NotNull Tree otherTree)
            throws MyGitStateException, IOException {
        final Tree mergedTree = new Tree();
        final ListIterator<Tree.TreeEdge> baseIterator = baseTree.getChildren().listIterator();
        final ListIterator<Tree.TreeEdge> otherIterator = otherTree.getChildren().listIterator();
        while (baseIterator.hasNext() && otherIterator.hasNext()) {
            final Tree.TreeEdge baseTreeEdge = baseIterator.next();
            final Tree.TreeEdge otherTreeEdge = otherIterator.next();
            int comparison = baseTreeEdge.getName().compareTo(otherTreeEdge.getName());
            if (comparison == 0) {
                if (baseTreeEdge.isDirectory() && otherTreeEdge.isDirectory()) {
                    final Tree baseChildTree = internalStateAccessor.readTree(baseTreeEdge.getName());
                    final Tree otherChildTree = internalStateAccessor.readTree(otherTreeEdge.getName());
                    final String mergedTreeHash = mergeTwoTrees(baseChildTree, otherChildTree);
                    final Tree.TreeEdge mergedTreeEdge =
                            new Tree.TreeEdge(mergedTreeHash, baseTreeEdge.getName(), baseTreeEdge.getType());
                    mergedTree.addChild(mergedTreeEdge);
                } else {
                    mergedTree.addChild(
                            baseTreeEdge.getDateCreated().compareTo(otherTreeEdge.getDateCreated()) > 0
                                    ? baseTreeEdge
                                    : otherTreeEdge
                    );
                }
            } else if (comparison < 0) {
                mergedTree.addChild(baseTreeEdge);
                otherIterator.previous();
            } else {
                mergedTree.addChild(otherTreeEdge);
                baseIterator.previous();
            }
        }
        while (baseIterator.hasNext()) {
            mergedTree.addChild(baseIterator.next());
        }
        while (otherIterator.hasNext()) {
            mergedTree.addChild(otherIterator.next());
        }
        return internalStateAccessor.map(mergedTree);
    }
}
