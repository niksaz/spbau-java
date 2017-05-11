package ru.spbau.sazanovich.nikita;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Button;
import javafx.util.Duration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Class which controls the flow of the game through user input.
 */
class FindPairController {

    private static final long FREEZING_TIME_MS = 1000;

    private final int fieldSize;

    @NotNull
    private final FindPairSceneContainer sceneContainer;

    @NotNull
    private final int[][] field;

    @NotNull
    private final Button[][] buttons;

    @Nullable
    private Button lastButtonClicked;

    private int buttonsLeft;

    FindPairController(int fieldSize, @NotNull FindPairSceneContainer sceneContainer) {
        this.sceneContainer = sceneContainer;
        if (fieldSize <= 0 || fieldSize % 2 != 0) {
            throw new IllegalArgumentException("Field size should be a positive even number");
        }
        this.fieldSize = fieldSize;
        buttonsLeft = fieldSize * fieldSize;
        List<Integer> numbers = generateRandomNumbers(buttonsLeft);

        field = new int[fieldSize][];
        buttons = new Button[fieldSize][];
        for (int i = 0; i < fieldSize; i++) {
            field[i] = new int[fieldSize];
            buttons[i] = new Button[fieldSize];
            for (int j = 0; j < fieldSize; j++) {
                field[i][j] = numbers.get(i * fieldSize + j);
                buttons[i][j] = constructButton(i, j);
            }
        }
    }

    int getFieldSize() {
        return fieldSize;
    }

    @NotNull
    Button getButtonFor(int x, int y) {
        if (x < 0 || x >= fieldSize || y < 0 || y >= fieldSize) {
            throw new IllegalArgumentException("Asking for a button outside the field");
        }
        return buttons[x][y];
    }

    @NotNull
    private List<Integer> generateRandomNumbers(int size) {
        if (size <= 0 || size % 2 != 0) {
            throw new IllegalArgumentException("Size for generating numbers should be a positive even number");
        }
        final Random generator = new Random();
        List<Integer> numbers = new ArrayList<>();
        while (size > 0) {
            int number = generator.nextInt(2);
            numbers.add(number);
            numbers.add(number);
            size -= 2;
        }
        Collections.shuffle(numbers);
        return numbers;
    }

    @NotNull
    private Button constructButton(int x, int y) {
        Button button = new Button();
        button.setFocusTraversable(false);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setMaxHeight(Double.MAX_VALUE);
        button.setOnMouseClicked(event -> {
            button.setDisable(true);
            button.setText(String.valueOf(field[x][y]));
            if (lastButtonClicked == null) {
                lastButtonClicked = button;
            } else {
                if (lastButtonClicked.getText().equals(button.getText())) {
                    updateWinCondition();
                } else {
                    final Button button1 = lastButtonClicked;
                    final Button button2 = button;
                    Timeline timeline = new Timeline(new KeyFrame(
                            Duration.millis(FREEZING_TIME_MS),
                            ae -> {
                                button1.setText("");
                                button1.setDisable(false);
                                button2.setText("");
                                button2.setDisable(false);
                            }
                    ));
                    timeline.play();
                }
                lastButtonClicked = null;
            }
        });
        return button;
    }

    private void updateWinCondition() {
        buttonsLeft -= 2;
        if (buttonsLeft == 0) {
            sceneContainer.showWinAlert();
        }
    }
}
