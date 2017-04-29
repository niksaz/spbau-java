package ru.spbau.sazanovich.nikita.server;

import org.jetbrains.annotations.NotNull;
import ru.spbau.sazanovich.nikita.server.commands.Command;
import ru.spbau.sazanovich.nikita.server.commands.GetCommand;
import ru.spbau.sazanovich.nikita.server.commands.ListCommand;
import ru.spbau.sazanovich.nikita.server.commands.UnsuccessfulCommandExecutionException;
import ru.spbau.sazanovich.nikita.utils.ChannelByteReader;
import ru.spbau.sazanovich.nikita.utils.ChannelByteWriter;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * Represents a server which accepts client's requests via {@link ServerSocketChannel}.
 */
public class Server {

    private final int port;

    private volatile boolean stopped;

    @NotNull
    private final Queue<ProcessRequest> processorQueue;

    /**
     * Constructs a server on a given port.
     *
     * @param port port to start the server on
     */
    public Server(int port) {
        this.port = port;
        this.stopped = false;
        this.processorQueue = new LinkedList<>();
    }

    /**
     * Starts a server in a separate thread.
     */
    public void start() {
        final Runnable serverCycleTask = () -> {
            try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
                 Selector selector = Selector.open()
            ) {
                serverSocketChannel.configureBlocking(false);
                serverSocketChannel.bind(new InetSocketAddress(port));
                serverSocketChannel.configureBlocking(false);
                serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

                while (!stopped) {
                    int readyChannels;
                    readyChannels = selector.selectNow();
                    if (readyChannels == 0) {
                        continue;
                    }
                    processReadyKeys(selector);
                    processQueue(selector);
                }
            } catch (IOException e) {
                System.out.println("Server will stop unexpectedly:");
                e.printStackTrace();
            }
        };
        new Thread(serverCycleTask).start();
    }

    /**
     * Asks the server to stop. Server will stop an execution with next cycle.
     */
    public void stop() {
        stopped = true;
    }

    /**
     * Gets the port where server is running.
     *
     * @return port
     */
    int getPort() {
        return port;
    }

    private void processReadyKeys(@NotNull Selector selector) {
        final Set<SelectionKey> keySet = selector.selectedKeys();
        Iterator<SelectionKey> keyIterator = keySet.iterator();
        while (keyIterator.hasNext()) {
            final SelectionKey key = keyIterator.next();
            if (key.isAcceptable()) {
                acceptConnection(key, selector);
            } else if (key.isReadable()) {
                final ChannelByteReader reader = (ChannelByteReader) key.attachment();
                try {
                    int bytesRead = reader.read((ByteChannel) key.channel());
                    if (bytesRead == -1) {
                        byte[] data = reader.getData();
                        key.interestOps(0);
                        processorQueue.add(new ProcessRequest(key, data));
                    }
                } catch (IOException e) {
                    finishWorkWithChannelForKey(key);
                }

            } else if (key.isWritable()) {
                final ChannelByteWriter writer = (ChannelByteWriter) key.attachment();
                try {
                    int bytesWritten = writer.write((ByteChannel) key.channel());
                    if (bytesWritten == -1) {
                        finishWorkWithChannelForKey(key);
                    }
                } catch (IOException e) {
                    finishWorkWithChannelForKey(key);
                }
            }
            keyIterator.remove();
        }
    }

    private void processQueue(@NotNull Selector selector) {
        while (!processorQueue.isEmpty()) {
            ProcessRequest processRequest = processorQueue.poll();
            byte[] response;
            try (ByteArrayInputStream byteStream = new ByteArrayInputStream(processRequest.getContent());
                 DataInputStream inputStream = new DataInputStream(byteStream)
            ) {
                int code = inputStream.readInt();
                switch (code) {
                    case ListCommand.CODE: {
                        final String path = inputStream.readUTF();
                        response = new ListCommand(path).execute();
                        break;
                    }
                    case GetCommand.CODE: {
                        final String path = inputStream.readUTF();
                        response = new GetCommand(path).execute();
                        break;
                    }
                    default:
                        throw new UnsuccessfulCommandExecutionException();
                }
            } catch (IOException | UnsuccessfulCommandExecutionException e) {
                response = Command.errorResponseBytes();
            }

            try {
                SelectableChannel channel = processRequest.getKey().channel();
                channel.register(selector, SelectionKey.OP_WRITE, new ChannelByteWriter(response));
            } catch (ClosedChannelException e) {
                finishWorkWithChannelForKey(processRequest.getKey());
            }
        }
    }

    private void finishWorkWithChannelForKey(@NotNull SelectionKey key) {
        try {
            key.cancel();
            Channel channel = key.channel();
            channel.close();
        } catch (IOException e) {
            System.out.println("Exception while closing channel:");
            e.printStackTrace();
        }
    }

    private void acceptConnection(@NotNull SelectionKey key, @NotNull Selector selector) {
        Channel channel = key.channel();
        if (!(channel instanceof ServerSocketChannel)) {
            return;
        }
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel client;
        try {
            client = serverSocketChannel.accept();
            client.configureBlocking(false);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        try {
            client.register(selector, SelectionKey.OP_READ, new ChannelByteReader());
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        }
    }
}
