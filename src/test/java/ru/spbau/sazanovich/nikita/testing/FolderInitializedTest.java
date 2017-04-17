package ru.spbau.sazanovich.nikita.testing;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Path;

/**
 * Base class for tests which require folder to be initialized.
 */
public abstract class FolderInitializedTest {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    protected Path folderPath;

    @Before
    public void initialize() throws Exception {
        folderPath = folder.getRoot().toPath();
    }
}
