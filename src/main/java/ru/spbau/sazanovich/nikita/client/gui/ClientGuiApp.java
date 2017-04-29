package ru.spbau.sazanovich.nikita.client.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import ru.spbau.sazanovich.nikita.client.Client;
import ru.spbau.sazanovich.nikita.server.ServerCommandLineApp;

/**
 * Class which interacts with user through graphical interface and make requests to the server.
 */
public class ClientGuiApp extends Application {

    /**
     * The main entry point for the client JavaFX application.
     *
     * @param primaryStage the stage onto which the application scene can be set
     * @throws Exception if some exception happens during app's execution
     */
    @Override
    public void start(@NotNull Stage primaryStage) throws Exception {
        ClientSceneBuilder sceneBuilder = new ClientSceneBuilder(() -> new Client(ServerCommandLineApp.SERVER_PORT));
        Scene scene = sceneBuilder.build(primaryStage);

        primaryStage.setTitle("FPT client");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Starts GUI application with given command line arguments.
     *
     * @param args command line arguments
     */
    public static void main(@NotNull String[] args) {
        Application.launch(args);
    }
}
