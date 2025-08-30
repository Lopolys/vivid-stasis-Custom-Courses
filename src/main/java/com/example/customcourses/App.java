package com.example.customcourses;

import com.example.customcourses.utils.CoursesSynchronizer;
import com.example.customcourses.utils.DataInitializer;
import com.example.customcourses.utils.UserPreferences;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import com.example.customcourses.utils.StyleUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        try {
            // Initialise les fichiers JSON modifiables au premier lancement et les synchronise
            DataInitializer.initializeJsonFiles();

            CoursesSynchronizer.syncCourses("courses.json");
            CoursesSynchronizer.syncCourses("coursesLegacy.json");

            Path dataDir = DataInitializer.getDataDirectory();
            System.out.println("JSON data directory: " + dataDir.toAbsolutePath());

            // Charge la vue principale (MainView.fxml)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/customcourses/views/MainView.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);

            // Sauvegarder les préférences à la fermeture
            primaryStage.setOnCloseRequest(event -> {
                try {
                    UserPreferences.getInstance().saveToFile();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            // Applique les préférences dès le démarrage
            StyleUtil.applyUserPreferences(scene);

            primaryStage.setOnCloseRequest(event -> {
                try {
                    UserPreferences.getInstance().saveToFile();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            primaryStage.getIcons().add(new Image(Objects.requireNonNull(App.class.getResourceAsStream("/images/icon.png"))));
            primaryStage.setTitle("vivid/stasis Custom Courses");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
