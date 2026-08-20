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
            } else if (command.startsWith("unmark ")){
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
                tasks[taskCount] = new Task(command);
                taskCount++;
                System.out.println("added: " + command);
            }

            System.out.println(separator);
        }
    }
}
