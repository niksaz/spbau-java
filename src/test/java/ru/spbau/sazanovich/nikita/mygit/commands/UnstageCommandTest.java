package ru.spbau.sazanovich.nikita.mygit.commands;

import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UnstageCommandTest extends MockedInternalStateAccessorTest {

    @Test
    public void perform() throws Exception {
        final Path path1 = Paths.get("input.txt");
        final Path path2 = Paths.get("output.txt");
        when(accessor.convertStringToPathRelativeToMyGitDirectory("input.txt")).thenReturn(path1);
        final Set<Path> paths = new HashSet<>();
        paths.add(path1);
        paths.add(path2);
        when(accessor.readIndexPaths()).thenReturn(paths);
        final UnstageCommand command = new UnstageCommand("input.txt", accessor);
        command.perform();
        final Set<Path> correctPaths = new HashSet<>();
        correctPaths.add(path2);
        verify(accessor).writeIndexPaths(correctPaths);
    }
}