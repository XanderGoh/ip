import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Black-box tests for Kia's command handling and in-memory task state.
 *
 * <p>Run this class with {@code java KiaTest}.</p>
 */
public class KiaTest {
    /**
     * Runs all Kia command tests.
     *
     * @param args unused command-line arguments
     */
    public static void main(String[] args) {
        testInterleavedValidAndInvalidCommands();
        testMalformedCommandsDoNotCreateTasks();
        testByeStopsCommandProcessing();
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

        assertContains(output, "OOPS!!! I'm sorry, but I don't know what that means :-(");
        assertContains(output, "OOPS!!! A deadline must include a /by date or time.");
        assertContains(output, "OOPS!!! The description of a todo cannot be empty.");
        assertContains(output, "OOPS!!! The task number is invalid.");
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

        assertContains(output, "OOPS!!! A deadline must include a /by date or time.");
        assertContains(output, "OOPS!!! An event must include /from and /to date or time values.");
        assertContains(output, "OOPS!!! The task number is invalid.");
        assertContains(output, "OOPS!!! The description of a todo cannot be empty.");
        assertNotContains(output, "1.[");
    }

    /**
     * Checks that commands after {@code bye} are not processed.
     */
    private static void testByeStopsCommandProcessing() {
        String output = runKia("bye\ntodo should not be added\n");

        assertContains(output, "Bye. Hope to see you again soon!");
        assertNotContains(output, "should not be added");
    }

    /**
     * Runs Kia with scripted standard input and captures standard output.
     *
     * @param input commands to provide to Kia
     * @return Kia's complete output
     */
    private static String runKia(String input) {
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

    private static void assertContains(String output, String expected) {
        require(output.contains(expected), "Expected output to contain: " + expected);
    }

    private static void assertLastContains(String output, String expected) {
        int lastList = output.lastIndexOf("Here are the tasks in your list:");
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
