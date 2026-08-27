package kia.ui;

import java.util.ArrayList;
import java.util.Scanner;

import kia.exception.KiaException;
import kia.task.Task;

/**
 * Handles all console input and output for Kia.
 *
 * <p>Keeping presentation here allows the command-processing code to focus on
 * application behavior instead of formatting messages for the user.</p>
 */
public class Ui {
    private static final String SEPARATOR = "_".repeat(60);

    private final Scanner scanner;

    /** Creates a UI that reads commands from standard input. */
    public Ui() {
        scanner = new Scanner(System.in);
    }

    /**
     * Returns whether another command is available from standard input.
     *
     * @return {@code true} if another line can be read
     */
    public boolean hasNextCommand() {
        return scanner.hasNextLine();
    }

    /**
     * Reads the next command from standard input.
     *
     * @return the next user-entered command
     */
    public String readCommand() {
        return scanner.nextLine();
    }

    /** Prints the initial banner and welcome message. */
    public void showWelcome() {
        String banner = "██╗  ██╗██╗ █████╗\n"
                + "██║ ██╔╝██║██╔══██╗\n"
                + "█████╔╝ ██║███████║\n"
                + "██╔═██╗ ██║██╔══██║\n"
                + "██║  ██╗██║██║  ██║\n"
                + "╚═╝  ╚═╝╚═╝╚═╝  ╚═╝";
        showSeparator();
        System.out.println(banner);
        System.out.println("Heyo! I'm Kia.");
        System.out.println("What can I do for you?");
        showSeparator();
    }

    /** Prints the separator used between chatbot messages. */
    public void showSeparator() {
        System.out.println(SEPARATOR);
    }

    /** Prints the farewell message. */
    public void showBye() {
        System.out.println("Aww, goodbye. Hope to see you again soon!");
        showSeparator();
    }

    /**
     * Prints an error returned while processing a command.
     *
     * @param exception the command error to display
     */
    public void showError(KiaException exception) {
        System.out.println("Hey!!! " + exception.getMessage());
    }

    /**
     * Prints the loading error returned during startup.
     *
     * @param exception the loading error to display
     */
    public void showLoadingError(KiaException exception) {
        showError(exception);
    }

    /**
     * Prints all tasks with their one-based list positions.
     *
     * @param tasks the tasks to display
     */
    public void showTaskList(ArrayList<Task> tasks) {
        System.out.println("Here ya go! These are the tasks in your list:");
        for (int i = 0; i < tasks.size(); i++) {
            System.out.println((i + 1) + "." + tasks.get(i));
        }
    }

    /**
     * Prints confirmation for a newly added task.
     *
     * @param task the newly added task
     * @param taskCount the number of tasks after the addition
     */
    public void showTaskAdded(Task task, int taskCount) {
        System.out.println("Gotcha! I've added this task:");
        System.out.println("  " + task);
        showTaskCount(taskCount);
    }

    /**
     * Prints confirmation for a deleted task.
     *
     * @param task the deleted task
     * @param taskCount the number of tasks after the deletion
     */
    public void showTaskDeleted(Task task, int taskCount) {
        System.out.println("Okies. I've removed this task:");
        System.out.println("  " + task);
        showTaskCount(taskCount);
    }

    /**
     * Prints confirmation that a task was marked done.
     *
     * @param task the task that was marked done
     */
    public void showTaskMarked(Task task) {
        System.out.println("Yay! I've marked this task as done:");
        System.out.println("  " + task);
    }

    /**
     * Prints confirmation that a task was marked not done.
     *
     * @param task the task that was marked not done
     */
    public void showTaskUnmarked(Task task) {
        System.out.println("Golly! I've marked this task as not done yet:");
        System.out.println("  " + task);
    }

    /**
     * Prints the task-count confirmation using the singular or plural form.
     *
     * @param taskCount the number of tasks currently stored
     */
    private void showTaskCount(int taskCount) {
        String taskWord = taskCount == 1 ? "task" : "tasks";
        System.out.println("Alright, now you have " + taskCount + " " + taskWord + " in the list.");
    }
}
