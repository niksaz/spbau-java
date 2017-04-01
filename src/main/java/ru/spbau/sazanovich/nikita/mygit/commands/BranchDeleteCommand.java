package ru.spbau.sazanovich.nikita.mygit.commands;

import org.jetbrains.annotations.NotNull;
import ru.spbau.sazanovich.nikita.mygit.MyGitIllegalArgumentException;
import ru.spbau.sazanovich.nikita.mygit.MyGitStateException;
import ru.spbau.sazanovich.nikita.mygit.objects.Branch;
import ru.spbau.sazanovich.nikita.mygit.objects.HeadStatus;

import java.io.IOException;

/**
 * Command class which deletes the branch with the given name.
 */
class BranchDeleteCommand extends Command {

    @NotNull
    private final String branchName;

    BranchDeleteCommand(@NotNull String branchName, @NotNull InternalStateAccessor internalStateAccessor) {
        super(internalStateAccessor);
        this.branchName = branchName;
    }

    void perform() throws MyGitStateException, IOException, MyGitIllegalArgumentException {
        final HeadStatus headStatus = new HeadStatusCommand(internalStateAccessor).perform();
        if (headStatus.getType().equals(Branch.TYPE) && headStatus.getName().equals(branchName)) {
            throw new MyGitIllegalArgumentException(
                    "cannot delete branch '" + branchName +
                            "' checked out at " + internalStateAccessor.getMyGitDirectory());
        }
        boolean branchExists = new BranchExistsCommand(branchName, internalStateAccessor).perform();
        if (!branchExists) {
            throw new MyGitIllegalArgumentException("'" + branchName + "' branch is missing");
        }
        internalStateAccessor.deleteBranch(branchName);
    }
}
