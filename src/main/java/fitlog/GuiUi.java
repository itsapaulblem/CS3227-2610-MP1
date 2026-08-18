package fitlog;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Renders categorised FitLog feedback as styled messages in a conversation view.
 */
public class GuiUi implements Ui {
    private static final double MAXIMUM_MESSAGE_WIDTH = 620;
    private final VBox conversation;
    private final ScrollPane conversationScrollPane;

    /**
     * Creates a GUI feedback adapter for the specified conversation view.
     *
     * @param conversation the container that receives message rows
     * @param conversationScrollPane the scroll pane that displays the conversation
     */
    public GuiUi(VBox conversation, ScrollPane conversationScrollPane) {
        this.conversation = conversation;
        this.conversationScrollPane = conversationScrollPane;
    }

    /**
     * Adds a user-entered command as a right-aligned conversation message.
     *
     * @param command the submitted command
     */
    public void showUserCommand(String command) {
        appendMessage(command, "user-message", Pos.CENTER_RIGHT);
    }

    @Override
    public void showInfo(String message) {
        appendMessage(message, "info-message", Pos.CENTER_LEFT);
    }

    @Override
    public void showExample(String message) {
        appendMessage(message, "example-message", Pos.CENTER_LEFT);
    }

    @Override
    public void showSuccess(String message) {
        appendMessage(message, "success-message", Pos.CENTER_LEFT);
    }

    @Override
    public void showError(String message) {
        appendMessage(message, "error-message", Pos.CENTER_LEFT);
    }

    @Override
    public void showWarning(String message) {
        appendMessage(message, "warning-message", Pos.CENTER_LEFT);
    }

    @Override
    public void showPersonalRecord(String message) {
        appendMessage(message, "pr-message", Pos.CENTER_LEFT);
    }

    /**
     * Builds, adds, and scrolls to one conversation message.
     *
     * @param message the text to show
     * @param styleClass the CSS class describing the message category
     * @param alignment the message-row alignment
     */
    private void appendMessage(String message, String styleClass, Pos alignment) {
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(MAXIMUM_MESSAGE_WIDTH);
        messageLabel.getStyleClass().add("message-bubble");
        messageLabel.getStyleClass().add(styleClass);

        HBox row = new HBox(messageLabel);
        row.setAlignment(alignment);
        row.getStyleClass().add("message-row");
        conversation.getChildren().add(row);
        Platform.runLater(() -> conversationScrollPane.setVvalue(1.0));
    }
}
