package ru.spbau.sazanovich.nikita.testing;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Path;

/**
 * Represents common routines for tests -- creating a folder, initializing the path.
 */
public class MyGitInitialized {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    protected Path myGitPath;

    @Before
    public void initializeMyGit() throws Exception {
        myGitPath = folder.getRoot().toPath();
    }
}
