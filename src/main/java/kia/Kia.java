package kia;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

import kia.command.Command;
import kia.command.CommandType;
import kia.command.ExitCommand;
import kia.exception.KiaException;
import kia.storage.Storage;
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

    /** Component responsible for reading and writing persisted tasks. */
    private final Storage storage;

    /** Startup loading failure, if the persisted data could not be read. */
    private KiaException loadingError;

    /** Creates the application entry-point object. */
    public Kia() {
        storage = new Storage(TASK_FILE);
        tasks = new ArrayList<>();
        try {
            tasks.addAll(storage.load());
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
                storage.save(tasks);
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
                storage.save(tasks);
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
                storage.save(tasks);
            } catch (KiaException e) {
                task.setStatus(previousStatus);
                throw e;
            }
            ui.showTaskUnmarked(task);
        } else {
            Task newTask = createTask(command);
            tasks.add(newTask);
            try {
                storage.save(tasks);
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

}
