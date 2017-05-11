package ru.spbau.sazanovich.nikita;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

/**
 * Class which stores game scene and its inner state.
 */
class FindPairSceneContainer {

    private static final double INITIAL_SIZE = 400.0;

    @NotNull
    private final FindPairController controller;

    @NotNull
    private final Scene scene;

    /**
     * Constructs a container with given field size and a stage to attach the scene to.
     *
     * @param fieldSize size of the field
     * @param stage a stage to attach the scene to
     */
    FindPairSceneContainer(int fieldSize, @NotNull Stage stage) {
        this.controller = new FindPairController(fieldSize, this);
        this.scene = build();
    }

    /**
     * Gets built scene.
     *
     * @return the assembled scene
     */
    @NotNull
    Scene getScene() {
        return scene;
    }

    void showWinAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("You've won!");
        alert.setContentText("Congratulations.");
        alert.show();
    }

    @NotNull
    private Scene build() {
        int fieldSize = controller.getFieldSize();
        HBox[] rows = new HBox[fieldSize];
        for (int i = 0; i < fieldSize; i++) {
            rows[i] = buildRowFor(i, fieldSize);
        }
        return new Scene(buildVerticalLayoutForRows(rows), INITIAL_SIZE, INITIAL_SIZE);
    }

    @NotNull
    private VBox buildVerticalLayoutForRows(@NotNull HBox[] rows) {
        VBox field = new VBox();
        for (HBox row : rows) {
            VBox.setVgrow(row, Priority.ALWAYS);
            field.getChildren().add(row);
        }
        field.setPadding(new Insets(10, 10, 10,10));
        return field;
    }

    @NotNull
    private HBox buildRowFor(int rowX, int size) {
        HBox row = new HBox();
        for (int j = 0; j < size; j++) {
            Button button = controller.getButtonFor(rowX, j);
            HBox.setHgrow(button, Priority.ALWAYS);
            button.prefWidthProperty().bind(row.widthProperty().divide(size));
            row.getChildren().add(button);
        }
        return row;
    }
}
