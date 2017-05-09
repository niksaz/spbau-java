package ru.spbau.sazanovich.nikita.client.gui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import ru.spbau.sazanovich.nikita.client.Client;
import ru.spbau.sazanovich.nikita.client.ClientFactory;
import ru.spbau.sazanovich.nikita.server.commands.FileInfo;

import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import static javafx.scene.control.Alert.AlertType.ERROR;
import static javafx.scene.control.Alert.AlertType.INFORMATION;
import static javafx.scene.layout.Priority.ALWAYS;

/**
 * Class which allows to build scenes which provide access to client operations.
 */
class ClientSceneBuilder {

    private static final Font SMALL_FONT = new Font(10.0);

    @NotNull
    private final ClientFactory clientFactory;

    @NotNull
    private Path currentDirectory;

    ClientSceneBuilder(@NotNull ClientFactory clientFactory, @NotNull Path currentDirectory) {
        this.clientFactory = clientFactory;
        this.currentDirectory = currentDirectory;
    }

    @NotNull
    Scene build(@NotNull Stage stage) {
        final List<Node> nodes = new LinkedList<>();

        nodes.add(createInfoLabel("Remote path:"));
        TextField remotePathTextField = new TextField();
        nodes.add(remotePathTextField);

        Button listButton = new Button("List");
        nodes.add(listButton);

        Label lastDirectoryListedLabel = createInfoLabel("");
        nodes.add(lastDirectoryListedLabel);
        ListView<FileInfo> fileListView = new ListView<>();
        nodes.add(fileListView);

        Button downloadButton = new Button("Download");
        Button chooseDirButton = new Button("Choose directory");
        nodes.add(packTwoButtons(downloadButton, chooseDirButton));

        Label localPathLabel = createInfoLabel(constructLocalLabelText());
        nodes.add(localPathLabel);

        TextField localPathTextField = new TextField();
        nodes.add(localPathTextField);

        listButton.setOnAction(event ->
                performListOperationOf(remotePathTextField.getText(), lastDirectoryListedLabel, fileListView));
        fileListView.setOnMouseClicked(event -> {
            if (fileListView.getSelectionModel().getSelectedItem() == null) {
                return;
            }
            if (event.getClickCount() == 1) {
                FileInfo selectedItem = fileListView.getSelectionModel().getSelectedItem();
                if (!selectedItem.isDirectory()) {
                    localPathTextField.setText(selectedItem.getName());
                }
                remotePathTextField.setText(Paths.get(lastDirectoryListedLabel.getText(), selectedItem.getName()).toString());
                return;
            }
            if (event.getClickCount() == 2) {
                performListOperationOf(remotePathTextField.getText(), lastDirectoryListedLabel, fileListView);
            }
        });
        downloadButton.setOnAction(createDownloadEventHandler(remotePathTextField, localPathTextField));
        chooseDirButton.setOnAction(event -> chooseDirectoryTo(localPathLabel, stage));

        return new Scene(buildRootLayoutFor(nodes));
    }

    @NotNull
    private EventHandler<ActionEvent> createDownloadEventHandler(@NotNull TextField remotePathTextField,
                                                                 @NotNull TextField localPathTextField) {
        return event -> {
            Path completeLocalPath;
            try {
                completeLocalPath = Paths.get(currentDirectory.toString(), localPathTextField.getText());
            } catch (InvalidPathException e) {
                showAlert(ERROR, "Unsuccessful downloading", e.getMessage());
                return;
            }
            performGetOperationOf(remotePathTextField.getText(), completeLocalPath.toString());
        };
    }

    private String constructLocalLabelText() {
        return "Local path relative to directory " + currentDirectory + ":";
    }

    private void chooseDirectoryTo(@NotNull Labeled labeled, @NotNull Stage stage) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose a directory where to save file");
        File selected = chooser.showDialog(stage);
        if (selected != null) {
            currentDirectory = selected.toPath();
            labeled.setText(constructLocalLabelText());
        }
    }

    private Label createInfoLabel(@NotNull String text) {
        Label label = new Label(text);
        label.setFont(SMALL_FONT);
        return label;
    }

    private Pane packTwoButtons(@NotNull Button left, @NotNull Button right) {
        Pane spacer = new Pane();
        HBox.setHgrow(spacer, ALWAYS);
        return new HBox(left, spacer, right);
    }

    @NotNull
    private Parent buildRootLayoutFor(@NotNull List<Node> nodesList) {
        Node[] nodes = new Node[nodesList.size()];
        nodesList.toArray(nodes);

        VBox vbox = new VBox(nodes);
        vbox.setSpacing(10);
        vbox.setMinWidth(400);
        vbox.setPadding(new Insets(10, 10, 10,10));
        return vbox;
    }

    private void performGetOperationOf(@NotNull String remotePath, String localPath) {
        Client client = clientFactory.createClient();
        try {
            boolean successfulRequest = client.get(remotePath, localPath);
            if (successfulRequest) {
                showAlert(INFORMATION, "Success",
                          "Downloaded remote file " + remotePath + " to " + localPath);
            } else {
                showAlert(ERROR, "Unsuccessful downloading", "Error response from server");
            }
        } catch (Exception e) {
            showAlert(ERROR, "Unsuccessful downloading", e.getMessage());
        }
    }

    private void performListOperationOf(@NotNull String remotePath, @NotNull Label lastDirectoryListedLabel,
                                        @NotNull ListView<FileInfo> fileListView) {
        Client client = clientFactory.createClient();
        try {
            List<FileInfo> paths = client.list(remotePath);
            if (paths == null) {
                showAlert(ERROR, "Unsuccessful listing", "Error response from server");
                return;
            }
            lastDirectoryListedLabel.setText(remotePath);
            fileListView.getItems().clear();
            for (FileInfo fileInfo : paths) {
                fileListView.getItems().add(fileInfo);
            }
        } catch (Exception e) {
            showAlert(ERROR, "Unsuccessful listing", e.getMessage());
        }
    }

    private static void showAlert(@NotNull Alert.AlertType type, @NotNull String header, @NotNull String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.show();
    }
}
