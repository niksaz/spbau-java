package ru.spbau.sazanovich.nikita.mygit;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MyGit {

    public static boolean init() {
        // TODO: exceptions
        if (Files.exists(Paths.get(".mygit"))) {
            return false;
        } else {
            boolean successful = new File(".mygit").mkdir();
            return successful;
        }
    }
}
