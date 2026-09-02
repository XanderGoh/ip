package kia.ui;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 * A chat message displaying the speaker and message text.
 */
public class DialogBox extends HBox {
    @FXML
    private Label speaker;

    @FXML
    private Label message;

    /**
     * Creates a styled chat message.
     *
     * @param speakerName label identifying the speaker
     * @param text message to display
     */
    private DialogBox(String speakerName, String text) {
        FXMLLoader loader = new FXMLLoader(DialogBox.class.getResource("/view/DialogBox.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load the dialog layout.", e);
        }
        speaker.setText(speakerName);
        message.setText(text);
    }

    /**
     * Creates a right-aligned message from the user.
     *
     * @param text user-entered text
     * @return a user dialog box
     */
    public static DialogBox getUserDialog(String text) {
        DialogBox dialogBox = new DialogBox("You", text);
        dialogBox.setAlignment(Pos.TOP_RIGHT);
        return dialogBox;
    }

    /**
     * Creates a left-aligned message from Kia.
     *
     * @param text Kia's response
     * @return a Kia dialog box
     */
    public static DialogBox getKiaDialog(String text) {
        return new DialogBox("Kia", text);
    }
}
