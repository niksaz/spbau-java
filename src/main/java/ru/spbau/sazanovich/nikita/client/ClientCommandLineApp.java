package ru.spbau.sazanovich.nikita.client;

import org.jetbrains.annotations.NotNull;
import ru.spbau.sazanovich.nikita.server.ServerCommandLineApp;

import java.util.Scanner;

/**
 * Class which interacts with user and make requests to the server through terminal.
 */
public class ClientCommandLineApp {

    /**
     * Runs a client command line app.
     *
     * @param args command line arguments
     */
    public static void main(@NotNull String[] args) {
        ClientTokenHandler handler = new ClientTokenHandler(
                () -> new Client(ServerCommandLineApp.SERVER_PORT),
                System.out);
        handler.showHelp();
        try (Scanner scanner = new Scanner(System.in)
        ) {
            handler.handleTokensFrom(scanner);
        }
    }
}
