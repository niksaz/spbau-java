package ru.spbau.sazanovich.nikita.server.commands;

import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.*;

public class ListCommandTest {

    @Test
    public void list() throws Exception {
        TemporaryFolder folder = new TemporaryFolder();
        folder.create();
        Path file1 = folder.newFile("file1").toPath();
        Path file2 = folder.newFile("file2").toPath();
        Path dir = folder.newFolder("dir").toPath();

        ListCommand command = new ListCommand(folder.getRoot().getAbsolutePath());
        List<Path> list = command.list();
        assertEquals(3, list.size());
        assertTrue(list.contains(file1.toAbsolutePath()));
        assertTrue(list.contains(file2.toAbsolutePath()));
        assertTrue(list.contains(dir.toAbsolutePath()));
    }

    @Test(expected = UnsuccessfulCommandExecutionException.class)
    public void listNonexistentPath() throws Exception {
        ListCommand command = new ListCommand(Paths.get("nonexistent", "directory").toString());
        command.list();
    }

    @Test(expected = UnsuccessfulCommandExecutionException.class)
    public void listInvalidPath() throws Exception {
        ListCommand command = new ListCommand("C::/Program files/");
        command.list();
    }

    @Test(expected = UnsuccessfulCommandExecutionException.class)
    public void listFile() throws Exception {
        TemporaryFolder folder = new TemporaryFolder();
        folder.create();
        Path file = folder.newFile("file").toPath();

        ListCommand command = new ListCommand(file.toAbsolutePath().toString());
        command.list();
    }

    @Test
    public void convert() throws Exception {
        TemporaryFolder folder = new TemporaryFolder();
        folder.create();
        Path file1 = folder.newFile("file1").toPath();
        Path file2 = folder.newFile("file2").toPath();
        Path dir = folder.newFolder("dir").toPath();

        ListCommand command = new ListCommand(folder.getRoot().getAbsolutePath());
        byte[] content = command.execute();
        List<String> list = ListCommand.fromBytes(content);

        assertEquals(3, list.size());
        assertTrue(list.contains(file1.getFileName().toString()));
        assertTrue(list.contains(file2.getFileName().toString()));
        assertTrue(list.contains(dir.getFileName().toString()));
    }
}