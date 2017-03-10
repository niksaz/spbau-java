package ru.spbau.sazanovich.nikita.mygit;

import ru.spbau.sazanovich.nikita.mygit.exceptions.MyGitInitException;
import ru.spbau.sazanovich.nikita.mygit.objects.Tree;
import ru.spbau.sazanovich.nikita.mygit.utils.Mapper;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MyGit {

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void init() throws MyGitInitException, IOException {
        if (Files.exists(Paths.get(".mygit"))) {
            throw new MyGitInitException("mygit repository is already created");
        } else {
            boolean createdSuccessfully = new File(".mygit").mkdir();
            if (!createdSuccessfully) {
                throw new MyGitInitException("could not create .mygit/");
            }
            new File(".mygit/HEAD").createNewFile();
            try (PrintWriter printWriter = new PrintWriter(".mygit/HEAD")) {
                printWriter.println("master");
            }
            new File(".mygit/index").createNewFile();
            createdSuccessfully = new File(".mygit/objects").mkdir();
            if (!createdSuccessfully) {
                throw new MyGitInitException("could not create .mygit/objects");
            }
            final String initialTreeHash = Mapper.map(new Tree());
            createdSuccessfully = new File(".mygit/branches").mkdir();
            if (!createdSuccessfully) {
                throw new MyGitInitException("could not create .mygit/branches");
            }
            new File(".mygit/branches/master").createNewFile();
            try (PrintWriter printWriter = new PrintWriter(".mygit/branches/master")) {
                printWriter.println(initialTreeHash);
            }
        }
    }

    private MyGit() {}
}
