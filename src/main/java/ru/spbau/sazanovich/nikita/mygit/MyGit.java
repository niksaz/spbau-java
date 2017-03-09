package ru.spbau.sazanovich.nikita.mygit;

import ru.spbau.sazanovich.nikita.mygit.exceptions.MyGitException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class MyGit {

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void init() throws MyGitException, IOException {
        // TODO: exceptions
        if (Files.exists(Paths.get(".mygit"))) {
            throw new MyGitException("mygit repository is already created");
        } else {
            boolean createdSuccessfully = new File(".mygit").mkdir();
            if (!createdSuccessfully) {
                throw new MyGitException("could not create .mygit/");
            }
            new File(".mygit/HEAD").createNewFile();
            try (PrintWriter printWriter = new PrintWriter(".mygit/HEAD")) {
                printWriter.println("master");
            }
            new File(".mygit/index").createNewFile();
            new File(".mygit/branches").mkdir();
            new File(".mygit/branches/master").createNewFile();
            try (PrintWriter printWriter = new PrintWriter(".mygit/branches/master")) {
                printWriter.println("null");
            }
            new File(".mygit/objects").mkdir();
        }
    }

    public static ArrayList<Path> scanDirectory() throws IOException {
        return Files
                .find(Paths.get("."), Integer.MAX_VALUE, (p, bfa) -> !p.startsWith("./.mygit"))
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
