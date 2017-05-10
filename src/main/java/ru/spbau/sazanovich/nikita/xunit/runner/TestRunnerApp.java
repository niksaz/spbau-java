package ru.spbau.sazanovich.nikita.xunit.runner;

import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;

/**
 * Class which runs all test classes with given, as command line arguments, names.
 */
public class TestRunnerApp {

    /**
     * Runs tests in test classes with given names.
     *
     * @param args names of test classes
     */
    public static void main(@NotNull String[] args) {
        PrintStream logStream = System.out;
        Executor executor = new Executor(logStream);
        for (String testClassName : args) {
            try {
                executor.execute(testClassName);
            } catch (IllegalTestClassException e) {
                e.printStackTrace(logStream);
            }
        }
    }
}
