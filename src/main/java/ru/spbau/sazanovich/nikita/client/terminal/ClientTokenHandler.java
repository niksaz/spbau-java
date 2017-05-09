package ru.spbau.sazanovich.nikita.client.terminal;

import org.jetbrains.annotations.NotNull;
import ru.spbau.sazanovich.nikita.client.Client;
import ru.spbau.sazanovich.nikita.client.ClientFactory;
import ru.spbau.sazanovich.nikita.server.commands.FileInfo;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ConnectException;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.util.Iterator;
import java.util.List;

/**
 * Class which handles client commands which are coming as strings from {@link Iterator}
 * and prints the result to {@link PrintStream}.
 */
class ClientTokenHandler {

    private static final String LIST_CMD = "list";
    private static final String GET_CMD = "get";
    private static final String EXIT_CMD = "exit";

    @NotNull
    private final ClientFactory clientFactory;

    @NotNull
    private final PrintStream printStream;

    ClientTokenHandler(@NotNull ClientFactory clientFactory, @NotNull PrintStream printStream) {
        this.clientFactory = clientFactory;
        this.printStream = printStream;
    }

    void handleTokensFrom(@NotNull Iterator<String> iterator) {
        Client client = clientFactory.createClient();
        while (true) {
            String token = iterator.next();
            try {
                switch (token) {
                    case LIST_CMD: {
                        String path = iterator.next();
                        List<FileInfo> files = client.list(path);
                        if (files == null) {
                            printStream.println("Unsuccessful command.");
                        } else {
                            for (FileInfo fileInfo : files) {
                                printStream.println(fileInfo);
                            }
                        }
                        break;
                    }
                    case GET_CMD: {
                        String fromPath = iterator.next();
                        String toPath = iterator.next();
                        boolean got = client.get(fromPath, toPath);
                        if (!got) {
                            printStream.println("Unsuccessful command.");
                        } else {
                            printStream.println("File is written to " + toPath);
                        }
                        break;
                    }
                    case EXIT_CMD:
                        return;
                    default:
                        printStream.println("Unknown command.");
                        break;
                }
            } catch (InvalidPathException | NoSuchFileException e) {
                printStream.println("Local path should be valid: " + e.getMessage());
            } catch (ConnectException e) {
                printStream.println("Could not connect to the server.");
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    void showHelp() {
        printStream.println(
                LIST_CMD + " <path>                to list directory from server\n" +
                GET_CMD + " <fromPath> <toPath>    to copy file from server\n" +
                EXIT_CMD + "                       to exit\n");
    }
}
