package ru.spbau.sazanovich.nikita.server;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a server which accepts client's requests.
 */
class Server {

    private final int port;

    Server(int port) {
        this.port = port;
    }

    void start() throws IOException {
        final ServerSocket serverSocket = new ServerSocket(port);

        while (true) {
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
