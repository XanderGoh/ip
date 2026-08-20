/**
 * Completion states for tasks.
 */
public enum TaskStatus {
    PENDING(" "),
    DONE("X");

    private final String icon;

    TaskStatus(String icon) {
        this.icon = icon;
    }

    /**
     * Returns the display icon for this status.
     *
     * @return the status icon
     */
    public String getIcon() {
        return icon;
    }
}
