package ru.spbau.sazanovich.nikita.mygit;

import org.jetbrains.annotations.NotNull;
import ru.spbau.sazanovich.nikita.mygit.exceptions.MyGitFilesystemException;
import ru.spbau.sazanovich.nikita.mygit.exceptions.MyGitStateException;
import ru.spbau.sazanovich.nikita.mygit.objects.Branch;
import ru.spbau.sazanovich.nikita.mygit.objects.Commit;
import ru.spbau.sazanovich.nikita.mygit.objects.Tree;
import ru.spbau.sazanovich.nikita.mygit.utils.Mapper;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MyGit {

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void init() throws MyGitFilesystemException, IOException, MyGitStateException {
        final Path myGitPath = Paths.get(".mygit");
        if (myGitPath.toFile().exists()) {
            throw new MyGitFilesystemException("mygit repository is already created");
        } else {
            Files.createDirectory(myGitPath);
            Files.createFile(Paths.get(myGitPath.toString(), "HEAD"));
            try (PrintWriter printWriter = new PrintWriter(".mygit/HEAD")) {
                printWriter.println(Branch.TYPE);
                printWriter.println("master");
            }
            Files.createFile(Paths.get(myGitPath.toString(), "index"));
            Files.createDirectory(Paths.get(myGitPath.toString(), "objects"));
            Files.createDirectory(Paths.get(myGitPath.toString(), "branches"));
            final Mapper mapper = new Mapper(myGitPath.toAbsolutePath().getParent());
            final String commitHash = createInitialCommit(mapper);
            mapper.writeBranch("master", commitHash);
        }
    }

    @NotNull
    private static String createInitialCommit(@NotNull Mapper mapper)
            throws MyGitFilesystemException, IOException, MyGitStateException {
        final String treeHash = mapper.map(new Tree());
        final Commit primaryCommit = new Commit(treeHash);
        return mapper.map(primaryCommit);
    }

    private MyGit() {}
}
