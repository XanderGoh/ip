package kia.task;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for task completion state transitions.
 */
class TaskTest {
    @Test
    void getStatusIcon_newTask_returnsPendingIcon() {
        Task task = new Task("read book");

        assertEquals(TaskStatus.PENDING, task.getStatus());
        assertEquals(" ", task.getStatusIcon());
    }

    @Test
    void getTypeIcon_todoTask_returnsTodoIcon() {
        Task task = new Task("read book");

        assertEquals("T", task.getTypeIcon());
    }

    @Test
    void markAsDone_pendingTask_marksItDone() {
        Task task = new Task("read book");

        task.markAsDone();

        assertEquals(TaskStatus.DONE, task.getStatus());
        assertEquals("X", task.getStatusIcon());
    }

    @Test
    void markAsDone_doneTask_remainsDone() {
        Task task = new Task("read book");
        task.markAsDone();

        task.markAsDone();

        assertEquals(TaskStatus.DONE, task.getStatus());
        assertEquals("X", task.getStatusIcon());
    }

    @Test
    void markAsUndone_doneTask_marksItPending() {
        Task task = new Task("read book");
        task.markAsDone();

        task.markAsUndone();

        assertEquals(TaskStatus.PENDING, task.getStatus());
        assertEquals(" ", task.getStatusIcon());
    }

    @Test
    void markAsUndone_pendingTask_remainsPending() {
        Task task = new Task("read book");

        task.markAsUndone();

        assertEquals(TaskStatus.PENDING, task.getStatus());
        assertEquals(" ", task.getStatusIcon());
    }

    @Test
    void setStatus_doneStatus_updatesTaskState() {
        Task task = new Task("read book");

        task.setStatus(TaskStatus.DONE);

        assertEquals(TaskStatus.DONE, task.getStatus());
        assertEquals("X", task.getStatusIcon());
    }

    @Test
    void toString_pendingTask_includesPendingMarkerAndDescription() {
        Task task = new Task("read book");

        assertEquals("[ ] read book", task.toString());
    }

    @Test
    void toString_doneTask_includesDoneMarkerAndDescription() {
        Task task = new Task("read book");
        task.markAsDone();

        assertEquals("[X] read book", task.toString());
    }

    @Test
    void toStorageString_pendingTask_usesPendingStatusRecord() {
        Task task = new Task("read book");

        assertEquals("T | 0 | read book", task.toStorageString());
    }

    @Test
    void toStorageString_doneTask_usesDoneStatusRecord() {
        Task task = new Task("read book");
        task.markAsDone();

        assertEquals("T | 1 | read book", task.toStorageString());
    }
}
