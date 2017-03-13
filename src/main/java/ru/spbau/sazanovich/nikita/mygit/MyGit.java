package ru.spbau.sazanovich.nikita.mygit;

import org.jetbrains.annotations.NotNull;
import ru.spbau.sazanovich.nikita.mygit.exceptions.MyGitAlreadyInitialized;
import ru.spbau.sazanovich.nikita.mygit.exceptions.MyGitStateException;
import ru.spbau.sazanovich.nikita.mygit.logs.HeadStatus;
import ru.spbau.sazanovich.nikita.mygit.objects.Branch;
import ru.spbau.sazanovich.nikita.mygit.objects.Commit;
import ru.spbau.sazanovich.nikita.mygit.objects.Tree;
import ru.spbau.sazanovich.nikita.mygit.utils.Mapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MyGit {

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void init() throws MyGitAlreadyInitialized, MyGitStateException, IOException {
        final Path myGitPath = Paths.get(".mygit");
        if (myGitPath.toFile().exists()) {
            throw new MyGitAlreadyInitialized();
        } else {
            Files.createDirectory(myGitPath);
            final Mapper mapper = new Mapper(myGitPath.toAbsolutePath().getParent());
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
