package ru.spbau.sazanovich.nikita;

import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.spbau.sazanovich.nikita.client.Client;
import ru.spbau.sazanovich.nikita.server.Server;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WorkflowTest {

    private static final int TEST_PORT = 50000;

    @Test
    public void testListAndGet() throws Exception {
        Server server = new Server(TEST_PORT);
        server.start();
        sleep(500);

        TemporaryFolder folder = new TemporaryFolder();
        folder.create();
        Path file1 = folder.newFile("file1").toPath();
        Path file2 = folder.newFile("file2").toPath();
        Path dir = folder.newFolder("dir").toPath();
        byte[] data = new byte[10000];
        new Random().nextBytes(data);
        Files.write(file1, data);

        Client client = new Client(TEST_PORT);

        List<String> list = client.list(folder.getRoot().getAbsolutePath());
        assertTrue(list != null);
        assertEquals(3, list.size());
        assertTrue(list.contains(file1.getFileName().toString()));
        assertTrue(list.contains(file2.getFileName().toString()));
        assertTrue(list.contains(dir.getFileName().toString()));

        Path tmp = folder.newFile("tmp").toPath();
        boolean successful = client.get(file1.toAbsolutePath().toString(), tmp.toAbsolutePath().toString());
        assertTrue(successful);
        byte[] dataReceived = Files.readAllBytes(tmp);
        assertArrayEquals(data, dataReceived);

        server.stop();
    }
}