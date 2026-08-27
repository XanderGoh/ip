import java.util.ArrayList;

/** A command that says goodbye and terminates Kia's command loop. */
public class ExitCommand extends Command {
    @Override
    public void execute(ArrayList<Task> tasks, Ui ui) {
        ui.showBye();
    }

    @Override
    public boolean isExit() {
        return true;
    }
}
