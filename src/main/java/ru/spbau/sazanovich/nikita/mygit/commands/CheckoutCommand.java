package ru.spbau.sazanovich.nikita.mygit.commands;

import org.jetbrains.annotations.NotNull;
import ru.spbau.sazanovich.nikita.mygit.MyGitIllegalArgumentException;
import ru.spbau.sazanovich.nikita.mygit.MyGitMissingPrerequisitesException;
import ru.spbau.sazanovich.nikita.mygit.MyGitStateException;
import ru.spbau.sazanovich.nikita.mygit.objects.Branch;
import ru.spbau.sazanovich.nikita.mygit.objects.Commit;
import ru.spbau.sazanovich.nikita.mygit.objects.HeadStatus;

import java.io.IOException;
import java.util.List;

/**
 * Command class which checkouts a revision and moves HEAD there.
 *
 * Replaces all files which differs from the HEAD's ones if such exist. Index should be empty before checking out.
 */
class CheckoutCommand extends Command {

    @NotNull
    private String revisionName;

    CheckoutCommand(@NotNull String revisionName, @NotNull InternalStateAccessor internalStateAccessor) {
        super(internalStateAccessor);
        this.revisionName = revisionName;
    }

    void perform()
            throws MyGitStateException, MyGitMissingPrerequisitesException, MyGitIllegalArgumentException, IOException {
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
        internalStateAccessor.moveFromCommitToCommit(fromCommit, toCommit);
        final HeadStatus toHeadStatus = new HeadStatus(toHeadType, revisionName);
        internalStateAccessor.setHeadStatus(toHeadStatus);
    }
}
