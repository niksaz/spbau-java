package ru.spbau.sazanovich.nikita.mygit.commands;

import org.jetbrains.annotations.NotNull;
import ru.spbau.sazanovich.nikita.mygit.MyGitIOException;
import ru.spbau.sazanovich.nikita.mygit.MyGitIllegalArgumentException;
import ru.spbau.sazanovich.nikita.mygit.MyGitStateException;
import ru.spbau.sazanovich.nikita.mygit.objects.Branch;
import ru.spbau.sazanovich.nikita.mygit.objects.HeadStatus;

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

    void perform() throws MyGitStateException, MyGitIOException, MyGitIllegalArgumentException {
        internalStateAccessor.getLogger().trace("BranchDeleteCommand -- started with name=" + branchName);
        final HeadStatus headStatus = getHeadStatus();
        if (headStatus.getType().equals(Branch.TYPE) && headStatus.getName().equals(branchName)) {
            throw new MyGitIllegalArgumentException(
                    "cannot delete branch '" + branchName +
                            "' checked out at " + internalStateAccessor.getMyGitDirectory());
        }
        boolean branchExists = doesBranchExists(branchName);
        if (!branchExists) {
            throw new MyGitIllegalArgumentException("'" + branchName + "' branch is missing");
        }
        internalStateAccessor.deleteBranch(branchName);
        internalStateAccessor.getLogger().trace("BranchDeleteCommand -- completed");
    }

    @NotNull
    HeadStatus getHeadStatus() throws MyGitStateException, MyGitIOException {
        return new HeadStatusCommand(internalStateAccessor).perform();
    }

    boolean doesBranchExists(@NotNull String branchName) throws MyGitStateException, MyGitIOException {
        return new BranchExistsCommand(branchName, internalStateAccessor).perform();
    }
}
