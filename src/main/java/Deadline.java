import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * A task that must be completed by a specified date or time.
 */
public class Deadline extends Task {
    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("MMM dd yyyy", Locale.ENGLISH);

    protected LocalDate by;

    /**
     * Creates an incomplete deadline task.
     *
     * @param description the task description
     * @param by the date by which the task should be completed
     */
    public Deadline(String description, LocalDate by) {
        super(description, TaskType.DEADLINE);
        this.by = by;
    }

    /**
     * Creates a deadline from an ISO-8601 date, retained for callers that use
     * the original string-based constructor.
     *
     * @param description the task description
     * @param by ISO date in {@code yyyy-MM-dd} format
     */
    public Deadline(String description, String by) {
        this(description, LocalDate.parse(by));
    }

    @Override
    public String toString() {
        return "[" + getTypeIcon() + "]" + super.toString()
                + " (by: " + by.format(DISPLAY_FORMAT) + ")";
    }

    @Override
    public String toStorageString() {
        return super.toStorageString() + " | " + by;
    }
}
