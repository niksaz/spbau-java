package ru.spbau.sazanovich.nikita.client;

/**
 * Interface which allows to create {@link Client}.
 */
interface ClientFactory {

    /**
     * Creates a client.
     *
     * @return a client
     */
    Client createClient();
}
