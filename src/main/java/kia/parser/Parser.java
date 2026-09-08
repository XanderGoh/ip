package kia.parser;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import kia.command.CommandType;
import kia.exception.KiaException;
import kia.task.Deadline;
import kia.task.Event;
import kia.task.Task;
import kia.task.Todo;

/** Converts raw user commands into command categories, arguments, and tasks. */
public final class Parser {
    private static final String BYE_COMMAND = "bye";
    private static final String LIST_COMMAND = "list";
    private static final String DELETE_COMMAND = "delete";
    private static final String DELETE_PREFIX = DELETE_COMMAND + " ";
    private static final String FIND_COMMAND = "find";
    private static final String FIND_PREFIX = FIND_COMMAND + " ";
    private static final String MARK_COMMAND = "mark";
    private static final String MARK_PREFIX = MARK_COMMAND + " ";
    private static final String UNMARK_COMMAND = "unmark";
    private static final String UNMARK_PREFIX = UNMARK_COMMAND + " ";
    private static final String TODO_COMMAND = "todo";
    private static final String TODO_PREFIX = TODO_COMMAND + " ";
    private static final String DEADLINE_COMMAND = "deadline";
    private static final String DEADLINE_PREFIX = DEADLINE_COMMAND + " ";
    private static final String EVENT_COMMAND = "event";
    private static final String EVENT_PREFIX = EVENT_COMMAND + " ";
    private static final String BY_MARKER = " /by ";
    private static final String FROM_MARKER = " /from ";
    private static final String TO_MARKER = " /to ";

    private Parser() {
    }

    /**
     * Classifies a command without validating its arguments.
     *
     * @param command the command entered by the user
     * @return the command category
     */
    public static CommandType classifyCommand(String command) {
        if (command.equals(BYE_COMMAND)) {
            return CommandType.BYE;
        } else if (command.equals(LIST_COMMAND)) {
            return CommandType.LIST;
        } else if (command.equals(DELETE_COMMAND) || command.startsWith(DELETE_PREFIX)) {
            return CommandType.DELETE;
        } else if (command.equals(FIND_COMMAND) || command.startsWith(FIND_PREFIX)) {
            return CommandType.FIND;
        } else if (command.equals(MARK_COMMAND) || command.startsWith(MARK_PREFIX)) {
            return CommandType.MARK;
        } else if (command.equals(UNMARK_COMMAND) || command.startsWith(UNMARK_PREFIX)) {
            return CommandType.UNMARK;
        } else if (command.equals(TODO_COMMAND) || command.startsWith(TODO_PREFIX)) {
            return CommandType.TODO;
        } else if (command.equals(DEADLINE_COMMAND) || command.startsWith(DEADLINE_PREFIX)) {
            return CommandType.DEADLINE;
        } else if (command.equals(EVENT_COMMAND) || command.startsWith(EVENT_PREFIX)) {
            return CommandType.EVENT;
        }
        return CommandType.UNKNOWN;
    }

    /**
     * Parses a task command into the appropriate task subtype.
     *
     * @param command the complete task command
     * @return a new task
     * @throws KiaException if the command is malformed
     */
    public static Task createTask(String command) throws KiaException {
        if (command.equals(TODO_COMMAND) || command.startsWith(TODO_PREFIX)) {
            return createTodo(command);
        } else if (command.equals(DEADLINE_COMMAND) || command.startsWith(DEADLINE_PREFIX)) {
            return createDeadline(command);
        } else if (command.equals(EVENT_COMMAND) || command.startsWith(EVENT_PREFIX)) {
            return createEvent(command);
        }

        throw new KiaException("What did you do...");
    }

    /** Creates a to-do task from its command text. */
    private static Task createTodo(String command) throws KiaException {
        String description = extractArguments(command, TODO_PREFIX);
        if (description.isEmpty()) {
            throw new KiaException("The description of a todo cannot be empty.");
        }
        return new Todo(description);
    }

    /** Creates a deadline task from its command text. */
    private static Task createDeadline(String command) throws KiaException {
        String details = extractArguments(command, DEADLINE_PREFIX);
        int byIndex = details.indexOf(BY_MARKER);
        if (byIndex < 0) {
            throw new KiaException("A deadline must include a /by date or time.");
        }
        String description = details.substring(0, byIndex).trim();
        String by = details.substring(byIndex + BY_MARKER.length()).trim();
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

    /** Creates an event task from its command text. */
    private static Task createEvent(String command) throws KiaException {
        String details = extractArguments(command, EVENT_PREFIX);
        int fromIndex = details.indexOf(FROM_MARKER);
        int toIndex = fromIndex < 0 ? -1 : details.indexOf(TO_MARKER, fromIndex + FROM_MARKER.length());
        if (fromIndex < 0 || toIndex < 0) {
            throw new KiaException("An event must include /from and /to date or time values.");
        }
        String description = details.substring(0, fromIndex).trim();
        String from = details.substring(fromIndex + FROM_MARKER.length(), toIndex).trim();
        String to = details.substring(toIndex + TO_MARKER.length()).trim();
        if (description.isEmpty()) {
            throw new KiaException("The description of an event cannot be empty.");
        }
        if (from.isEmpty() || to.isEmpty()) {
            throw new KiaException("The /from and /to values of an event cannot be empty.");
        }
        return new Event(description, from, to);
    }

    /** Extracts the text after a command prefix, or an empty string for no arguments. */
    private static String extractArguments(String command, String prefix) {
        return command.length() <= prefix.length() ? "" : command.substring(prefix.length()).trim();
    }

    /**
     * Extracts and validates the keyword from a find command.
     *
     * @param command the complete find command
     * @return the keyword to search for
     * @throws KiaException if no keyword was supplied
     */
    public static String parseFindKeyword(String command) throws KiaException {
        String keyword = command.length() <= FIND_PREFIX.length()
                ? "" : command.substring(FIND_PREFIX.length()).trim();
        if (keyword.isEmpty()) {
            throw new KiaException("A find command must include a keyword.");
        }
        return keyword;
    }

    /**
     * Parses and validates the task number in a mark, unmark, or delete command.
     *
     * @param command the command entered by the user
     * @param prefix the command prefix to remove
     * @param taskCount the number of stored tasks
     * @return the one-based task number
     * @throws KiaException if the number is missing, malformed, or out of range
     */
    public static int parseTaskNumber(String command, String prefix, int taskCount) throws KiaException {
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
