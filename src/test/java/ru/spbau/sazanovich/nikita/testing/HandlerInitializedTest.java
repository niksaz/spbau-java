package ru.spbau.sazanovich.nikita.testing;

import ru.spbau.sazanovich.nikita.mygit.commands.MyGitCommandHandler;

/**
 * Base class for tests which require folder and handler to be initialized.
 */
public abstract class HandlerInitializedTest extends FolderInitializedTest {

    protected MyGitCommandHandler handler;

    @Override
    public void initialize() throws Exception {
        super.initialize();
        MyGitCommandHandler.init(folderPath);
        handler = new MyGitCommandHandler(folderPath);
    }
}
