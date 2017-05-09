package ru.spbau.sazanovich.nikita.client.gui;

import javafx.application.Application;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import ru.spbau.sazanovich.nikita.client.Client;
import ru.spbau.sazanovich.nikita.client.ClientFactory;
import ru.spbau.sazanovich.nikita.server.ServerCommandLineApp;

import java.nio.file.Path;
import java.nio.file.Paths;

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
        ClientFactory factory = () -> new Client(ServerCommandLineApp.SERVER_PORT);
        Path directory = Paths.get(System.getProperty("user.dir"));
        ClientSceneContainer sceneContainer = new ClientSceneContainer(factory, directory, primaryStage);

        primaryStage.setTitle("FPT client");
        primaryStage.setScene(sceneContainer.getScene());
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
