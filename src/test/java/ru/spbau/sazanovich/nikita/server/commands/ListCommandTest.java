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
        List<FileInfo> infoList = command.list();
        assertEquals(3, infoList.size());
        assertTrue(infoList.contains(new FileInfo(file1)));
        assertTrue(infoList.contains(new FileInfo(file2)));
        assertTrue(infoList.contains(new FileInfo(dir)));
    }

    @Test(expected = UnsuccessfulCommandExecutionException.class)
    public void listNonexistentPath() throws Exception {
        ListCommand command = new ListCommand(Paths.get("nonexistent", "directory").toString());
        command.list();
    }

    @Test(expected = UnsuccessfulCommandExecutionException.class)
    public void listInvalidPath() throws Exception {
        ListCommand command = new ListCommand("C:/Program files\0");
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
        List<FileInfo> infoList = ListCommand.fromBytes(content);

        assertEquals(3, infoList.size());
        assertTrue(infoList.contains(new FileInfo(file1)));
        assertTrue(infoList.contains(new FileInfo(file2)));
        assertTrue(infoList.contains(new FileInfo(dir)));
    }
}