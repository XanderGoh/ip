package kia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import javafx.application.Application;

/**
 * Unit tests for the JavaFX application entry point.
 */
class MainTest {
    /**
     * Verifies that the Hello World view is a JavaFX application.
     */
    @Test
    void main_extendsApplication_isConfiguredForJavaFx() {
        assertEquals(Application.class, Main.class.getSuperclass());
    }

    /** Verifies that the separate launcher entry point required by JavaFX exists. */
    @Test
    void launcher_hasMainMethod_forwardsToJavaFx() throws NoSuchMethodException {
        assertNotNull(Launcher.class.getDeclaredMethod("main", String[].class));
    }

    /** Verifies that GUI commands are processed by the chatbot logic. */
    @Test
    void getResponse_todoCommand_confirmsTask() throws Exception {
        Path taskFile = Path.of("data", "kia.txt");
        Files.deleteIfExists(taskFile);
        try {
            Kia kia = new Kia();

            String response = kia.getResponse("todo read book");

            assertTrue(response.contains("Gotcha! I've added this task:"));
            assertTrue(response.contains("[T][ ] read book"));
        } finally {
            Files.deleteIfExists(taskFile);
        }
    }

    /** Verifies that the shared status handler preserves mark and unmark behavior. */
    @Test
    void getResponse_markAndUnmarkCommands_updatesTaskState() throws Exception {
        Path taskFile = Path.of("data", "kia.txt");
        Files.deleteIfExists(taskFile);
        try {
            Kia kia = new Kia();
            kia.getResponse("todo read book");

            assertTrue(kia.getResponse("mark 1").contains("[T][X] read book"));
            assertTrue(kia.getResponse("unmark 1").contains("[T][ ] read book"));
        } finally {
            Files.deleteIfExists(taskFile);
        }
    }

    /** Verifies that the FXML view is included in the application resources. */
    @Test
    void mainWindow_fxmlViewExists_inApplicationResources() {
        assertNotNull(Main.class.getResource("/view/MainWindow.fxml"));
    }
}
