package kia.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import kia.task.Deadline;
import kia.task.Event;
import kia.task.Task;
import kia.task.Todo;

/** Unit tests for task persistence and reconstruction. */
class StorageTest {
    @TempDir
    Path temporaryDirectory;

    /** Verifies that a missing data file is treated as an empty task list. */
    @Test
    void load_missingFile_returnsEmptyList() throws Exception {
        Storage storage = new Storage(temporaryDirectory.resolve("missing.txt"));

        assertTrue(storage.load().isEmpty());
    }

    /** Verifies that all supported task types survive a save/load round trip. */
    @Test
    void saveThenLoad_allTaskTypes_reconstructsTasks() throws Exception {
        Path file = temporaryDirectory.resolve("nested").resolve("tasks.txt");
        Storage storage = new Storage(file);
        ArrayList<Task> tasks = new ArrayList<>();
        Todo todo = new Todo("read book");
        todo.markAsDone();
        tasks.add(todo);
        tasks.add(new Deadline("return book", LocalDate.of(2019, 12, 2)));
        tasks.add(new Event("project meeting", "2pm", "4pm"));

        storage.save(tasks);

        ArrayList<Task> loadedTasks = storage.load();
        assertEquals(3, loadedTasks.size());
        assertEquals(tasks.get(0).toStorageString(), loadedTasks.get(0).toStorageString());
        assertEquals(tasks.get(1).toStorageString(), loadedTasks.get(1).toStorageString());
        assertEquals(tasks.get(2).toStorageString(), loadedTasks.get(2).toStorageString());
    }

    /** Verifies that malformed records are skipped while valid records are retained. */
    @Test
    void loadMixedRecords_malformedLinesAreSkipped() throws Exception {
        Path file = temporaryDirectory.resolve("tasks.txt");
        Files.writeString(file, String.join("\n",
                "invalid record",
                "T | 1 | loaded task",
                "D | 0 | invalid date | 2019-02-30",
                "E | 0 | valid event | 2pm | 4pm",
                "T | 0 | extra field | ignored") + "\n", StandardCharsets.UTF_8);
        Storage storage = new Storage(file);

        ArrayList<Task> loadedTasks = storage.load();

        assertEquals(2, loadedTasks.size());
        assertEquals("T | 1 | loaded task", loadedTasks.get(0).toStorageString());
        assertEquals("E | 0 | valid event | 2pm | 4pm", loadedTasks.get(1).toStorageString());
    }
}
