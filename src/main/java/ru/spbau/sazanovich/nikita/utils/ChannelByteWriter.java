package ru.spbau.sazanovich.nikita.utils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

/**
 * Class which helps to write data in non-blocking mode.
 */
public class ChannelByteWriter {

    @NotNull
    private final ByteBuffer buffer;

    /**
     * Creates a writer with given data.
     * @param data data to be written
     */
    public ChannelByteWriter(@NotNull byte[] data) {
        this.buffer = ByteBuffer.wrap(data);
    }

    /**
     * Writes data to the channel.
     *
     * @param channel channel to write to
     * @return number of bytes written; -1 if there was no bytes remaining
     * @throws IOException if an I/O error occurs
     */
    public int write(@NotNull ByteChannel channel) throws IOException {
        if (buffer.hasRemaining()) {
            return channel.write(buffer);
        } else {
            return -1;
        }
    }
}
