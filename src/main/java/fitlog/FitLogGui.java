package fitlog;

import java.nio.file.Path;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Provides FitLog's JavaFX command-conversation interface.
 */
public class FitLogGui extends Application {
    private final TextField commandInput = new TextField();
    private final Button sendButton = new Button("Send");
    private GuiUi ui;
    private FitLogController controller;
    private Stage stage;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        VBox conversation = new VBox(10);
        conversation.getStyleClass().add("conversation");
        ScrollPane conversationScrollPane = new ScrollPane(conversation);
        conversationScrollPane.setFitToWidth(true);
        conversationScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        conversationScrollPane.getStyleClass().add("conversation-scroll");

        ui = new GuiUi(conversation, conversationScrollPane);
        controller = new FitLogController(ui, new Storage(Path.of("data", "fitlog.txt")));

        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-root");
        root.setTop(createHeader());
        root.setCenter(createMainContent(conversationScrollPane));
        root.setBottom(createComposer());

        Scene scene = new Scene(root, 1060, 720);
        scene.getStylesheets().add(getClass().getResource("/fitlog/fitlog.css").toExternalForm());
        stage.setTitle("FitLog");
        stage.setMinWidth(820);
        stage.setMinHeight(580);
        stage.setScene(scene);
        stage.show();

        controller.start();
        commandInput.requestFocus();
    }

    /**
     * Creates the application header and status text.
     *
     * @return the configured header
     */
    private VBox createHeader() {
        Label title = new Label("FitLog");
        title.getStyleClass().add("app-title");
        Label subtitle = new Label("Your training command centre");
        subtitle.getStyleClass().add("app-subtitle");

        VBox text = new VBox(2, title, subtitle);
        HBox header = new HBox(text);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("header");
        return new VBox(header);
    }

    /**
     * Creates the full-width conversation area.
     *
     * @param conversationScrollPane the scrollable conversation view
     * @return the configured main content area
     */
    private VBox createMainContent(ScrollPane conversationScrollPane) {
        VBox conversationArea = new VBox(conversationScrollPane);
        conversationArea.getStyleClass().add("conversation-area");
        VBox.setVgrow(conversationScrollPane, Priority.ALWAYS);
        conversationArea.setPadding(new Insets(18));
        conversationArea.getStyleClass().add("main-content");
        return conversationArea;
    }

    /**
     * Creates the command input field and send control.
     *
     * @return the configured composer
     */
    private HBox createComposer() {
        commandInput.setPromptText("Try: log strength bench press /sets 3 /reps 10 /weight 80");
        commandInput.setOnAction(event -> submitInput());
        commandInput.getStyleClass().add("command-input");

        sendButton.setOnAction(event -> submitInput());
        sendButton.disableProperty().bind(Bindings.createBooleanBinding(
                () -> commandInput.getText().trim().isEmpty(), commandInput.textProperty()));
        sendButton.getStyleClass().add("send-button");

        HBox composer = new HBox(10, commandInput, sendButton);
        composer.setAlignment(Pos.CENTER);
        composer.setPadding(new Insets(16, 18, 20, 18));
        HBox.setHgrow(commandInput, Priority.ALWAYS);
        composer.getStyleClass().add("composer");
        return composer;
    }

    /**
     * Displays and submits the entered command, disabling interactions after exit.
     */
    private void submitInput() {
        String input = commandInput.getText().trim();
        if (input.isEmpty()) {
            return;
        }
        ui.showUserCommand(input);
        commandInput.clear();
        if (controller.submit(input)) {
            endSession();
        }
    }

    /**
     * Disables interaction and closes the window after the farewell message can be read.
     */
    private void endSession() {
        commandInput.setDisable(true);

        PauseTransition farewellDelay = new PauseTransition(Duration.seconds(1.2));
        farewellDelay.setOnFinished(event -> stage.close());
        farewellDelay.play();
    }
}
