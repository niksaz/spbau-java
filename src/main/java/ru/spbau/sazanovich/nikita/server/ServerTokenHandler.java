package ru.spbau.sazanovich.nikita.server;

import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;
import java.util.Iterator;

/**
 * Class which handles server commands which are coming as strings from {@link Iterator}
 * and prints the result to {@link PrintStream}.
 */
class ServerTokenHandler {

    private static final String START_CMD = "start";
    private static final String STOP_CMD = "stop";
    private static final String EXIT_CMD = "exit";

    @NotNull
    private final ServerFactory serverFactory;

    @NotNull
    private final PrintStream printStream;

    ServerTokenHandler(@NotNull ServerFactory serverFactory, @NotNull PrintStream printStream) {
        this.serverFactory = serverFactory;
        this.printStream = printStream;
    }

    void handleTokensFrom(@NotNull Iterator<String> iterator) {
        Server server = null;
        while (true) {
            String token = iterator.next();
            switch (token) {
                case START_CMD:
                    if (server == null) {
                        server = serverFactory.createServer();
                        server.start();
                        printStream.println("Server is running on port: " + server.getPort());
                    } else {
                        printStream.println("Server is already running.");
                    }
                    break;
                case STOP_CMD:
                    if (server == null) {
                        printStream.println("Server is not currently running.");
                    } else {
                        server.stop();
                        server = null;
                        printStream.println("Server will stop soon.");
                    }
                    break;
                case EXIT_CMD:
                    if (server != null) {
                        server.stop();
                    }
                    return;
                default:
                    printStream.println("Unknown command.");
                    break;
            }
        }
    }

    void showHelp() {
        printStream.println(
                START_CMD + "       to start the server\n" +
                STOP_CMD + "        to stop the server\n" +
                EXIT_CMD + "        to exit\n");
    }
}
