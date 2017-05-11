package ru.spbau.sazanovich.nikita;

import javafx.application.Application;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

public class FindPairApp extends Application {

    private static final int FIELD_SIZE = 4;

    /**
     * The main entry point for the Find Pair JavaFX application.
     *
     * @param primaryStage the stage onto which the application scene can be set
     * @throws Exception if some exception happens during app's execution
     */
    @Override
    public void start(@NotNull Stage primaryStage) throws Exception {
        FindPairSceneContainer sceneContainer = new FindPairSceneContainer(FIELD_SIZE, primaryStage);

        primaryStage.setTitle("Find Pair");
        primaryStage.setScene(sceneContainer.getScene());
        primaryStage.show();
    }

    /**
     * Starts Find Pair game with given command line arguments.
     *
     * @param args command line arguments
     */
    public static void main(@NotNull String[] args) {
        Application.launch(args);
    }
}
