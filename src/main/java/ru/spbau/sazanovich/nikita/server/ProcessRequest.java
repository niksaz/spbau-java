package ru.spbau.sazanovich.nikita.server;

import org.jetbrains.annotations.NotNull;

import java.nio.channels.SelectionKey;

/**
 * Represents a request which is gotten from a client.
 */
class ProcessRequest {

    @NotNull
    private final SelectionKey key;

    @NotNull
    private final byte[] content;

    ProcessRequest(@NotNull SelectionKey key, @NotNull byte[] content) {
        this.key = key;
        this.content = content;
    }

    @NotNull
    SelectionKey getKey() {
        return key;
    }

    @NotNull
    byte[] getContent() {
        return content;
    }
}
