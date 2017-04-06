package ru.spbau.sazanovich.nikita.console;

import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.jetbrains.annotations.NotNull;
import ru.spbau.sazanovich.nikita.mygit.logger.Log4j2ContextBuilder;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Provides a console access to MyGit system.
 */
public class ConsoleApp {

    /**
     * Entrance point to the console application.
     *
     * @param args command line arguments
     */
    public static void main(@NotNull String[] args) {
        final Path path = Paths.get("/Users",  "niksaz", "Git");

        final LoggerContext context = Log4j2ContextBuilder.createContext("MyGitLogger", path);
        final Logger logger = context.getRootLogger();
        logger.trace("HELLO!");

        final Path currentDirectory = Paths.get(System.getProperty("user.dir"));
        final CommandLineArgsHandler handler = new CommandLineArgsHandler(System.out, currentDirectory);
        try {
            handler.handle(args);
        } catch (CommandNotSupportedException e) {
            System.out.println("Entered command is not supported. " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unsuccessful operation: " + e.getMessage());
        }
    }

    private ConsoleApp() {}
}
