package ru.spbau.sazanovich.nikita.console;

import org.jetbrains.annotations.NotNull;
import ru.spbau.sazanovich.nikita.mygit.*;
import ru.spbau.sazanovich.nikita.mygit.commands.MyGitCommandHandler;
import ru.spbau.sazanovich.nikita.mygit.objects.*;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;

/**
 * Executes parsed commands and appends library's responses to a given {@link java.io.PrintStream}.
 */
class CommandExecutor {

    @NotNull
    private final Path currentDirectory;
    @NotNull
    private final PrintStream printStream;

    CommandExecutor(@NotNull Path currentDirectory, @NotNull PrintStream printStream) {
        this.currentDirectory = currentDirectory;
        this.printStream = printStream;
    }

    void performInit() throws
            MyGitIllegalArgumentException, MyGitAlreadyInitializedException, MyGitStateException, MyGitIOException {
        MyGitCommandHandler.init(currentDirectory);
        printStream.println("Successfully initialized mygit repository.");
    }

    void performStage(@NotNull List<String> paths)
            throws MyGitStateException, MyGitIllegalArgumentException, MyGitIOException {
        final MyGitCommandHandler handler = new MyGitCommandHandler(currentDirectory);
        for (String path : paths) {
            handler.stagePath(path);
        }
    }

    void performUnstage(@NotNull List<String> paths)
            throws MyGitStateException, MyGitIllegalArgumentException, MyGitIOException {
        final MyGitCommandHandler handler = new MyGitCommandHandler(currentDirectory);
        for (String path : paths) {
            handler.unstagePath(path);
        }
    }

    void performUnstageAll()
            throws MyGitStateException, MyGitIllegalArgumentException, MyGitIOException {
        final MyGitCommandHandler handler = new MyGitCommandHandler(currentDirectory);
        handler.unstageAllPaths();
    }

    void performReset(@NotNull List<String> paths)
            throws MyGitStateException, MyGitIllegalArgumentException, MyGitIOException {
        final MyGitCommandHandler handler = new MyGitCommandHandler(currentDirectory);
        for (String path : paths) {
            handler.resetPath(path);
        }
    }

    void performRemove(@NotNull List<String> paths)
            throws MyGitStateException, MyGitIllegalArgumentException, MyGitIOException {
        final MyGitCommandHandler handler = new MyGitCommandHandler(currentDirectory);
        for (String path : paths) {
            handler.removePath(path);
        }
    }

    void performClean() throws MyGitStateException, MyGitIllegalArgumentException, MyGitIOException {
        final MyGitCommandHandler handler = new MyGitCommandHandler(currentDirectory);
        handler.clean();
    }


    void performLog() throws MyGitStateException, MyGitIllegalArgumentException, MyGitIOException {
        final MyGitCommandHandler handler = new MyGitCommandHandler(currentDirectory);
        printStatusInfo(handler);
        printStream.println();
        final List<CommitLog> logsHistory = handler.getCommitsLogsHistory();
        for (CommitLog log : logsHistory) {
            printStream.println(
                    "commit " + log.getRevisionHash() + "\n" +
                            "Author: " + log.getAuthor() + "\n" +
                            "Date:   " + log.getDateCreated() + "\n" +
                            "\n" +
                            "    " + log.getMessage() +
                            "\n");
        }
    }

    void performPrintBranches() throws MyGitStateException, MyGitIOException, MyGitIllegalArgumentException {
        final MyGitCommandHandler handler = new MyGitCommandHandler(currentDirectory);
        final List<Branch> branches = handler.listBranches();
        final HeadStatus headStatus = handler.getHeadStatus();
        String currentBranchName;
        if (headStatus.getType().equals(Branch.TYPE)) {
            currentBranchName = headStatus.getName();
        } else {
            currentBranchName = null;
            printStream.println("* (HEAD detached at " + headStatus.getName() + ")");
        }
        for (Branch branch : branches) {
            printStream.println(
                    (branch.getName().equals(currentBranchName) ? "* " : "  ") +
                            branch.getName());
        }
    }

    void performBranchCreate(@NotNull String branchName)
            throws MyGitStateException, MyGitIllegalArgumentException, MyGitIOException {
        final MyGitCommandHandler handler = new MyGitCommandHandler(currentDirectory);
        handler.createBranch(branchName);
    }

    void performBranchDelete(@NotNull String branchName)
            throws MyGitStateException, MyGitIllegalArgumentException, MyGitIOException {
        final MyGitCommandHandler handler = new MyGitCommandHandler(currentDirectory);
        handler.deleteBranch(branchName);
    }

    void performCheckout(@NotNull String revisionName) throws
            MyGitStateException, MyGitIllegalArgumentException, MyGitIOException, MyGitMissingPrerequisitesException {
        final MyGitCommandHandler handler = new MyGitCommandHandler(currentDirectory);
        handler.checkout(revisionName);
    }

    void performCommit(@NotNull String message)
            throws MyGitStateException, MyGitIllegalArgumentException, MyGitIOException {
        final MyGitCommandHandler handler = new MyGitCommandHandler(currentDirectory);
        handler.commitWithMessage(message);
    }

    void performMerge(@NotNull String withBranchName) throws
            MyGitStateException, MyGitIllegalArgumentException, MyGitIOException, MyGitMissingPrerequisitesException {
        final MyGitCommandHandler handler = new MyGitCommandHandler(currentDirectory);
        handler.mergeHeadWithBranch(withBranchName);
    }

    void performStatus() throws MyGitStateException, MyGitIOException, MyGitIllegalArgumentException {
        final MyGitCommandHandler handler = new MyGitCommandHandler(currentDirectory);
        printStatusInfo(handler);

        final List<FileDifference> fileDifferences = handler.getHeadDifferences();
        final List<FileDifference> changesToBeCommitted =
                FileDifferenceStageStatus.filterBy(fileDifferences, FileDifferenceStageStatus.TO_BE_COMMITTED);
        if (changesToBeCommitted.size() != 0) {
            printStream.println("Changes to be committed:\n");
            printDifferencesWithoutStatus(changesToBeCommitted);
        }

        final List<FileDifference> changesNotStagedForCommit =
                FileDifferenceStageStatus.filterBy(fileDifferences, FileDifferenceStageStatus.NOT_STAGED_FOR_COMMIT);
        if (changesNotStagedForCommit.size() != 0) {
            printStream.println("Changes not staged for commit:\n");
            printDifferencesWithoutStatus(changesNotStagedForCommit);
        }

        final List<FileDifference> untrackedFiles =
                FileDifferenceStageStatus.filterBy(fileDifferences, FileDifferenceStageStatus.UNTRACKED);
        if (untrackedFiles.size() != 0) {
            printStream.println("Untracked files:\n");
            for (FileDifference change : untrackedFiles) {
                printStream.println(
                        "\t" +
                                change.getPath());
            }
            printStream.println();
        }
    }

    private void printDifferencesWithoutStatus(@NotNull List<FileDifference> changes) {
        for (FileDifference change : changes) {
            printStream.println(
                    "\t" +
                            mapFileChangeTypeToString(change.getType()) +
                            change.getPath());
        }
        printStream.println();
    }

    private void printStatusInfo(@NotNull MyGitCommandHandler handler) throws MyGitStateException, MyGitIOException {
        final HeadStatus headStatus = handler.getHeadStatus();
        if (headStatus.getType().equals(Branch.TYPE)) {
            printStream.println("On branch " + headStatus.getName());
        } else {
            printStream.println("HEAD detached at " + headStatus.getName());
        }
    }

    private static String mapFileChangeTypeToString(@NotNull FileDifferenceType fileDifferenceType) {
        switch (fileDifferenceType) {
            case ADDITION:
                return "new file:   ";
            case REMOVAL:
                return "deleted:   ";
            default:
                return "";
        }
    }
}
