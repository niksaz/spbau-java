package ru.spbau.sazanovich.nikita.mygit.commands;

import org.junit.Test;
import ru.spbau.sazanovich.nikita.testing.FolderInitializedTest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertArrayEquals;

public class ResetCommandTest extends FolderInitializedTest {

    @Test
    public void perform() throws Exception {
        MyGitCommandHandler.init(folderPath);
        final MyGitCommandHandler handler = new MyGitCommandHandler(folderPath);
        final Path path = Paths.get(folderPath.toString(), "input");
        Files.write(path, "1".getBytes());
        handler.stagePath(path.toString());
        handler.commitWithMessage("1");
        Files.write(path, "2".getBytes());
        handler.resetPath(path.toString());
        assertArrayEquals("1".getBytes(), Files.readAllBytes(path));
    }
}