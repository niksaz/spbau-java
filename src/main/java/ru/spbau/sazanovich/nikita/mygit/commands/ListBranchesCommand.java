package ru.spbau.sazanovich.nikita.mygit.commands;

import org.jetbrains.annotations.NotNull;
import ru.spbau.sazanovich.nikita.mygit.MyGitIOException;
import ru.spbau.sazanovich.nikita.mygit.MyGitStateException;
import ru.spbau.sazanovich.nikita.mygit.objects.Branch;

import java.util.List;

/**
 * Command class which gets list of branches in MyGit repository.
 */
class ListBranchesCommand extends Command {

    ListBranchesCommand(@NotNull InternalStateAccessor internalStateAccessor) {
        super(internalStateAccessor);
    }

    @NotNull
    List<Branch> perform() throws MyGitStateException, MyGitIOException {
        internalStateAccessor.getLogger().trace("ListBranchesCommand -- started");
        final List<Branch> branches = internalStateAccessor.listBranches();
        internalStateAccessor.getLogger().trace("ListBranchesCommand -- completed");
        return branches;
    }
}
