package kia.task;

/**
 * A task without an associated date or time.
 */
public class Todo extends Task {
    /**
     * Creates an incomplete to-do task.
     *
     * @param description the task description
     */
    public Todo(String description) {
        super(description, TaskType.TODO);
    }

    @Override
    public String toString() {
        return "[" + getTypeIcon() + "]" + super.toString();
    }

    @Override
    public String toStorageString() {
        return super.toStorageString();
    }
}
