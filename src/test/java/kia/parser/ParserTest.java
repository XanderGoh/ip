package kia.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import kia.command.CommandType;
import kia.exception.KiaException;
import kia.task.Task;

/** Unit tests for command classification and argument parsing. */
class ParserTest {
    /** Verifies that supported command prefixes map to their command categories. */
    @Test
    void classifyCommand_supportedCommands_returnsExpectedTypes() {
        assertEquals(CommandType.TODO, Parser.classifyCommand("todo buy milk"));
        assertEquals(CommandType.DEADLINE, Parser.classifyCommand("deadline report /by 2019-10-15"));
        assertEquals(CommandType.EVENT, Parser.classifyCommand("event meeting /from 2pm /to 4pm"));
        assertEquals(CommandType.LIST, Parser.classifyCommand("list"));
        assertEquals(CommandType.MARK, Parser.classifyCommand("mark 1"));
        assertEquals(CommandType.UNMARK, Parser.classifyCommand("unmark 1"));
        assertEquals(CommandType.DELETE, Parser.classifyCommand("delete 1"));
        assertEquals(CommandType.FIND, Parser.classifyCommand("find book"));
        assertEquals(CommandType.BYE, Parser.classifyCommand("bye"));
        assertEquals(CommandType.UNKNOWN, Parser.classifyCommand("unknown"));
    }

    /** Verifies that commands without arguments still retain their command categories. */
    @Test
    void classifyCommand_argumentlessCommands_returnsExpectedTypes() {
        assertEquals(CommandType.TODO, Parser.classifyCommand("todo"));
        assertEquals(CommandType.DEADLINE, Parser.classifyCommand("deadline"));
        assertEquals(CommandType.EVENT, Parser.classifyCommand("event"));
        assertEquals(CommandType.FIND, Parser.classifyCommand("find"));
        assertEquals(CommandType.MARK, Parser.classifyCommand("mark"));
        assertEquals(CommandType.UNMARK, Parser.classifyCommand("unmark"));
        assertEquals(CommandType.DELETE, Parser.classifyCommand("delete"));
    }

    /** Verifies that task commands produce the correct subtype and values. */
    @Test
    void createTask_supportedTaskCommands_buildsExpectedTasks() throws Exception {
        Task deadline = Parser.createTask("deadline return book /by 2019-12-02");
        Task event = Parser.createTask("event project meeting /from 2pm /to 4pm");
        Task todo = Parser.createTask("todo borrow book");

        assertEquals("[D][ ] return book (by: Dec 02 2019)", deadline.toString());
        assertEquals("[E][ ] project meeting (from: 2pm to: 4pm)", event.toString());
        assertEquals("[T][ ] borrow book", todo.toString());
    }

    /** Verifies that malformed task commands retain their user-facing errors. */
    @Test
    void createTask_missingTodoDescription_throwsKiaException() {
        KiaException exception = assertThrows(KiaException.class, () -> Parser.createTask("todo"));

        assertEquals("The description of a todo cannot be empty. >:[", exception.getMessage());
    }

    /** Verifies that find and numbered commands reject missing or invalid arguments. */
    @Test
    void parseArguments_invalidValues_throwsKiaException() {
        assertThrows(KiaException.class, () -> Parser.parseFindKeyword("find"));
        assertThrows(KiaException.class, () -> Parser.parseTaskNumber("mark nope", "mark ", 1));
        assertThrows(KiaException.class, () -> Parser.parseTaskNumber("delete 2", "delete ", 1));
    }
}
