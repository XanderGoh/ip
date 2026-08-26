import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Black-box tests for Kia's command handling and in-memory task state.
 *
 * <p>Run this class with {@code java KiaTest}.</p>
 */
public class KiaTest {
    private static final Path TASK_FILE = Path.of("data", "kia.txt");
    /**
     * Runs all Kia command tests.
     *
     * @param args unused command-line arguments
     */
    public static void main(String[] args) {
        testInterleavedValidAndInvalidCommands();
        testMalformedCommandsDoNotCreateTasks();
        testByeStopsCommandProcessing();
        testTaskChangesArePersisted();
        testTasksAreLoadedOnStartup();
        testMissingTaskFileStartsWithEmptyList();
        System.out.println("KiaTest passed.");
    }

    /**
     * Checks that invalid commands interleaved with valid ones do not affect
     * task numbering or completion state.
     */
    private static void testInterleavedValidAndInvalidCommands() {
        String input = String.join("\n",
                "todo read book",
                "blah",
                "deadline return book /by Sunday",
                "deadline missing date",
                "event meeting /from Mon 2pm /to 4pm",
                "todo",
                "list",
                "mark 1",
                "list",
                "mark 99",
                "unmark 1",
                "list",
                "bye",
                "todo after bye") + "\n";

        String output = runKia(input);

        assertContains(output, "Hey!!! What did you do... >:[");
        assertContains(output, "Hey!!! A deadline must include a /by date or time. >:[");
        assertContains(output, "Hey!!! The description of a todo cannot be empty. >:[");
        assertContains(output, "Hey!!! The task number is invalid. >:[");
        assertContains(output, "1.[T][ ] read book");
        assertContains(output, "2.[D][ ] return book (by: Sunday)");
        assertContains(output, "3.[E][ ] meeting (from: Mon 2pm to: 4pm)");
        assertContains(output, "1.[T][X] read book");
        assertLastContains(output, "1.[T][ ] read book");
        assertNotContains(output, "4.[");
        assertNotContains(output, "after bye");
    }

    /**
     * Checks malformed commands and invalid task numbers when the list is empty.
     */
    private static void testMalformedCommandsDoNotCreateTasks() {
        String input = String.join("\n",
                "deadline",
                "event",
                "mark",
                "unmark",
                "todo   ",
                "list",
                "bye") + "\n";

        String output = runKia(input);

        assertContains(output, "Hey!!! A deadline must include a /by date or time. >:[");
        assertContains(output, "Hey!!! An event must include /from and /to date or time values. >:[");
        assertContains(output, "Hey!!! The task number is invalid. >:[");
        assertContains(output, "Hey!!! The description of a todo cannot be empty. >:[");
        assertNotContains(output, "1.[");
    }

    /**
     * Checks that commands after {@code bye} are not processed.
     */
    private static void testByeStopsCommandProcessing() {
        String output = runKia("bye\ntodo should not be added\n");

        assertContains(output, "Aww, goodbye. Hope to see you again soon!");
        assertNotContains(output, "should not be added");
    }

    /**
     * Runs Kia with scripted standard input and captures standard output.
     *
     * @param input commands to provide to Kia
     * @return Kia's complete output
     */
    private static String runKia(String input) {
        clearTaskFile();
        try {
            return runKiaKeepingFile(input);
        } finally {
            clearTaskFile();
        }
    }

    private static String runKiaKeepingFile(String input) {
        java.io.InputStream originalIn = System.in;
        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
            Kia.main(new String[0]);
            return output.toString(StandardCharsets.UTF_8);
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }
    }

    private static void testTaskChangesArePersisted() {
        clearTaskFile();
        try {
            runKiaKeepingFile(String.join("\n",
                    "todo write this",
                    "deadline finish this /by Friday",
                    "mark 1",
                    "unmark 1",
                    "delete 2",
                    "bye") + "\n");
            try {
                String data = Files.readString(TASK_FILE, StandardCharsets.UTF_8);
                require(data.contains("T | 0 | write this"), "Expected the remaining task to be persisted.");
                require(!data.contains("finish this"), "Deleted tasks must not remain in the data file.");
            } catch (java.io.IOException e) {
                throw new AssertionError("Could not read the data file", e);
            }
        } finally {
            clearTaskFile();
        }
    }

    private static void testTasksAreLoadedOnStartup() {
        clearTaskFile();
        try {
            try {
                Files.createDirectories(TASK_FILE.getParent());
                Files.writeString(TASK_FILE, String.join("\n",
                        "not a task record",
                        "T | 1 | loaded todo",
                        "D | 0 | loaded deadline | Friday",
                        "E | 0 | loaded event | 2pm | 4pm") + "\n", StandardCharsets.UTF_8);
            } catch (java.io.IOException e) {
                throw new AssertionError("Could not prepare the data file", e);
            }
            String output = runKiaKeepingFile("list\nbye\n");
            assertContains(output, "1.[T][X] loaded todo");
            assertContains(output, "2.[D][ ] loaded deadline (by: Friday)");
            assertContains(output, "3.[E][ ] loaded event (from: 2pm to: 4pm)");
            assertNotContains(output, "4.[");
        } finally {
            clearTaskFile();
        }
    }

    private static void testMissingTaskFileStartsWithEmptyList() {
        clearTaskFile();
        String output = runKiaKeepingFile("list\nbye\n");
        assertContains(output, "Here ya go! These are the tasks in your list:");
        assertNotContains(output, "1.[");
    }

    private static void clearTaskFile() {
        try {
            Files.deleteIfExists(TASK_FILE);
        } catch (java.io.IOException e) {
            throw new AssertionError("Could not clean up the data file", e);
        }
    }

    private static void assertContains(String output, String expected) {
        require(output.contains(expected), "Expected output to contain: " + expected);
    }

    private static void assertLastContains(String output, String expected) {
        int lastList = output.lastIndexOf("Here ya go! These are the tasks in your list:");
        int lastMatch = output.lastIndexOf(expected);
        require(lastMatch > lastList, "Expected the final list to contain: " + expected);
    }

    private static void assertNotContains(String output, String unexpected) {
        require(!output.contains(unexpected), "Did not expect output to contain: " + unexpected);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
