package ru.spbau.sazanovich.nikita.mygit.commands;

import org.jetbrains.annotations.NotNull;
import ru.spbau.sazanovich.nikita.mygit.MyGitIOException;
import ru.spbau.sazanovich.nikita.mygit.MyGitStateException;
import ru.spbau.sazanovich.nikita.mygit.objects.Branch;

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

    boolean perform() throws MyGitStateException, MyGitIOException {
        internalStateAccessor.getLogger().trace("BranchExistsCommand -- started with name=" + branchName);
        final List<Branch> branches = new ListBranchesCommand(internalStateAccessor).perform();
        final boolean exists = branches.contains(new Branch(branchName));
        internalStateAccessor.getLogger().trace("BranchExistsCommand -- completed");
        return exists;
    }
}
