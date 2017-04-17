package ru.spbau.sazanovich.nikita.console;

import org.jetbrains.annotations.NotNull;
import ru.spbau.sazanovich.nikita.mygit.MyGitException;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Provides a console access to MyGit system.
 */
public class Main {

    /**
     * Entrance point to the console application.
     *
     * @param args command line arguments
     */
    public static void main(@NotNull String[] args) {
        final Path currentDirectory = Paths.get(System.getProperty("user.dir"));
        final CommandLineArgsHandler handler = new CommandLineArgsHandler(System.out, currentDirectory);
        try {
            handler.handle(args);
        } catch (CommandNotSupportedException e) {
            System.out.println("Entered command is not supported. " + e.getMessage());
        } catch (MyGitException e) {
            System.out.println("Unsuccessful operation: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unknown error occurred: " + e.getMessage());
        }
    }

    private Main() {}
}
