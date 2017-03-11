package ru.spbau.sazanovich.nikita;

import org.jetbrains.annotations.NotNull;
import ru.spbau.sazanovich.nikita.mygit.MyGit;
import ru.spbau.sazanovich.nikita.mygit.MyGitHandler;
import ru.spbau.sazanovich.nikita.mygit.exceptions.MyGitStateException;
import ru.spbau.sazanovich.nikita.mygit.logs.CommitLog;
import ru.spbau.sazanovich.nikita.mygit.logs.Status;
import ru.spbau.sazanovich.nikita.mygit.objects.Branch;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class ConsoleApp {

    private static final String INIT_CMD = "init";
    private static final String ADD_CMD = "add";
    private static final String RESET_CMD = "reset";
    private static final String LOG_CMD = "log";
    private static final String STATUS_CMD = "status";
    private static final String BRANCH_CMD = "branch";
    private static final String CHECKOUT_CMD = "checkout";
    private static final String COMMIT_CMD = "commit";
    private static final String MERGE_CMD = "merge";

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Enter some arguments");
            showHelp();
            return;
        }
        try {
            if (args[0].equals(INIT_CMD)) {
                MyGit.init();
                System.out.println("Successfully initialized mygit repository.");
            } else {
                final MyGitHandler handler = new MyGitHandler();
                switch (args[0]) {
                    case ADD_CMD:
                        if (args.length == 1) {
                            System.out.println(ADD_CMD + " requires some files to have an effect");
                            return;
                        }
                        handler.addPathsToIndex(suffixArgsToList(args));
                        break;
                    case RESET_CMD:
                        if (args.length == 1) {
                            System.out.println(RESET_CMD + " requires some files to have an effect");
                            return;
                        }
                        handler.resetPaths(suffixArgsToList(args));
                        break;
                    case LOG_CMD:
                        printStatusInfo(handler);
                        System.out.println();
                        final List<CommitLog> logsHistory = handler.getLogsHistory();
                        for (CommitLog log : logsHistory) {
                            System.out.println("commit " + log.getRevisionHash() + "\n" +
                                               "Author: " + log.getAuthor() + "\n" +
                                               "Date:   " + log.getDateCreated() + "\n" +
                                               "\n" +
                                               "    " + log.getMessage() +
                                               "\n");
                        }
                        break;
                    case STATUS_CMD:
                        printStatusInfo(handler);
                        final List<Path> paths = handler.scanDirectory();
                        break;
                    default:
                        showHelp();
                        break;
                }
            }
        } catch (Exception e) {
            System.out.println("Unsuccessful operation: " + e.getMessage());
        }
    }

    private static void printStatusInfo(@NotNull MyGitHandler handler) throws MyGitStateException, IOException {
        final Status status = handler.getHeadStatus();
        if (status.getType().equals(Branch.TYPE)) {
            System.out.println("On branch " + status.getName());
        } else {
            System.out.println("HEAD detached at " + status.getName());
        }
    }

    @NotNull
    private static List<String> suffixArgsToList(@NotNull String[] args) {
        return Arrays.asList(args).subList(1, args.length);
    }

    private static void showHelp() {
        System.out.println(
                "usage: mygit <command> [<args>]\n" +
                "\n" +
                "start a working area:\n" +
                "  " + INIT_CMD + "\n" +
                "\n" +
                "work on the current change:\n" +
                "  " + ADD_CMD + " [<files>]\n" +
                "  " + RESET_CMD + " [<files>]\n" +
                "\n" +
                "examine the history and state:\n" +
                "  " + LOG_CMD + "\n" +
                "  " + STATUS_CMD + "\n" +
                "\n" +
                "grow and tweak your common history:\n" +
                "  " + BRANCH_CMD + " [<name> | -d <name>]\n" +
                "  " + CHECKOUT_CMD + " <branch> | <revision>\n" +
                "  " + COMMIT_CMD +  " <message>\n" +
                "  " + MERGE_CMD + " <branch>");
    }
}
