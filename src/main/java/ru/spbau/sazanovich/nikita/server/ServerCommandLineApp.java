package ru.spbau.sazanovich.nikita.server;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Scanner;

/**
 * Command line access to server control.
 */
public class ServerCommandLineApp {

    public static final int SERVER_PORT = 40000;

    private static final String START_CMD = "start";
    private static final String STOP_CMD = "stop";
    private static final String EXIT_CMD = "exit";

    /**
     * Runs server command line control.
     *
     * @param args command line arguments
     */
    public static void main(@NotNull String[] args) {
        try (Scanner scanner = new Scanner(System.in)
        ) {
            commandLineCycle(scanner);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void commandLineCycle(@NotNull Scanner scanner) throws IOException {
        Server server = null;
        while (true) {
            String nextCommand = scanner.nextLine().trim();
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
                        System.out.println("Server is not running currently.");
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
}
