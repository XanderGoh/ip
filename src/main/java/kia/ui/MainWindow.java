package kia.ui;

import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import kia.Kia;

/**
 * FXML controller for Kia's JavaFX chatbot window.
 */
public class MainWindow {
    @FXML
    private ScrollPane scrollPane;

    @FXML
    private VBox dialogContainer;

    @FXML
    private TextField userInput;

    private Kia kia;

    /** Keeps the latest conversation visible after new messages are added. */
    @FXML
    public void initialize() {
        scrollPane.vvalueProperty().bind(dialogContainer.heightProperty());
        dialogContainer.getChildren().add(DialogBox.getKiaDialog(
                "Heyo! I'm Kia.\nWhat can I do for you?"));
    }

    /**
     * Injects the chatbot logic used to process GUI commands.
     *
     * @param kia chatbot service for this window
     */
    public void setKia(Kia kia) {
        this.kia = kia;
    }

    /**
     * Displays the user's command and Kia's response, then clears the input.
     */
    @FXML
    private void handleUserInput() {
        String input = userInput.getText();
        if (input == null || input.isBlank()) {
            return;
        }
        dialogContainer.getChildren().add(DialogBox.getUserDialog(input));
        String response = kia.getResponse(input);
        dialogContainer.getChildren().add(DialogBox.getKiaDialog(response));
        userInput.clear();
    }
}
