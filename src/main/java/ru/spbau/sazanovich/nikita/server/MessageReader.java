package ru.spbau.sazanovich.nikita.server;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

/**
 * Class which reads partial chunks of data and puts it together.
 */
class MessageReader {

    private static final int MEM_SIZE = 1024;

    private final byte[] array;
    private int pos;

    MessageReader() {
        this.array = new byte[MEM_SIZE];
        this.pos = 0;
    }

    boolean read(@NotNull SelectionKey selectionKey) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(100);
        SocketChannel channel = (SocketChannel) selectionKey.channel();
        int bytesRead = channel.read(buffer);
        if (bytesRead == -1) {
            return true;
        }
        int bytesToCopy = Math.min(MEM_SIZE - pos, bytesRead);
        buffer.flip();
        buffer.get(array, pos, bytesToCopy);
        pos += bytesToCopy;
        return false;
    }

    byte[] getMessage() {
        return Arrays.copyOf(array, pos);
    }
}
