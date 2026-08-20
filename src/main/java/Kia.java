import java.util.ArrayList;
import java.util.Scanner;

/**
 * Greets the user, manages in-memory tasks, and exits when the user enters {@code bye}.
 */
public class Kia {
    public static void main(String[] args) {
        String separator = "_".repeat(60);
        String banner = "██╗  ██╗██╗ █████╗\n"
                + "██║ ██╔╝██║██╔══██╗\n"
                + "█████╔╝ ██║███████║\n"
                + "██╔═██╗ ██║██╔══██║\n"
                + "██║  ██╗██║██║  ██║\n"
                + "╚═╝  ╚═╝╚═╝╚═╝  ╚═╝";

        System.out.println(separator);
        System.out.println(banner);
        System.out.println("Heyo! I'm Kia.");
        System.out.println("What can I do for you?");
        System.out.println(separator);

        ArrayList<Task> tasks = new ArrayList<>();
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine();
            System.out.println(separator);

            boolean shouldExit = false;
            try {
                CommandType commandType = classifyCommand(command);
                if (commandType == CommandType.BYE) {
                    System.out.println("Aww, goodbye. Hope to see you again soon!");
                    System.out.println(separator);
                    shouldExit = true;
                } else if (commandType == CommandType.LIST) {
                    System.out.println("Here ya go! These are the tasks in your list:");
                    for (int i = 0; i < tasks.size(); i++) {
                        System.out.println((i + 1) + "." + tasks.get(i));
                    }
                } else if (commandType == CommandType.DELETE) {
                    int taskNumber = parseTaskNumber(command, "delete ", tasks.size());
                    Task removedTask = tasks.remove(taskNumber - 1);
                    System.out.println("Okies. I've removed this task:");
                    System.out.println("  " + removedTask);
                    String taskWord = tasks.size() == 1 ? "task" : "tasks";
                    System.out.println("Alright, now you have " + tasks.size() + " " + taskWord + " in the list.");
                } else if (commandType == CommandType.MARK) {
                    int taskNumber = parseTaskNumber(command, "mark ", tasks.size());
                    Task task = tasks.get(taskNumber - 1);
                    task.markAsDone();
                    System.out.println("Yay! I've marked this task as done:");
                    System.out.println("  " + task);
                } else if (commandType == CommandType.UNMARK) {
                    int taskNumber = parseTaskNumber(command, "unmark ", tasks.size());
                    Task task = tasks.get(taskNumber - 1);
                    task.markAsUndone();
                    System.out.println("Golly! I've marked this task as not done yet:");
                    System.out.println("  " + task);
                } else {
                    Task newTask = createTask(command);
                    tasks.add(newTask);
                    System.out.println("Gotcha! I've added this task:");
                    System.out.println("  " + newTask);
                    String taskWord = tasks.size() == 1 ? "task" : "tasks";
                    System.out.println("Alright, now you have " + tasks.size() + " " + taskWord + " in the list.");
                }
            } catch (KiaException e) {
                System.out.println("Hey!!! " + e.getMessage());
            }

            if (shouldExit) {
                break;
            }
            System.out.println(separator);
        }
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
            return new Deadline(description, by);
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
