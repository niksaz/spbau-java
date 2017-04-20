package ru.spbau.sazanovich.nikita.server;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a server which accepts client's requests.
 */
class Server {

    private final int port;
    private volatile boolean stopped;

    Server(int port) {
        this.port = port;
        this.stopped = false;
    }

    void start() throws IOException {
        final ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(port));
        serverSocketChannel.configureBlocking(false);

        final Selector selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (!stopped) {
            int readyChannels = selector.selectNow();
            if (readyChannels == 0) {
                continue;
            }
            final Set<SelectionKey> keySet = selector.selectedKeys();
            final Iterator<SelectionKey> keyIterator = keySet.iterator();
            while (keyIterator.hasNext()) {
                final SelectionKey key = keyIterator.next();
                if (key.isAcceptable()) {
                    acceptConnection(key, selector);
                } else if (key.isReadable()) {
                    MessageReader reader = (MessageReader) key.attachment();
                    boolean finished = reader.read(key);
                    if (finished) {
                        final byte[] bytes = reader.getMessage();
                        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                             DataInputStream inputStream = new DataInputStream(byteArrayInputStream)
                        ) {
                            int i = inputStream.readInt();
                            String path = inputStream.readUTF();
                            System.out.println(i);
                            System.out.println(path);
                        }
                        SocketChannel channel = (SocketChannel) key.channel();
                        key.cancel();
                    }
                } else if (key.isWritable()) {
                    key.cancel();
                }
                keyIterator.remove();
            }

            /*
            final Socket socket = serverSocket.accept();
            System.out.println("GOT YOU!");
            try (DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                 DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream())
            ) {
                int code = inputStream.readInt();
                switch (code) {
                    case 1:
                        final String path = inputStream.readUTF();
                        final List<Path> files = list(path);
                        if (files == null) {
                            outputStream.writeInt(-1);
                        } else {
                            outputStream.writeInt(0);
                            for (Path file : files) {
                                outputStream.writeUTF(file.getFileName().toString());
                            }
                        }
                        break;
                    default:
                        outputStream.writeInt(-1);
                }
            }
            */
        }
        selector.close();
        serverSocketChannel.close();
    }

    void stop() {
        stopped = true;
    }

    private void acceptConnection(@NotNull SelectionKey selectionKey, @NotNull Selector selector) {
        Channel channel = selectionKey.channel();
        if (!(channel instanceof ServerSocketChannel)) {
            return;
        }
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
        SocketChannel client;
        try {
            client = serverSocketChannel.accept();
            client.configureBlocking(false);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        try {
            client.register(selector, SelectionKey.OP_READ, new MessageReader());
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    private List<Path> list(@NotNull String path) {
        final Path directory;
        try {
            directory = Paths.get(path);
        } catch (InvalidPathException e) {
            return null;
        }
        if (!Files.isDirectory(directory)) {
            return null;
        }
        try {
            return Files.list(directory).collect(Collectors.toList());
        } catch (IOException e) {
            return null;
        }
    }
}
