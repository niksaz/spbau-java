package ru.spbau.sazanovich.nikita.server;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Server starter class.
 */
public class Main {

    public static final int SERVER_PORT = 40000;

    public static void main(@NotNull String[] args) {
        final Server server = new Server(SERVER_PORT);
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
