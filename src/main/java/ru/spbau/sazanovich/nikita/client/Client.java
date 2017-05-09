package ru.spbau.sazanovich.nikita.client;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbau.sazanovich.nikita.server.commands.Command;
import ru.spbau.sazanovich.nikita.server.commands.FileInfo;
import ru.spbau.sazanovich.nikita.server.commands.GetCommand;
import ru.spbau.sazanovich.nikita.server.commands.ListCommand;
import ru.spbau.sazanovich.nikita.utils.ChannelByteReader;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * Class which represents a client of {@link ru.spbau.sazanovich.nikita.server.Server}.
 */
public class Client {

    private static final long TIMEOUT = 2000;

    private final int port;

    /**
     * Constructs a client to work with server on a given port.
     *
     * @param port server's port
     */
    public Client(int port) {
        this.port = port;
    }

    /**
     * Sends a request to the server to list directory at the given path.
     * Returns null if the request was unsuccessful.
     *
     * @param path directory to list
     * @return info about files in a given directory
     * @throws IOException if an I/O error occurs
     */
    @Nullable
    public List<FileInfo> list(@NotNull String path) throws IOException {
        byte[] content = sendRequestWithCodeAndArg(ListCommand.CODE, path);
        if (Arrays.equals(content, Command.errorResponseBytes())) {
            return null;
        }
        return ListCommand.fromBytes(content);
    }

    /**
     * Sends a request to the server to get file's content from the path on the server to the given path.
     *
     * @param fromPath file on the server to get
     * @param toPath path to save file from the server
     * @return {@code true} if request was successful; {@code false} otherwise
     * @throws IOException if an I/O error occurs
     * @throws InvalidPathException if {@code toPath} is not a valid path
     */
    public boolean get(@NotNull String fromPath, @NotNull String toPath) throws IOException, InvalidPathException {
        Path toFile = Paths.get(toPath);
        byte[] content = sendRequestWithCodeAndArg(GetCommand.CODE, fromPath);
        if (Arrays.equals(content, Command.errorResponseBytes())) {
            return false;
        }
        Files.write(toFile, content);
        return true;
    }

    @NotNull
    private byte[] sendRequestWithCodeAndArg(int code, @NotNull String arg) throws IOException {
        SocketChannel channel = openChannel();
        byte[] bytes;
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
             DataOutputStream outputStream = new DataOutputStream(byteStream)
        ) {
            outputStream.writeInt(code);
            outputStream.writeUTF(arg);
            outputStream.flush();
            bytes = byteStream.toByteArray();
        }
        writeBytesTo(bytes, channel);
        byte[] content = readFullyFrom(channel);
        channel.close();
        return content;
    }

    @NotNull
    private SocketChannel openChannel() throws IOException {
        SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(false);
        try (Selector selector = Selector.open()
        ) {
            channel.register(selector, SelectionKey.OP_CONNECT);
            channel.connect(new InetSocketAddress(port));
            int selected = selector.select(TIMEOUT);
            if (selected == 0 || !channel.finishConnect()) {
                throw new SocketTimeoutException("Could not connect to server in " + TIMEOUT + "ms");
            }
            return channel;
        }
    }

    private void writeBytesTo(@NotNull byte[] bytes, @NotNull SocketChannel channel) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
        channel.shutdownOutput();
    }

    @NotNull
    private byte[] readFullyFrom(@NotNull SocketChannel channel) throws IOException {
        ChannelByteReader reader = new ChannelByteReader();
        int bytesRead = reader.read(channel);
        while (bytesRead != -1) {
            bytesRead = reader.read(channel);
        }
        return reader.getData();
    }
}
