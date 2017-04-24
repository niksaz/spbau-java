package ru.spbau.sazanovich.nikita.utils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.Arrays;

/**
 * Class which helps to read data in chunks in non-blocking mode and join it together.
 */
public class ChannelByteReader {

    private final static int BUFFER_SIZE = 256;

    @NotNull
    private final ByteBuffer buffer;
    @NotNull
    private byte[] data;
    private int position;

    /**
     * Constructs a reader with default buffer size = 256.
     */
    public ChannelByteReader() {
        this.buffer = ByteBuffer.allocate(BUFFER_SIZE);
        this.data = new byte[BUFFER_SIZE];
        this.position = 0;
    }

    /**
     * Should be called consequently on {@link ByteChannel} to get input data. Assumes that the channel is in
     * non-blocking mode.
     *
     * @param channel channel to read from
     * @return number of bytes read; {@code -1} if EOF is reached
     * @throws IOException if an I/O error occurs
     */
    public int read(@NotNull ByteChannel channel) throws IOException {
        int bytesRead = channel.read(buffer);
        if (bytesRead == -1) {
            return -1;
        }
        while (position + bytesRead > data.length) {
            byte[] newData = new byte[data.length * 2];
            System.arraycopy(data, 0, newData, 0, position);
            data = newData;
        }
        buffer.flip();
        buffer.get(data, position, bytesRead);
        position += bytesRead;
        buffer.clear();
        return bytesRead;
    }

    /**
     * Should be called after EOF was reached. Returns concatenated byte chunks that was read.
     *
     * @return concatenated byte array
     */
    @NotNull
    public byte[] getData() {
        return Arrays.copyOf(data, position);
    }
}
