package ru.spbau.sazanovich.nikita;

import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.spbau.sazanovich.nikita.client.Client;
import ru.spbau.sazanovich.nikita.server.Server;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WorkflowTest {

    private static final int TEST_PORT = 50000;

    @Test
    public void testList() throws Exception {
        Server server = new Server(TEST_PORT);
        server.start();
        sleep(500);

        TemporaryFolder folder = new TemporaryFolder();
        folder.create();
        Path file1 = folder.newFile("file1").toPath();
        Path file2 = folder.newFile("file2").toPath();
        Path dir = folder.newFolder("dir").toPath();
        String text = "A selectable channel for stream-oriented connecting sockets.";
        Files.write(file1, text.getBytes());

        Client client = new Client(TEST_PORT);
        List<String> list = client.list(folder.getRoot().getAbsolutePath());
        assertTrue(list != null);
        assertEquals(3, list.size());
        assertTrue(list.contains(file1.getFileName().toString()));
        assertTrue(list.contains(file2.getFileName().toString()));
        assertTrue(list.contains(dir.getFileName().toString()));

        server.stop();
    }
}