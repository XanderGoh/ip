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
    private Parser() {
    }

    /**
     * Classifies a command without validating its arguments.
     *
     * @param command the command entered by the user
     * @return the command category
     */
    public static CommandType classifyCommand(String command) {
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
     * @param command the complete task command
     * @return a new task
     * @throws KiaException if the command is malformed
     */
    public static Task createTask(String command) throws KiaException {
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
    public static String parseFindKeyword(String command) throws KiaException {
        String keyword = command.length() <= "find ".length()
                ? "" : command.substring("find ".length()).trim();
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
