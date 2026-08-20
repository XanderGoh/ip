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
        System.out.println("Hello! I'm Kia.");
        System.out.println("What can I do for you?");
        System.out.println(separator);

        Task[] tasks = new Task[100];
        int taskCount = 0;
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine();
            System.out.println(separator);

            if (command.equals("bye")) {
                System.out.println("Bye. Hope to see you again soon!");
                System.out.println(separator);
                break;
            }

            if (command.equals("list")) {
                System.out.println("Here are the tasks in your list:");
                for (int i = 0; i < taskCount; i++) {
                    System.out.println((i + 1) + "." + tasks[i]);
                }
            } else if (command.startsWith("mark ")) {
                String taskNumberText = command.substring("mark ".length()).trim();
                try {
                    int taskNumber = Integer.parseInt(taskNumberText);
                    if (taskNumber >= 1 && taskNumber <= taskCount) {
                        Task task = tasks[taskNumber - 1];
                        task.markAsDone();
                        System.out.println("Nice! I've marked this task as done:");
                        System.out.println("  " + task);
                    } else {
                        System.out.println("Invalid task number.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid task number.");
                }
            } else if (command.startsWith("unmark ")) {
                String taskNumberText = command.substring("unmark ".length()).trim();
                try {
                    int taskNumber = Integer.parseInt(taskNumberText);
                    if (taskNumber >= 1 && taskNumber <= taskCount) {
                        Task task = tasks[taskNumber - 1];
                        task.markAsUndone();
                        System.out.println("Nice! I've marked this task as not done yet:");
                        System.out.println("  " + task);
                    } else {
                        System.out.println("Invalid task number.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid task number.");
                }
            } else {
                Task newTask = createTask(command);
                if (newTask == null) {
                    System.out.println("I don't understand that command.");
                } else if (taskCount >= tasks.length) {
                    System.out.println("Your task list is full.");
                } else {
                    tasks[taskCount] = newTask;
                    taskCount++;
                    System.out.println("Got it. I've added this task:");
                    System.out.println("  " + newTask);
                    String taskWord = taskCount == 1 ? "task" : "tasks";
                    System.out.println("Now you have " + taskCount + " " + taskWord + " in the list.");
                }
            }

            System.out.println(separator);
        }
    }

    /**
     * Parses a task command into the appropriate task subtype.
     *
     * @param command the command entered by the user
     * @return a new task, or {@code null} when the command is malformed or unknown
     */
    private static Task createTask(String command) {
        if (command.startsWith("todo ")) {
            String description = command.substring("todo ".length()).trim();
            return description.isEmpty() ? null : new Todo(description);
        }

        if (command.startsWith("deadline ")) {
            String details = command.substring("deadline ".length()).trim();
            int byIndex = details.indexOf(" /by ");
            if (byIndex < 0) {
                return null;
            }
            String description = details.substring(0, byIndex).trim();
            String by = details.substring(byIndex + " /by ".length()).trim();
            return description.isEmpty() || by.isEmpty() ? null : new Deadline(description, by);
        }

        if (command.startsWith("event ")) {
            String details = command.substring("event ".length()).trim();
            int fromIndex = details.indexOf(" /from ");
            int toIndex = fromIndex < 0 ? -1 : details.indexOf(" /to ", fromIndex + " /from ".length());
            if (fromIndex < 0 || toIndex < 0) {
                return null;
            }
            String description = details.substring(0, fromIndex).trim();
            String from = details.substring(fromIndex + " /from ".length(), toIndex).trim();
            String to = details.substring(toIndex + " /to ".length()).trim();
            return description.isEmpty() || from.isEmpty() || to.isEmpty()
                    ? null : new Event(description, from, to);
        }

        return null;
    }
}
