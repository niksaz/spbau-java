package ru.spbau.sazanovich.nikita.mygit.commands;

import org.jetbrains.annotations.NotNull;
import ru.spbau.sazanovich.nikita.mygit.MyGitStateException;
import ru.spbau.sazanovich.nikita.mygit.objects.Branch;

import java.io.IOException;
import java.util.List;

/**
 * Command class which checks whether a branch with given name exists.
 */
class BranchExistsCommand extends Command {

    @NotNull
    private final String branchName;

    BranchExistsCommand(@NotNull String branchName, @NotNull InternalStateAccessor internalStateAccessor) {
        super(internalStateAccessor);
        this.branchName = branchName;
    }

    boolean perform() throws MyGitStateException, IOException {
        internalStateAccessor.getLogger().trace("BranchExistsCommand -- started");
        final List<Branch> branches = new ListBranchesCommand(internalStateAccessor).perform();
        final boolean exists = branches.contains(new Branch(branchName));
        internalStateAccessor.getLogger().trace("BranchExistsCommand -- completed");
        return exists;
    }
}
