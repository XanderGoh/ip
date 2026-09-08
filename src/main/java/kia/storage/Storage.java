package kia.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import kia.exception.KiaException;
import kia.task.Deadline;
import kia.task.Event;
import kia.task.Task;
import kia.task.Todo;

/**
 * Persists Kia's tasks using a compact, pipe-delimited text format.
 */
public class Storage {
    private final Path filePath;

    /**
     * Creates a storage component for the supplied data-file path.
     *
     * @param filePath path of the task data file
     */
    public Storage(Path filePath) {
        this.filePath = Objects.requireNonNull(filePath);
    }

    /**
     * Saves the supplied tasks, creating the parent directory when necessary.
     *
     * @param tasks tasks to persist
     * @throws KiaException if the file cannot be written
     */
    public void save(List<Task> tasks) throws KiaException {
        try {
            Path parent = filePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            ArrayList<String> records = new ArrayList<>();
            for (Task task : tasks) {
                records.add(task.toStorageString());
            }
            Files.write(filePath, records, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);
        } catch (IOException | SecurityException e) {
            throw new KiaException("Unable to save tasks to disk.");
        }
    }

    /**
     * Loads valid task records from disk. A missing file represents an empty list.
     * Invalid individual records are skipped so one corrupt line does not hide valid tasks.
     *
     * @return tasks loaded from the data file
     * @throws KiaException if the data file cannot be read
     */
    public ArrayList<Task> load() throws KiaException {
        try {
            if (Files.notExists(filePath)) {
                return new ArrayList<>();
            }
            if (!Files.isRegularFile(filePath)) {
                throw new KiaException("Unable to load tasks from disk.");
            }

            ArrayList<Task> loadedTasks = new ArrayList<>();
            int lineNumber = 0;
            for (String line : Files.readAllLines(filePath, StandardCharsets.UTF_8)) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                try {
                    loadedTasks.add(parseStoredTask(line));
                } catch (KiaException e) {
                    System.out.println("Hey!!! Skipping invalid task data on line " + lineNumber + ".");
                }
            }
            return loadedTasks;
        } catch (IOException | SecurityException e) {
            throw new KiaException("Unable to load tasks from disk.");
        }
    }

    /** Parses one persisted task record into its corresponding task subtype. */
    private static Task parseStoredTask(String line) throws KiaException {
        if (line == null) {
            throw new KiaException("A task record is incomplete.");
        }
        // A UTF-8 BOM can appear at the start of a file created by some editors.
        if (!line.isEmpty() && line.charAt(0) == '\uFEFF') {
            line = line.substring(1);
        }
        String[] fields = line.split("\\s*\\|\\s*", -1);
        if (fields.length < 3) {
            throw new KiaException("A task record is incomplete.");
        }
        String type = fields[0].trim();
        String status = fields[1].trim();
        String description = fields[2].trim();
        if (!(status.equals("0") || status.equals("1"))) {
            throw new KiaException("A task record has an invalid status.");
        }
        if (description.isEmpty()) {
            throw new KiaException("A task record has an empty description.");
        }

        Task task = switch (type) {
            case "T" -> parseTodo(fields, description);
            case "D" -> parseDeadline(fields, description);
            case "E" -> parseEvent(fields, description);
            default -> throw new KiaException("A task record has an invalid format.");
        };
        if (status.equals("1")) {
            task.markAsDone();
        }
        return task;
    }

    /** Parses a to-do record after common fields have been validated. */
    private static Task parseTodo(String[] fields, String description) throws KiaException {
        if (fields.length != 3) {
            throw new KiaException("A task record has an invalid format.");
        }
        return new Todo(description);
    }

    /** Parses a deadline record after common fields have been validated. */
    private static Task parseDeadline(String[] fields, String description) throws KiaException {
        if (fields.length != 4 || fields[3].trim().isEmpty()) {
            throw new KiaException("A task record has an invalid format.");
        }
        try {
            return new Deadline(description, LocalDate.parse(fields[3].trim()));
        } catch (DateTimeParseException e) {
            throw new KiaException("A deadline record has an invalid date.");
        }
    }

    /** Parses an event record after common fields have been validated. */
    private static Task parseEvent(String[] fields, String description) throws KiaException {
        if (fields.length != 5 || fields[3].trim().isEmpty() || fields[4].trim().isEmpty()) {
            throw new KiaException("A task record has an invalid format.");
        }
        return new Event(description, fields[3].trim(), fields[4].trim());
    }
}
