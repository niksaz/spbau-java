package ru.spbau.sazanovich.nikita.console;

import org.jetbrains.annotations.NotNull;
import ru.spbau.sazanovich.nikita.mygit.MyGitException;
import ru.spbau.sazanovich.nikita.mygit.MyGitStateException;
import ru.spbau.sazanovich.nikita.mygit.commands.MyGitCommandHandler;
import ru.spbau.sazanovich.nikita.mygit.objects.*;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Parses and executes command line arguments.
 */
class CommandLineArgsHandler {

    private static final String INIT_CMD = "init";
    private static final String STAGE_CMD = "stage";
    private static final String UNSTAGE_CMD = "unstage";
    private static final String UNSTAGE_ALL_CMD = "unstage-all";
    private static final String RESET_CMD = "reset";
    private static final String LOG_CMD = "log";
    private static final String STATUS_CMD = "status";
    private static final String BRANCH_CMD = "branch";
    private static final String CHECKOUT_CMD = "checkout";
    private static final String COMMIT_CMD = "commit";
    private static final String MERGE_CMD = "merge";
    private static final String HELP_CMD = "help";

    @NotNull
    private final PrintStream printStream;
    @NotNull
    private final Path currentDirectory;

    /**
     * Constructs a handler that writes output to the given {@code printStream}
     * and assumes it is in {@code currentDirectory}.
     *
     * @param printStream      output stream
     * @param currentDirectory a path to a current directory
     */
    CommandLineArgsHandler(@NotNull PrintStream printStream, @NotNull Path currentDirectory) {
        this.printStream = printStream;
        this.currentDirectory = currentDirectory;
    }

    /**
     * Parses command line arguments and executes them if parsed successfully.
     *
     * @param args command line arguments
     * @throws CommandNotSupportedException if the command does not exist in MyGit library
     * @throws MyGitException               if an exception occurred in MyGit system
     * @throws IOException                  if an error occurs during working with a filesystem
     */
    void handle(@NotNull String[] args) throws CommandNotSupportedException, MyGitException, IOException {
        if (args.length == 0) {
            throw new CommandNotSupportedException("Enter some arguments");
        }
        if (args[0].equals(HELP_CMD)) {
            showHelp();
            return;
        }
        if (args[0].equals(INIT_CMD)) {
            MyGitCommandHandler.init(currentDirectory);
            printStream.println("Successfully initialized mygit repository.");
            return;
        }
        final MyGitCommandHandler handler = new MyGitCommandHandler(currentDirectory);
        switch (args[0]) {
            case STAGE_CMD:
                if (args.length > 1) {
                    for (int i = 1; i < args.length; i++) {
                        handler.stagePath(args[i]);
                    }
                    return;
                }
                throw new CommandNotSupportedException(STAGE_CMD + " requires some files to have an effect");
            case UNSTAGE_CMD:
                if (args.length > 1) {
                    for (int i = 1; i < args.length; i++) {
                        handler.unstagePath(args[i]);
                    }
                    return;
                }
                throw new CommandNotSupportedException(UNSTAGE_CMD + " requires some files to have an effect");
            case UNSTAGE_ALL_CMD:
                handler.unstageAllPaths();
                return;
            case RESET_CMD:
                if (args.length > 1) {
                    for (int i = 1; i < args.length; i++) {
                        handler.resetPath(args[i]);
                    }
                    return;
                }
                throw new CommandNotSupportedException(RESET_CMD + " requires some files to have an effect");
            case LOG_CMD:
                performLogCommand(handler);
                return;
            case STATUS_CMD:
                performStatusCommand(handler);
                return;
            case BRANCH_CMD:
                if (args.length == 1) {
                    printAllBranches(handler);
                    return;
                }
                if (args.length == 2) {
                    handler.createBranch(args[1]);
                    return;
                }
                if (args.length == 3 && args[1].equals("-d")) {
                    handler.deleteBranch(args[2]);
                    return;
                }
                throw new CommandNotSupportedException(BRANCH_CMD + " entered too many arguments");
            case CHECKOUT_CMD:
                if (args.length > 1) {
                    handler.checkout(args[1]);
                    return;
                }
                throw new CommandNotSupportedException(CHECKOUT_CMD + " requires a revision name");
            case COMMIT_CMD:
                if (args.length > 1) {
                    handler.commitWithMessage(args[1]);
                    return;
                }
                throw new CommandNotSupportedException(COMMIT_CMD + " requires a message");
            case MERGE_CMD:
                if (args.length > 1) {
                    handler.mergeHeadWithBranch(args[1]);
                    return;
                }
                throw new CommandNotSupportedException(MERGE_CMD + " requires another branch");
            default:
                throw new CommandNotSupportedException(String.join(" ", args));
        }
    }

    private void printAllBranches(@NotNull MyGitCommandHandler handler) throws MyGitStateException, IOException {
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

    private void performLogCommand(@NotNull MyGitCommandHandler handler) throws MyGitStateException, IOException {
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

    private void performStatusCommand(@NotNull MyGitCommandHandler handler) throws MyGitStateException, IOException {
        printStatusInfo(handler);

        final List<FileDifference> fileDifferences = handler.getHeadDifferences();
        final List<FileDifference> changesToBeCommitted =
                filterDifferencesByStatus(fileDifferences, FileDifferenceStageStatus.TO_BE_COMMITTED);
        if (changesToBeCommitted.size() != 0) {
            printStream.println("Changes to be committed:\n");
            printDifferencesWithoutStatus(changesToBeCommitted);
        }

        final List<FileDifference> changesNotStagedForCommit =
                filterDifferencesByStatus(fileDifferences, FileDifferenceStageStatus.NOT_STAGED_FOR_COMMIT);
        if (changesNotStagedForCommit.size() != 0) {
            printStream.println("Changes not staged for commit:\n");
            printDifferencesWithoutStatus(changesNotStagedForCommit);
        }

        final List<FileDifference> untrackedFiles =
                filterDifferencesByStatus(fileDifferences, FileDifferenceStageStatus.UNTRACKED);
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

    private void printStatusInfo(@NotNull MyGitCommandHandler handler) throws MyGitStateException, IOException {
        final HeadStatus headStatus = handler.getHeadStatus();
        if (headStatus.getType().equals(Branch.TYPE)) {
            printStream.println("On branch " + headStatus.getName());
        } else {
            printStream.println("HEAD detached at " + headStatus.getName());
        }
    }

    private void showHelp() {
        printStream.println(
                "usage: mygit <command> [<args>]\n" +
                        "\n" +
                        "start a working area:\n" +
                        "  " + INIT_CMD + "\n" +
                        "\n" +
                        "work on the current change:\n" +
                        "  " + STAGE_CMD + " [<files>]\n" +
                        "  " + UNSTAGE_CMD + " [<files>]\n" +
                        "  " + UNSTAGE_ALL_CMD + "\n" +
                        "  " + RESET_CMD + " [<files>]\n" +
                        "\n" +
                        "examine the history and state:\n" +
                        "  " + LOG_CMD + "\n" +
                        "  " + STATUS_CMD + "\n" +
                        "\n" +
                        "grow and tweak your common history:\n" +
                        "  " + BRANCH_CMD + " [<name> | -d <name>]\n" +
                        "  " + CHECKOUT_CMD + " <branch> | <revision>\n" +
                        "  " + COMMIT_CMD + " <message>\n" +
                        "  " + MERGE_CMD + " <branch>\n" +
                        "\n" +
                        "'mygit help' list all available commands.");
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

    @NotNull
    private static List<FileDifference> filterDifferencesByStatus(
            @NotNull List<FileDifference> list, @NotNull FileDifferenceStageStatus status) {
        return list
                .stream()
                .filter(diff -> diff.getStageStatus().equals(status))
                .collect(Collectors.toList());
    }
}
