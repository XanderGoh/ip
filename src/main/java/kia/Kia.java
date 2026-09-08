package kia;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import kia.command.Command;
import kia.command.CommandType;
import kia.command.ExitCommand;
import kia.exception.KiaException;
import kia.task.Deadline;
import kia.task.Event;
import kia.task.Task;
import kia.task.TaskStatus;
import kia.task.Todo;
import kia.ui.Ui;

/**
 * Greets the user, manages in-memory tasks, and exits when the user enters {@code bye}.
 */
public class Kia {
    /** Relative, OS-independent location of Kia's task data. */
    private static final Path TASK_FILE = Path.of("data", "kia.txt");

    /** Tasks managed by this Kia instance. */
    private final ArrayList<Task> tasks;

    /** Startup loading failure, if the persisted data could not be read. */
    private KiaException loadingError;

    /** Creates the application entry-point object. */
    public Kia() {
        tasks = new ArrayList<>();
        try {
            loadTasks(tasks);
        } catch (KiaException e) {
            loadingError = e;
        }
    }

    /**
     * Starts Kia, processes commands until the user exits, and persists task changes.
     *
     * @param args command-line arguments, currently unused
     */
    public static void main(String[] args) {
        Kia kia = new Kia();
        Ui ui = new Ui();
        if (kia.loadingError != null) {
            ui.showLoadingError(kia.loadingError);
        }

        ui.showWelcome();

        while (ui.hasNextCommand()) {
            String command = ui.readCommand();
            ui.showSeparator();

            boolean shouldExit = false;
            try {
                shouldExit = kia.processCommand(command, ui);
            } catch (KiaException e) {
                ui.showError(e);
            }

            if (shouldExit) {
                break;
            }
            ui.showSeparator();
        }
    }

    /**
     * Processes one command and captures the same response used by the console UI.
     *
     * @param input command entered in the GUI
     * @return the response text, without console separators
     */
    public String getResponse(String input) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Ui responseUi = new Ui(new PrintStream(output, true, StandardCharsets.UTF_8));
        if (loadingError != null) {
            responseUi.showLoadingError(loadingError);
        }
        try {
            processCommand(input == null ? "" : input, responseUi);
        } catch (KiaException e) {
            responseUi.showError(e);
        }
        return output.toString(StandardCharsets.UTF_8).trim();
    }

    /**
     * Executes one command against this instance's task list.
     *
     * @param command command to execute
     * @param ui interface used to display the result
     * @return {@code true} when the command is {@code bye}
     * @throws KiaException if the command is invalid or persistence fails
     */
    private boolean processCommand(String command, Ui ui) throws KiaException {
        CommandType commandType = classifyCommand(command);
        if (commandType == CommandType.BYE) {
            Command exitCommand = new ExitCommand();
            exitCommand.execute(tasks, ui);
            return exitCommand.isExit();
        } else if (commandType == CommandType.LIST) {
            ui.showTaskList(tasks);
        } else if (commandType == CommandType.FIND) {
            String keyword = parseFindKeyword(command);
            ui.showMatchingTasks(tasks, keyword);
        } else if (commandType == CommandType.DELETE) {
            int taskNumber = parseTaskNumber(command, "delete ", tasks.size());
            Task removedTask = tasks.remove(taskNumber - 1);
            try {
                saveTasks(tasks);
            } catch (KiaException e) {
                tasks.add(taskNumber - 1, removedTask);
                throw e;
            }
            ui.showTaskDeleted(removedTask, tasks.size());
        } else if (commandType == CommandType.MARK) {
            int taskNumber = parseTaskNumber(command, "mark ", tasks.size());
            Task task = tasks.get(taskNumber - 1);
            TaskStatus previousStatus = task.getStatus();
            task.markAsDone();
            try {
                saveTasks(tasks);
            } catch (KiaException e) {
                task.setStatus(previousStatus);
                throw e;
            }
            ui.showTaskMarked(task);
        } else if (commandType == CommandType.UNMARK) {
            int taskNumber = parseTaskNumber(command, "unmark ", tasks.size());
            Task task = tasks.get(taskNumber - 1);
            TaskStatus previousStatus = task.getStatus();
            task.markAsUndone();
            try {
                saveTasks(tasks);
            } catch (KiaException e) {
                task.setStatus(previousStatus);
                throw e;
            }
            ui.showTaskUnmarked(task);
        } else {
            Task newTask = createTask(command);
            tasks.add(newTask);
            try {
                saveTasks(tasks);
            } catch (KiaException e) {
                tasks.remove(tasks.size() - 1);
                throw e;
            }
            ui.showTaskAdded(newTask, tasks.size());
        }
        return false;
    }

    /**
     * Classifies a command without validating its arguments.
     *
     * @param command the command entered by the user
     * @return the command category
     */
    private static CommandType classifyCommand(String command) {
        if (command.equals("bye")) {
            return CommandType.BYE;
        } else if (command.equals("list")) {
            return CommandType.LIST;
        } else if (command.equals("delete") || command.startsWith("delete ")) {
            return CommandType.DELETE;
        } else if (command.equals("find") || command.startsWith("find ")) {
            return CommandType.FIND;
        } else if (command.equals("mark") || command.startsWith("mark ")) {
            return CommandType.MARK;
        } else if (command.equals("unmark") || command.startsWith("unmark ")) {
            return CommandType.UNMARK;
        } else if (command.equals("todo") || command.startsWith("todo ")) {
            return CommandType.TODO;
        } else if (command.equals("deadline") || command.startsWith("deadline ")) {
            return CommandType.DEADLINE;
        } else if (command.equals("event") || command.startsWith("event ")) {
            return CommandType.EVENT;
        }
        return CommandType.UNKNOWN;
    }

    /**
     * Parses a task command into the appropriate task subtype.
     *
     * @param command the command entered by the user
     * @return a new task
     */
    private static Task createTask(String command) throws KiaException {
        if (command.equals("todo") || command.startsWith("todo ")) {
            String description = command.length() <= "todo ".length()
                    ? "" : command.substring("todo ".length()).trim();
            if (description.isEmpty()) {
                throw new KiaException("The description of a todo cannot be empty.");
            }
            return new Todo(description);
        }

        if (command.equals("deadline") || command.startsWith("deadline ")) {
            String details = command.length() <= "deadline ".length()
                    ? "" : command.substring("deadline ".length()).trim();
            int byIndex = details.indexOf(" /by ");
            if (byIndex < 0) {
                throw new KiaException("A deadline must include a /by date or time.");
            }
            String description = details.substring(0, byIndex).trim();
            String by = details.substring(byIndex + " /by ".length()).trim();
            if (description.isEmpty()) {
                throw new KiaException("The description of a deadline cannot be empty.");
            }
            if (by.isEmpty()) {
                throw new KiaException("The /by date or time of a deadline cannot be empty.");
            }
            try {
                return new Deadline(description, LocalDate.parse(by));
            } catch (DateTimeParseException e) {
                throw new KiaException("The /by date or time must use yyyy-MM-dd format.");
            }
        }

        if (command.equals("event") || command.startsWith("event ")) {
            String details = command.length() <= "event ".length()
                    ? "" : command.substring("event ".length()).trim();
            int fromIndex = details.indexOf(" /from ");
            int toIndex = fromIndex < 0 ? -1 : details.indexOf(" /to ", fromIndex + " /from ".length());
            if (fromIndex < 0 || toIndex < 0) {
                throw new KiaException("An event must include /from and /to date or time values.");
            }
            String description = details.substring(0, fromIndex).trim();
            String from = details.substring(fromIndex + " /from ".length(), toIndex).trim();
            String to = details.substring(toIndex + " /to ".length()).trim();
            if (description.isEmpty()) {
                throw new KiaException("The description of an event cannot be empty.");
            }
            if (from.isEmpty() || to.isEmpty()) {
                throw new KiaException("The /from and /to values of an event cannot be empty.");
            }
            return new Event(description, from, to);
        }

        throw new KiaException("What did you do...");
    }

    /**
     * Extracts and validates the keyword from a find command.
     *
     * @param command the complete find command
     * @return the keyword to search for
     * @throws KiaException if no keyword was supplied
     */
    private static String parseFindKeyword(String command) throws KiaException {
        String keyword = command.length() <= "find ".length()
                ? "" : command.substring("find ".length()).trim();
        if (keyword.isEmpty()) {
            throw new KiaException("A find command must include a keyword.");
        }
        return keyword;
    }

    /**
     * Parses and validates the task number in a mark or unmark command.
     *
     * @param command the command entered by the user
     * @param prefix the command prefix to remove
     * @param taskCount the number of stored tasks
     * @return the one-based task number
     * @throws KiaException if the number is missing, malformed, or out of range
     */
    private static int parseTaskNumber(String command, String prefix, int taskCount) throws KiaException {
        String taskNumberText = command.length() <= prefix.length()
                ? "" : command.substring(prefix.length()).trim();
        try {
            int taskNumber = Integer.parseInt(taskNumberText);
            if (taskNumber < 1 || taskNumber > taskCount) {
                throw new KiaException("The task number is invalid.");
            }
            return taskNumber;
        } catch (NumberFormatException e) {
            throw new KiaException("The task number is invalid.");
        }
    }

    /**
     * Writes the current task list to disk, creating its parent directory when needed.
     *
     * @param tasks tasks to persist
     * @throws KiaException if the file cannot be written
     */
    private static void saveTasks(ArrayList<Task> tasks) throws KiaException {
        try {
            Path parent = TASK_FILE.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            List<String> records = tasks.stream()
                    .map(Task::toStorageString)
                    .toList();
            Files.write(TASK_FILE, records, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);
        } catch (IOException | SecurityException e) {
            throw new KiaException("Unable to save tasks to disk.");
        }
    }

    /**
     * Loads valid task records from disk. Missing files are treated as an empty list.
     * Invalid individual records are skipped so one corrupt line does not hide valid tasks.
     *
     * @param tasks destination list
     * @throws KiaException if the data file cannot be read
     */
    private static void loadTasks(ArrayList<Task> tasks) throws KiaException {
        if (tasks == null) {
            throw new KiaException("Unable to load tasks from disk.");
        }
        try {
            if (Files.notExists(TASK_FILE)) {
                return;
            }
            if (!Files.isRegularFile(TASK_FILE)) {
                throw new KiaException("Unable to load tasks from disk.");
            }

            ArrayList<Task> loadedTasks = new ArrayList<>();
            int lineNumber = 0;
            for (String line : Files.readAllLines(TASK_FILE, StandardCharsets.UTF_8)) {
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
            tasks.addAll(loadedTasks);
        } catch (IOException | SecurityException e) {
            throw new KiaException("Unable to load tasks from disk.");
        }
    }

    /** Parses one persisted task record. */
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

        Task task;
        if (type.equals("T") && fields.length == 3) {
            task = new Todo(description);
        } else if (type.equals("D") && fields.length == 4 && !fields[3].trim().isEmpty()) {
            try {
                task = new Deadline(description, LocalDate.parse(fields[3].trim()));
            } catch (DateTimeParseException e) {
                throw new KiaException("A deadline record has an invalid date.");
            }
        } else if (type.equals("E") && fields.length == 5
                && !fields[3].trim().isEmpty() && !fields[4].trim().isEmpty()) {
            task = new Event(description, fields[3].trim(), fields[4].trim());
        } else {
            throw new KiaException("A task record has an invalid format.");
        }
        if (status.equals("1")) {
            task.markAsDone();
        }
        return task;
    }
}
