/**
 * A task with a start date or time and an end date or time.
 */
public class Event extends Task {
    protected String from;
    protected String to;

    /**
     * Creates an incomplete event task.
     *
     * @param description the event description
     * @param from the event start date or time
     * @param to the event end date or time
     */
    public Event(String description, String from, String to) {
        super(description, TaskType.EVENT);
        this.from = from;
        this.to = to;
    }

    @Override
    public String toString() {
        return "[" + getTypeIcon() + "]" + super.toString() + " (from: " + from + " to: " + to + ")";
    }
}
