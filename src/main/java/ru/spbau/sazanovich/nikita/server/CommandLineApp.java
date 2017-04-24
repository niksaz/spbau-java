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

    private static final String START_CMD = "start";
    private static final String STOP_CMD = "stop";
    private static final String EXIT_CMD = "exit";

    /**
     * Runs a server command line app.
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
        Server server = null;
        while (true) {
            String nextCommand = scanner.next();
            switch (nextCommand) {
                case START_CMD:
                    if (server == null) {
                        server = new Server(SERVER_PORT);
                        server.start();
                        System.out.println("Server is running on port: " + SERVER_PORT);
                    } else {
                        System.out.println("Server is already running.");
                    }
                    break;
                case STOP_CMD:
                    if (server == null) {
                        System.out.println("Server is not currently running.");
                    } else {
                        server.stop();
                        server = null;
                        System.out.println("Server will stop soon.");
                    }
                    break;
                case EXIT_CMD:
                    if (server != null) {
                        server.stop();
                    }
                    return;
                default:
                    System.out.println("Unknown command.");
                    break;
            }
        }
    }

    private static void showHelp() {
        System.out.println(
                START_CMD + "       to start the server\n" +
                STOP_CMD + "        to stop the server\n" +
                EXIT_CMD + "        to exit\n");
    }
}
