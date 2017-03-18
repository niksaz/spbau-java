package ru.spbau.sazanovich.nikita.mygit;

import org.jetbrains.annotations.NotNull;
import ru.spbau.sazanovich.nikita.mygit.exceptions.MyGitAlreadyInitializedException;
import ru.spbau.sazanovich.nikita.mygit.exceptions.MyGitIllegalArgumentException;
import ru.spbau.sazanovich.nikita.mygit.exceptions.MyGitStateException;
import ru.spbau.sazanovich.nikita.mygit.logs.HeadStatus;
import ru.spbau.sazanovich.nikita.mygit.objects.Branch;
import ru.spbau.sazanovich.nikita.mygit.objects.Commit;
import ru.spbau.sazanovich.nikita.mygit.objects.Tree;
import ru.spbau.sazanovich.nikita.mygit.utils.SHA1Hasher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Class with a static method which is used to initialize a repository.
 */
public class MyGit {

    /**
     * Initialize MyGit repository in a given directory.
     *
     * @param directory an absolute path to a directory to initialize MyGit in
     * @throws MyGitIllegalArgumentException if the directory path is not absolute
     * @throws MyGitAlreadyInitializedException if the directory already contains .mygit file
     * @throws MyGitStateException if an internal error occurs during operations
     * @throws IOException if an error occurs during working with a filesystem
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void init(@NotNull Path directory)
            throws MyGitIllegalArgumentException, MyGitAlreadyInitializedException, MyGitStateException, IOException {
        if (!directory.isAbsolute()) {
            throw new MyGitIllegalArgumentException("path parameter should be an absolute");
        }
        final Path myGitPath = Paths.get(directory.toString(), ".mygit");
        if (myGitPath.toFile().exists()) {
            throw new MyGitAlreadyInitializedException();
        } else {
            Files.createDirectory(myGitPath);
            final Mapper mapper = new Mapper(myGitPath.toAbsolutePath().getParent(), new SHA1Hasher());
            Files.createFile(Paths.get(myGitPath.toString(), "HEAD"));
            mapper.setHeadStatus(new HeadStatus(Branch.TYPE, "master"));
            Files.createFile(Paths.get(myGitPath.toString(), "index"));
            Files.createDirectory(Paths.get(myGitPath.toString(), "objects"));
            Files.createDirectory(Paths.get(myGitPath.toString(), "branches"));
            final String commitHash = createInitialCommit(mapper);
            mapper.writeBranch("master", commitHash);
        }
    }

    @NotNull
    private static String createInitialCommit(@NotNull Mapper mapper) throws MyGitStateException, IOException  {
        final String treeHash = mapper.map(new Tree());
        final Commit primaryCommit = new Commit(treeHash);
        return mapper.map(primaryCommit);
    }

    private MyGit() {}
}
