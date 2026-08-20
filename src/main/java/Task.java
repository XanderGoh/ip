/**
 * Represents a task and whether it has been completed.
 */
public class Task {
    protected String description;
    protected boolean isDone;

    /**
     * Creates an incomplete task with the supplied description.
     *
     * @param description the text describing the task
     */
    public Task(String description) {
        this.description = description;
        this.isDone = false;
    }

    /**
     * Returns the marker used to display this task's completion state.
     *
     * @return {@code X} when complete, otherwise a space
     */
    public String getStatusIcon() {
        return isDone ? "X" : " ";
    }

    /**
     * Marks this task as complete.
     */
    public void markAsDone() {
        isDone = true;
    }

    /**
     * Marks this task as incomplete again.
     */
    public void markAsUndone() {
        isDone = false;
    }

    /**
     * Formats the task for display in the task list.
     *
     * @return the status marker followed by the task description
     */
    @Override
    public String toString() {
        return "[" + getStatusIcon() + "] " + description;
    }
}
