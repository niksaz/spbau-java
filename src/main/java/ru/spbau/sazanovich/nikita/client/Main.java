package ru.spbau.sazanovich.nikita.client;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;

import static ru.spbau.sazanovich.nikita.server.ServerCommandLineApp.SERVER_PORT;

/**
 * Class which interacts with user and make requests to the server.
 */
public class Main {
    public static void main(String[] args) throws IOException {
        try (Scanner scanner = new Scanner(System.in)
        ) {
            while (true) {
                int numberToSend = scanner.nextInt();
                if (numberToSend != 1 && numberToSend != 2) {
                    break;
                }
                String path = scanner.nextLine().trim();
                System.out.println(numberToSend);
                System.out.println(path);

                SocketChannel channel = SocketChannel.open();
                channel.connect(new InetSocketAddress(SERVER_PORT));

                ByteBuffer buffer;
                try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                     DataOutputStream outputStream = new DataOutputStream(byteArrayOutputStream)
                ) {
                    outputStream.writeInt(numberToSend);
                    outputStream.writeUTF(path);
                    outputStream.flush();
                    byte[] bytes = byteArrayOutputStream.toByteArray();
                    buffer = ByteBuffer.allocate(bytes.length);
                    buffer.put(bytes);
                    buffer.flip();
                }

                while (buffer.hasRemaining()) {
                    channel.write(buffer);
                }

                channel.shutdownOutput();

                buffer.flip();
                buffer.clear();

                byte[] message = new byte[1024];
                int pos = 0;

                while (true) {
                    int bytesRead = channel.read(buffer);
                    if (bytesRead == -1) {
                        break;
                    }
                    buffer.flip();
                    while (bytesRead + pos > message.length) {
                        byte[] newMes = new byte[message.length * 2];
                        System.arraycopy(message, 0, newMes, 0, pos);
                        message = newMes;
                    }
                    buffer.get(message, pos, bytesRead);
                    buffer.flip();
                    pos += bytesRead;
                }


                if (numberToSend == 1) {
                    try (ByteArrayInputStream byteStream = new ByteArrayInputStream(message);
                         DataInputStream dataStream = new DataInputStream(byteStream)
                    ) {
                        int size = dataStream.readInt();
                        System.out.println(size);
                        while (size > 0) {
                            size--;
                            String fileName = dataStream.readUTF();
                            System.out.println(fileName);
                        }
                    }
                } else {
                    final Path currentDirectory = Paths.get(System.getProperty("user.dir"));
                    final Path p = Paths.get(path);
                    final Path toWrite = currentDirectory.resolve(p.getFileName().toString());
                    Files.write(toWrite, Arrays.copyOf(message, pos));
                }

                channel.close();
            }
        }
    }
}
