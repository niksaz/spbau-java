package ru.spbau.sazanovich.nikita.server.commands;

import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

import static org.junit.Assert.*;

public class GetCommandTest {

    @Test
    public void get() throws Exception {
        TemporaryFolder folder = new TemporaryFolder();
        folder.create();
        Path file = folder.newFile("file").toPath();
        byte[] data = new byte[10000];
        new Random().nextBytes(data);
        Files.write(file, data);

        GetCommand command = new GetCommand(file.toAbsolutePath().toString());
        byte[] content = command.get();

        assertArrayEquals(data, content);
    }

    @Test(expected = UnsuccessfulCommandExecutionException.class)
    public void getNonexistentPath() throws Exception {
        GetCommand command = new GetCommand(Paths.get("nonexistent", "directory").toString());
        command.get();
    }

    @Test(expected = UnsuccessfulCommandExecutionException.class)
    public void getInvalidPath() throws Exception {
        GetCommand command = new GetCommand("C::/Program files/");
        command.get();
    }

    @Test(expected = UnsuccessfulCommandExecutionException.class)
    public void getDirectory() throws Exception {
        TemporaryFolder folder = new TemporaryFolder();
        folder.create();
        Path dir = folder.newFolder("dir").toPath();

        GetCommand command = new GetCommand(dir.toAbsolutePath().toString());
        command.get();
    }
}