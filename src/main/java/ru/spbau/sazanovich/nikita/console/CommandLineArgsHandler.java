package ru.spbau.sazanovich.nikita.console;

import org.jetbrains.annotations.NotNull;
import ru.spbau.sazanovich.nikita.mygit.MyGitException;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Arrays;
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
    private static final String RM_CMD = "rm";
    private static final String CLEAN_CMD = "clean";
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
     * Parses command line arguments and delegates execution to {@link CommandExecutor} if parsed successfully.
     *
     * @param args command line arguments
     * @throws CommandNotSupportedException if the command does not exist in MyGit library
     * @throws MyGitException               if an exception occurred in MyGit system
     */
    void handle(@NotNull String[] args) throws CommandNotSupportedException, MyGitException {
        final CommandExecutor commandExecutor = createCommandExecutor();
        if (args.length == 0) {
            throw new CommandNotSupportedException("Add at least one argument.");
        }
        if (args[0].equals(HELP_CMD)) {
            printHelp();
            return;
        }
        if (args[0].equals(INIT_CMD)) {
            commandExecutor.performInit();
            return;
        }
        switch (args[0]) {
            case STAGE_CMD:
                if (args.length > 1) {
                    commandExecutor.performStage(omitFirstArg(args));
                    return;
                }
                throw new CommandNotSupportedException(STAGE_CMD + " requires some files to have an effect");
            case UNSTAGE_CMD:
                if (args.length > 1) {
                    commandExecutor.performUnstage(omitFirstArg(args));
                    return;
                }
                throw new CommandNotSupportedException(UNSTAGE_CMD + " requires some files to have an effect");
            case UNSTAGE_ALL_CMD:
                commandExecutor.performUnstageAll();
                return;
            case RESET_CMD:
                if (args.length > 1) {
                    commandExecutor.performReset(omitFirstArg(args));
                    return;
                }
                throw new CommandNotSupportedException(RESET_CMD + " requires some files to have an effect");
            case RM_CMD:
                if (args.length > 1) {
                    commandExecutor.performRemove(omitFirstArg(args));
                    return;
                }
                throw new CommandNotSupportedException(RM_CMD + " requires some files to have an effect");
            case CLEAN_CMD:
                commandExecutor.performClean();
                return;
            case LOG_CMD:
                commandExecutor.performLog();
                return;
            case STATUS_CMD:
                commandExecutor.performStatus();
                return;
            case BRANCH_CMD:
                if (args.length == 1) {
                    commandExecutor.performPrintBranches();
                    return;
                }
                if (args.length == 2) {
                    commandExecutor.performBranchCreate(args[1]);
                    return;
                }
                if (args.length == 3 && args[1].equals("-d")) {
                    commandExecutor.performBranchDelete(args[2]);
                    return;
                }
                throw new CommandNotSupportedException(BRANCH_CMD + " entered too many arguments");
            case CHECKOUT_CMD:
                if (args.length > 1) {
                    commandExecutor.performCheckout(args[1]);
                    return;
                }
                throw new CommandNotSupportedException(CHECKOUT_CMD + " requires a revision name");
            case COMMIT_CMD:
                if (args.length > 1) {
                    commandExecutor.performCommit(args[1]);
                    return;
                }
                throw new CommandNotSupportedException(COMMIT_CMD + " requires a message");
            case MERGE_CMD:
                if (args.length > 1) {
                    commandExecutor.performMerge(args[1]);
                    return;
                }
                throw new CommandNotSupportedException(MERGE_CMD + " requires another branch");
            default:
                throw new CommandNotSupportedException(String.join(" ", args));
        }
    }

    private void printHelp() {
        printStream.println(
                "usage: mygit <command> [<args>]\n" +
                        "\n" +
                        "start a working area:\n" +
                        "  " + INIT_CMD + "\n" +
                        "\n" +
                        "work on the current change:\n" +
                        "  " + STAGE_CMD + " <files>\n" +
                        "  " + UNSTAGE_CMD + " <files>\n" +
                        "  " + UNSTAGE_ALL_CMD + "\n" +
                        "  " + RESET_CMD + " <files>\n" +
                        "  " + RM_CMD + " <files>\n" +
                        "  " + CLEAN_CMD + "\n" +
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

    @NotNull
    CommandExecutor createCommandExecutor() {
        return new CommandExecutor(currentDirectory, printStream);
    }

    @NotNull
    private static List<String> omitFirstArg(@NotNull String[] args) {
        return Arrays.stream(args).skip(1).collect(Collectors.toList());
    }
}
