package ru.spbau.sazanovich.nikita.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

import static ru.spbau.sazanovich.nikita.server.Main.SERVER_PORT;

/**
 * Class which interacts with user and make requests to the server.
 */
public class Main {
    public static void main(String[] args) throws IOException {
        try (Scanner scanner = new Scanner(System.in)
        ) {
            while (true) {
                int numberToSend = scanner.nextInt();
                if (numberToSend == 0) {
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

                channel.close();
            }
        }
    }
}
