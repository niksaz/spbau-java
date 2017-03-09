package ru.spbau.sazanovich.nikita;

import ru.spbau.sazanovich.nikita.mygit.MyGit;
import ru.spbau.sazanovich.nikita.mygit.MyGitHandler;
import ru.spbau.sazanovich.nikita.mygit.exceptions.MyGitException;
import ru.spbau.sazanovich.nikita.mygit.objects.Tree;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class ConsoleApp {

    private static final String INIT_CMD = "init";
    private static final String ADD_CMD = "add";
    private static final String LOG_CMD = "log";
    private static final String STATUS_CMD = "status";
    private static final String BRANCH_CMD = "branch";
    private static final String CHECKOUT_CMD = "checkout";
    private static final String COMMIT_CMD = "commit";
    private static final String MERGE_CMD = "merge";

    public static void main(String[] args) {
        /*
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            String s = "test content";
            digest.update(s.getBytes());
            byte[] sha1 = digest.digest();
            final String hexString = new HexBinaryAdapter().marshal(sha1);
            System.out.println(hexString);
        } catch (NoSuchAlgorithmException ignored) {
        }
        */

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(new Tree("name"));
            objectOutputStream.flush();
            System.out.println(byteArrayOutputStream.toByteArray().length);

        } catch (IOException e) {
            e.printStackTrace();
        }


        if (args.length - 100 < 0) {
            return;
        }

        try {
            if (args.length > 0 && args[0].equals(INIT_CMD)) {
                try {
                    MyGit.init();
                } catch (MyGitException e) {
                    System.out.println("Error occurred while trying to create a mygit repository: " + e.getMessage());
                    return;
                }
                System.out.println("Successfully initialized mygit repository.");
                return;
            }

            final MyGitHandler handler = new MyGitHandler();

            if (args.length > 0 && args[0].equals(STATUS_CMD)) {
                final ArrayList<Path> paths = handler.scanDirectory();
                final Path currentPath = Paths.get("").toAbsolutePath();
                System.out.println(currentPath);
                for (Path path : paths) {
                    System.out.println("? " + currentPath.relativize(path));
                }
            } else {
                showHelp();
            }
        } catch (Exception e) {
            System.out.println("An exception occurred: " + e.getMessage());
        }
    }

    private static void showHelp() {
        System.out.println(
                "usage: mygit <command> [<args>]\n" +
                "\n" +
                "start a working area:\n" +
                "  " + INIT_CMD + "\n" +
                "\n" +
                "work on the current change:\n" +
                "  " + ADD_CMD + " [<files>]\n" +
                "\n" +
                "examine the history and state:\n" +
                "  " + LOG_CMD + "\n" +
                "  " + STATUS_CMD + "\n" +
                "\n" +
                "grow and tweak your common history:\n" +
                "  " + BRANCH_CMD + " [<name> | -d <name>]\n" +
                "  " + CHECKOUT_CMD + " <branch> | <revision>\n" +
                "  " + COMMIT_CMD +  " <message>\n" +
                "  " + MERGE_CMD + " <branch>");
    }
}
