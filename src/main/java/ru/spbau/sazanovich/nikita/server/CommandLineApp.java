package ru.spbau.sazanovich.nikita.server;

import org.jetbrains.annotations.NotNull;

import java.util.Scanner;

/**
 * Command line access to server control.
 */
public class CommandLineApp {

    /**
     * Server's default port.
     */
    public static final int SERVER_PORT = 40000;

    /**
     * Runs a server command line app.
     *
     * @param args command line arguments
     */
    public static void main(@NotNull String[] args) {
        final ServerTokenHandler handler = new ServerTokenHandler(
                () -> new Server(SERVER_PORT),
                System.out);
        handler.showHelp();
        try (Scanner scanner = new Scanner(System.in)
        ) {
            handler.handleTokensFrom(scanner);
        }
    }
}
