package ru.spbau.sazanovich.nikita.client;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.ConnectException;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.util.List;
import java.util.Scanner;

/**
 * Class which interacts with user and make requests to the server.
 */
public class CommandLineApp {

    private static final String LIST_CMD = "list";
    private static final String GET_CMD = "get";
    private static final String EXIT_CMD = "exit";

    /**
     * Runs a client command line app.
     *
     * @param args command line arguments
     */
    public static void main(@NotNull String[] args) {
        showHelp();
        try (Scanner scanner = new Scanner(System.in)
        ) {
            commandLineCycle(scanner);
        }
    }

    private static void commandLineCycle(@NotNull Scanner scanner) {
        Client client = new Client(ru.spbau.sazanovich.nikita.server.CommandLineApp.SERVER_PORT);
        while (true) {
            String nextCommand = scanner.next();
            try {
                switch (nextCommand) {
                    case LIST_CMD: {
                        String path = scanner.next();
                        List<String> files = client.list(path);
                        if (files == null) {
                            System.out.println("Unsuccessful command.");
                        } else {
                            for (String fileName : files) {
                                System.out.println(fileName);
                            }
                        }
                        break;
                    }
                    case GET_CMD: {
                        String fromPath = scanner.next();
                        String toPath = scanner.next();
                        boolean got = client.get(fromPath, toPath);
                        if (!got) {
                            System.out.println("Unsuccessful command.");
                        } else {
                            System.out.println("File is written to " + toPath);
                        }
                        break;
                    }
                    case EXIT_CMD:
                        return;
                    default:
                        System.out.println("Unknown command.");
                        break;
                }
            } catch (InvalidPathException | NoSuchFileException e) {
                System.out.println("Local path should be valid: " + e.getMessage());
            } catch (ConnectException e) {
                System.out.println("Could not connect to the server.");
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void showHelp() {
        System.out.println(
                LIST_CMD + " <path>                to list directory from server\n" +
                GET_CMD + " <fromPath> <toPath>    to copy file from server\n" +
                EXIT_CMD + "                       to exit\n");
    }
}
