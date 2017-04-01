package ru.spbau.sazanovich.nikita.mygit.commands;

import org.junit.Test;
import ru.spbau.sazanovich.nikita.mygit.objects.FileDifference;
import ru.spbau.sazanovich.nikita.testing.FolderInitializedTest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static ru.spbau.sazanovich.nikita.mygit.objects.FileDifferenceStageStatus.*;
import static ru.spbau.sazanovich.nikita.mygit.objects.FileDifferenceType.*;

public class MyGitCommandHandlerTest extends FolderInitializedTest {

    private MyGitCommandHandler handler;

    @Override
    public void initialize() throws Exception {
        super.initialize();
        MyGitCommandHandler.init(folderPath);
        handler = new MyGitCommandHandler(folderPath);
    }

    @Test
    public void getHeadChanges() throws Exception {
        final Path inputsPath = Paths.get(folderPath.toString(), "inputs");
        Files.createDirectory(inputsPath);
        handler.stagePath(inputsPath.toString());
        List<FileDifference> fileDifferences = handler.getHeadDifferences();
        assertEquals(1, fileDifferences.size());
        assertTrue(fileDifferences.get(0).getStageStatus().equals(TO_BE_COMMITTED));
        assertTrue(fileDifferences.get(0).getType().equals(ADDITION));
        handler.commitWithMessage("inputs added");

        final Path input1Path = Paths.get(inputsPath.toString(), "input1.txt");
        Files.createFile(input1Path);
        final Path input2Path = Paths.get(inputsPath.toString(), "input2.txt");
        Files.createFile(input2Path);
        fileDifferences = handler.getHeadDifferences();
        assertEquals(2, fileDifferences.size());
        assertTrue(fileDifferences.get(0).getStageStatus().equals(UNTRACKED));
        assertTrue(fileDifferences.get(1).getStageStatus().equals(UNTRACKED));
        handler.stagePath(input1Path.toString());
        handler.stagePath(input2Path.toString());
        handler.commitWithMessage("inputs 1 & 2");

        Files.delete(input1Path);
        fileDifferences = handler.getHeadDifferences();
        assertEquals(1, fileDifferences.size());
        assertTrue(fileDifferences.get(0).getStageStatus().equals(NOT_STAGED_FOR_COMMIT));
        assertTrue(fileDifferences.get(0).getType().equals(REMOVAL));
        handler.stagePath(input1Path.toString());
        handler.commitWithMessage("removed 1");

        Files.write(input2Path, new byte[10]);
        fileDifferences = handler.getHeadDifferences();
        assertEquals(1, fileDifferences.size());
        assertTrue(fileDifferences.get(0).getStageStatus().equals(NOT_STAGED_FOR_COMMIT));
        assertTrue(fileDifferences.get(0).getType().equals(MODIFICATION));
    }
}
