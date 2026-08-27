package kia.task;

import java.util.Locale;

/**
 * Represents a task and whether it has been completed.
 */
public class Task {
    protected String description;
    protected TaskType type;
    protected TaskStatus status;

    /**
     * Creates an incomplete task with the supplied description.
     *
     * @param description the text describing the task
     */
    public Task(String description) {
        this(description, TaskType.TODO);
    }

    /**
     * Creates an incomplete task with the supplied description and type.
     *
     * @param description the text describing the task
     * @param type the type of task
     */
    protected Task(String description, TaskType type) {
        this.description = description;
        this.type = type;
        this.status = TaskStatus.PENDING;
    }

    /**
     * Returns the marker used to display this task's completion state.
     *
     * @return {@code X} when complete, otherwise a space
     */
    public String getStatusIcon() {
        return status.getIcon();
    }

    /**
     * Returns the marker used to display this task's type.
     *
     * @return {@code T}, {@code D}, or {@code E}
     */
    public String getTypeIcon() {
        return type.getIcon();
    }

    /**
     * Checks whether this task description contains a keyword.
     *
     * <p>The comparison ignores letter case and treats the keyword as a
     * substring, allowing users to find partial words as well as complete
     * words.</p>
     *
     * @param keyword the text to search for
     * @return {@code true} when the keyword occurs in the description
     */
    public boolean matchesDescription(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return false;
        }
        return description.toLowerCase(Locale.ROOT)
                .contains(keyword.trim().toLowerCase(Locale.ROOT));
    }

    /**
     * Marks this task as complete.
     */
    public void markAsDone() {
        status = TaskStatus.DONE;
    }

    /**
     * Marks this task as incomplete again.
     */
    public void markAsUndone() {
        status = TaskStatus.PENDING;
    }

    /**
     * Returns the current completion state.
     *
     * @return the task status
     */
    public TaskStatus getStatus() {
        return status;
    }

    /**
     * Restores the task to a supplied completion state.
     *
     * <p>This is used by the application when a persistence operation fails
     * and an in-memory change must be rolled back.</p>
     *
     * @param status the status to restore
     */
    public void setStatus(TaskStatus status) {
        this.status = status;
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

    /**
     * Serialises this task using the compact on-disk format used by Kia.
     *
     * @return a pipe-delimited task record
     */
    public String toStorageString() {
        String done = status == TaskStatus.DONE ? "1" : "0";
        return type.getIcon() + " | " + done + " | " + description;
    }
}
