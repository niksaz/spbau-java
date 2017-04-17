package ru.spbau.sazanovich.nikita.mygit.commands;

import org.junit.Test;
import ru.spbau.sazanovich.nikita.mygit.objects.FileDifference;
import ru.spbau.sazanovich.nikita.mygit.objects.FileDifferenceStageStatus;
import ru.spbau.sazanovich.nikita.testing.HandlerInitializedTest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class CleanCommandTest extends HandlerInitializedTest {

    @Test
    public void perform() throws Exception {
        final Path stagedFile = Paths.get(folderPath.toString(), "in1");
        Files.createFile(stagedFile);
        handler.stagePath(stagedFile.toString());
        final Path unstagedFile = Paths.get(folderPath.toString(), "in2");
        Files.createFile(unstagedFile);
        final Path directory = Paths.get(folderPath.toString(), "out");
        Files.createDirectory(directory);
        final Path fileInDirectory = Paths.get(directory.toString(), "out1");
        Files.createFile(fileInDirectory);
        handler.clean();

        final List<FileDifference> differences = handler.getHeadDifferences();
        assertEquals(1, differences.size());
        assertEquals(FileDifferenceStageStatus.TO_BE_COMMITTED, differences.get(0).getStageStatus());
        assertEquals("in1", differences.get(0).getPath().getFileName().toString());

        assertEquals(2, Files.list(folderPath).collect(Collectors.toList()).size());
    }

}