package ru.spbau.sazanovich.nikita.sp;

import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static ru.spbau.sazanovich.nikita.sp.SecondPartTasks.*;

public class SecondPartTasksTest {

    private static final int NUMBER_OF_CHECKS = 5;
    private static final double DELTA_FOR_DOUBLE_COMPARISON = 1e-3;

    @Test
    public void testFindQuotes() throws IOException {
        final TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();

        final File file1 = temporaryFolder.newFile("TemporaryFolder");
        final String line1 = "The TemporaryFolder Rule allows creation of files and folders that should be deleted when the test method finishes (whether it passes or fails).";
        final String line2 = "Whether the deletion is successful or not is not checked by this rule.";
        final String line3 = "No exception will be thrown in case the deletion fails.";
        writeToFileLines(file1, line1, line2, line3);

        final File file2 = temporaryFolder.newFile("ExternalResource");
        final String line4 = "A base class for Rules (like TemporaryFolder) that set up an external resource before a test (a file, socket, server, database connection, etc.), and guarantee to tear it down afterward.";
        final String line5 = "Modifies the method-running Statement to implement this test-running rule.";
        writeToFileLines(file2, line4, line5);

        final File file3 = temporaryFolder.newFile("File");
        final String line6 = "An abstract representation of file and directory pathnames.";
        final String line7 = "User interfaces and operating systems use system-dependent pathname strings to name files and directories.";
        writeToFileLines(file3, line6, line7);

        final List<String> paths = Arrays.asList(file1.getPath(), file2.getPath(), file3.getPath());
        final List<String> correctLines = Arrays.asList(line1, line4, line6, line7);
        final List<String> assumedLines = findQuotes(paths, "file");
        assertEquals(correctLines.size(), assumedLines.size());
        Collections.sort(correctLines);
        Collections.sort(assumedLines);
        assertEquals(correctLines, assumedLines);
    }

    @Test
    public void testPiDividedBy4() {
        for (int check = 0; check < NUMBER_OF_CHECKS; check++) {
            assertEquals(Math.PI / 4, piDividedBy4(), DELTA_FOR_DOUBLE_COMPARISON);
        }
    }

    @Test
    public void testFindPrinter() {
        final Map<String, List<String>> compositions = new HashMap<String, List<String>>() {
            {
                put("Shakespeare",
                    Arrays.asList(
                            "Be not afraid of greatness: some are born great, some achieve greatness, and some have greatness thrust upon them.",
                            "The course of true love never did run smooth.",
                            "There is nothing either good or bad, but thinking makes it so.",
                            "Cowards die many times before their deaths; the valiant never taste of death but once."));
                put("Tolstoy",
                    Arrays.asList(
                            "Everyone thinks of changing the world, but no one thinks of changing himself.",
                            "If you want to be happy, be.",
                            "The two most powerful warriors are patience and time.",
                            "Truth, like gold, is to be obtained not by its growth, but by washing away from it all that is not gold.",
                            "The sole meaning of life is to serve humanity."));
                put("Twain",
                    Arrays.asList(
                            "Whenever you find yourself on the side of the majority, it is time to pause and reflect.",
                            "If you tell the truth, you don't have to remember anything.",
                            "I have never let my schooling interfere with my education.",
                            "The man who does not read good books has no advantage over the man who cannot read them."));
            }
        };
        assertEquals("Tolstoy", findPrinter(compositions));
    }

    @Test
    public void testCalculateGlobalOrder() {
        final Map<String, Integer> order1 = new HashMap<String, Integer>() {
            {
                put("apple", 10);
                put("banana", 19);
                put("orange", 2);
            }
        };
        final Map<String, Integer> order2 = new HashMap<String, Integer>() {
            {
                put("apple", 15);
                put("grapes", 10);
                put("avocado", 12);
            }
        };
        final Map<String, Integer> order3 = new HashMap<String, Integer>() {
            {
                put("avocado", 13);
                put("grapes", 15);
                put("banana", 6);
            }
        };
        final Map<String, Integer> correctGlobalOrder = new HashMap<String, Integer>() {
            {
                put("apple", 25);
                put("banana", 25);
                put("orange", 2);
                put("grapes", 25);
                put("avocado", 25);
            }
        };
        assertEquals(correctGlobalOrder, calculateGlobalOrder(Arrays.asList(order1, order2, order3)));
    }

    private static void writeToFileLines(File file, String... lines) throws IOException {
        final PrintStream printStream = new PrintStream(file);
        for (String line : lines) {
            printStream.println(line);
        }
    }
}