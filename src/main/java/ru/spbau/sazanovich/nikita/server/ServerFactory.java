package ru.spbau.sazanovich.nikita.server;

/**
 * Interface which allows to create {@link Server}.
 */
public interface ServerFactory {

    /**
     * Creates a server.
     *
     * @return a server
     */
    Server createServer();
}
