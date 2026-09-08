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
        if (commandType == CommandType.BYE) {
            Command exitCommand = new ExitCommand();
            exitCommand.execute(tasks, ui);
            return exitCommand.isExit();
        } else if (commandType == CommandType.LIST) {
            ui.showTaskList(tasks);
        } else if (commandType == CommandType.FIND) {
            String keyword = Parser.parseFindKeyword(command);
            ui.showMatchingTasks(tasks, keyword);
        } else if (commandType == CommandType.DELETE) {
            int taskNumber = Parser.parseTaskNumber(command, "delete ", tasks.size());
            Task removedTask = tasks.remove(taskNumber - 1);
            try {
                storage.save(tasks);
            } catch (KiaException e) {
                tasks.add(taskNumber - 1, removedTask);
                throw e;
            }
            ui.showTaskDeleted(removedTask, tasks.size());
        } else if (commandType == CommandType.MARK) {
            int taskNumber = Parser.parseTaskNumber(command, "mark ", tasks.size());
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
            int taskNumber = Parser.parseTaskNumber(command, "unmark ", tasks.size());
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
        return false;
    }

}
