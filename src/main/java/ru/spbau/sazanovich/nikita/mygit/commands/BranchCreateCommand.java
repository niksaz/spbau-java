package ru.spbau.sazanovich.nikita.mygit.commands;

import org.jetbrains.annotations.NotNull;
import ru.spbau.sazanovich.nikita.mygit.MyGitIllegalArgumentException;
import ru.spbau.sazanovich.nikita.mygit.MyGitStateException;

import java.io.IOException;

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

    void perform() throws MyGitStateException, IOException, MyGitIllegalArgumentException {
        boolean branchExists = new BranchExistsCommand(branchName, internalStateAccessor).perform();
        if (branchExists) {
            throw new MyGitIllegalArgumentException("'" + branchName + "' branch already exists");
        }
        internalStateAccessor.writeBranch(branchName, internalStateAccessor.getHeadCommitHash());
    }
}
