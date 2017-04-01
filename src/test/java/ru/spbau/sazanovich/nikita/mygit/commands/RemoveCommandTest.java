package ru.spbau.sazanovich.nikita.mygit.commands;

import org.junit.Test;
import ru.spbau.sazanovich.nikita.mygit.utils.SHA1Hasher;
import ru.spbau.sazanovich.nikita.testing.HandlerInitializedTest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class RemoveCommandTest extends HandlerInitializedTest {

    @Test
    public void testFactualRemoval() throws Exception {
        final Path directory = Paths.get(folderPath.toString(), "formulas");
        Files.createDirectory(directory);
        final Path file = Paths.get(directory.toString(), "trigonometry.txt");
        Files.createFile(file);
        handler.removePath(directory.toString());
        assertFalse(Files.exists(directory));
        assertFalse(Files.exists(file));
    }

    @Test
    public void testRepositoryRemoval() throws Exception {
        final InternalStateAccessor accessor = new InternalStateAccessor(folderPath, new SHA1Hasher());
        final Path file = Paths.get(folderPath.toString(), "formulas");
        Files.createFile(file);
        handler.stagePath(file.toString());
        handler.commitWithMessage("1");
        assertNotNull(accessor.findElementInHeadTree(accessor.relativizeWithMyGitDirectory(file)));
        handler.removePath(file.toString());
        handler.commitWithMessage("2");
        assertNull(accessor.findElementInHeadTree(accessor.relativizeWithMyGitDirectory(file)));
    }
}