package kia;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;

import kia.command.Command;
import kia.command.CommandType;
import kia.command.ExitCommand;
import kia.exception.KiaException;
import kia.parser.Parser;
import kia.storage.Storage;
import kia.task.Task;
import kia.task.TaskStatus;
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
        CommandType commandType = Parser.classifyCommand(command);
        switch (commandType) {
            case BYE:
                return executeExit(ui);
            case LIST:
                ui.showTaskList(tasks);
                break;
            case FIND:
                showMatchingTasks(command, ui);
                break;
            case DELETE:
                deleteTask(command, ui);
                break;
            case MARK:
                updateTaskStatus(command, ui, true);
                break;
            case UNMARK:
                updateTaskStatus(command, ui, false);
                break;
            default:
                addTask(command, ui);
                break;
        }
        return false;
    }

    /**
     * Executes the exit command and reports whether the command loop should stop.
     *
     * @throws KiaException if the exit command cannot be executed
     */
    private boolean executeExit(Ui ui) throws KiaException {
        Command exitCommand = new ExitCommand();
        exitCommand.execute(tasks, ui);
        return exitCommand.isExit();
    }

    /** Displays tasks matching the keyword supplied by a find command. */
    private void showMatchingTasks(String command, Ui ui) throws KiaException {
        String keyword = Parser.parseFindKeyword(command);
        ui.showMatchingTasks(tasks, keyword);
    }

    /** Deletes a task and rolls back the in-memory change if saving fails. */
    private void deleteTask(String command, Ui ui) throws KiaException {
        int taskNumber = Parser.parseTaskNumber(command, "delete ", tasks.size());
        Task removedTask = tasks.remove(taskNumber - 1);
        try {
            storage.save(tasks);
        } catch (KiaException e) {
            tasks.add(taskNumber - 1, removedTask);
            throw e;
        }
        ui.showTaskDeleted(removedTask, tasks.size());
    }

    /** Marks or unmarks a task and rolls back its state if saving fails. */
    private void updateTaskStatus(String command, Ui ui, boolean shouldMarkDone) throws KiaException {
        String commandPrefix = shouldMarkDone ? "mark " : "unmark ";
        int taskNumber = Parser.parseTaskNumber(command, commandPrefix, tasks.size());
        Task task = tasks.get(taskNumber - 1);
        TaskStatus previousStatus = task.getStatus();
        if (shouldMarkDone) {
            task.markAsDone();
        } else {
            task.markAsUndone();
        }
        try {
            storage.save(tasks);
        } catch (KiaException e) {
            task.setStatus(previousStatus);
            throw e;
        }
        if (shouldMarkDone) {
            ui.showTaskMarked(task);
        } else {
            ui.showTaskUnmarked(task);
        }
    }

    /** Adds a task and rolls back the in-memory change if saving fails. */
    private void addTask(String command, Ui ui) throws KiaException {
        Task newTask = Parser.createTask(command);
        tasks.add(newTask);
        try {
            storage.save(tasks);
        } catch (KiaException e) {
            tasks.remove(tasks.size() - 1);
            throw e;
        }
        ui.showTaskAdded(newTask, tasks.size());
    }

}
