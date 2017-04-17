package ru.spbau.sazanovich.nikita.mygit.commands;

import org.jetbrains.annotations.NotNull;
import ru.spbau.sazanovich.nikita.mygit.MyGitIOException;
import ru.spbau.sazanovich.nikita.mygit.MyGitStateException;
import ru.spbau.sazanovich.nikita.mygit.objects.Commit;
import ru.spbau.sazanovich.nikita.mygit.objects.CommitLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

/**
 * Command class which gets the logs from commit's which are reachable from the HEAD's commit.
 */
class LogCommand extends Command {

    LogCommand(@NotNull InternalStateAccessor internalStateAccessor) {
        super(internalStateAccessor);
    }

    @NotNull
    List<CommitLog> perform() throws MyGitStateException, MyGitIOException {
        internalStateAccessor.getLogger().trace("LogCommand -- started");
        final Commit headCommit = internalStateAccessor.getHeadCommit();
        final TreeSet<Commit> commitTree = new TreeSet<>();
        traverseCommitsTree(headCommit, commitTree);
        final List<CommitLog> logsHistory = new ArrayList<>();
        for (Commit commit : commitTree) {
            final CommitLog log =
                    new CommitLog(internalStateAccessor.getObjectHash(commit), commit.getMessage(),
                            commit.getAuthor(), commit.getDateCreated());
            logsHistory.add(log);
        }
        Collections.reverse(logsHistory);
        internalStateAccessor.getLogger().trace("LogCommand -- completed");
        return logsHistory;
    }

    private void traverseCommitsTree(@NotNull Commit commit, @NotNull TreeSet<Commit> commitTree)
            throws MyGitStateException, MyGitIOException {
        if (!commitTree.contains(commit)) {
            commitTree.add(commit);
            for (String parentHash : commit.getParentsHashes()) {
                final Commit parentCommit = internalStateAccessor.readCommit(parentHash);
                traverseCommitsTree(parentCommit, commitTree);
            }
        }
    }
}
