package ru.spbau.sazanovich.nikita.client;

/**
 * Interface which allows to create {@link Client}.
 */
public interface ClientFactory {

    /**
     * Creates a client.
     *
     * @return a client
     */
    Client createClient();
}
