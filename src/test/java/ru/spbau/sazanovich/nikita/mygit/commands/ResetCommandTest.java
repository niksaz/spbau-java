package ru.spbau.sazanovich.nikita.mygit.commands;

import org.junit.Test;
import ru.spbau.sazanovich.nikita.testing.HandlerInitializedTest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertArrayEquals;

public class ResetCommandTest extends HandlerInitializedTest {

    @Test
    public void perform() throws Exception {
        final Path path = Paths.get(folderPath.toString(), "input");
        Files.write(path, "1".getBytes());
        handler.stagePath(path.toString());
        handler.commitWithMessage("1");
        Files.write(path, "2".getBytes());
        handler.resetPath(path.toString());
        assertArrayEquals("1".getBytes(), Files.readAllBytes(path));
    }
}