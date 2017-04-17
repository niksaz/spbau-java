package ru.spbau.sazanovich.nikita.mygit.commands;

import org.jetbrains.annotations.NotNull;
import ru.spbau.sazanovich.nikita.mygit.MyGitIOException;
import ru.spbau.sazanovich.nikita.mygit.MyGitIllegalArgumentException;
import ru.spbau.sazanovich.nikita.mygit.MyGitStateException;

/**
 * Command class which creates a new branch with the given name.
 */
class BranchCreateCommand extends Command {

    @NotNull
    private final String branchName;

    BranchCreateCommand(@NotNull String branchName, @NotNull InternalStateAccessor internalStateAccessor) {
        super(internalStateAccessor);
        this.branchName = branchName;
    }

    void perform() throws MyGitStateException, MyGitIOException, MyGitIllegalArgumentException {
        internalStateAccessor.getLogger().trace("BranchCreateCommand -- started with name=" + branchName);
        boolean branchExists = new BranchExistsCommand(branchName, internalStateAccessor).perform();
        if (branchExists) {
            throw new MyGitIllegalArgumentException("'" + branchName + "' branch already exists");
        }
        internalStateAccessor.writeBranch(branchName, internalStateAccessor.getHeadCommitHash());
        internalStateAccessor.getLogger().trace("BranchCreateCommand -- completed");
    }
}
