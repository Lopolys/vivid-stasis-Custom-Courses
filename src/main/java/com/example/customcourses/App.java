package com.example.customcourses;

import com.example.customcourses.controllers.HiddenMainController;
import com.example.customcourses.controllers.MainController;
import com.example.customcourses.utils.CoursesSynchronizer;
import com.example.customcourses.utils.DataInitializer;
import com.example.customcourses.utils.UserPreferences;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import com.example.customcourses.utils.StyleUtil;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;

import static com.example.customcourses.models.Title.loadUnlockedTitleIds;
import static com.example.customcourses.models.Title.unlockTitle;

public class App extends Application {

    private Scene normalScene;
    private Scene hiddenScene;
    private static Stage primaryStage;
    private int count;

    @Override
    public void start(Stage primaryStage) {
        App.primaryStage = primaryStage;
        count = 0;
        try {
            // Initialise les fichiers JSON modifiables au premier lancement et les synchronise
            DataInitializer.initializeJsonFiles();

            CoursesSynchronizer.syncCourses("courses.json");
            CoursesSynchronizer.syncCourses("coursesLegacy.json");
            CoursesSynchronizer.syncCourses("hiddenCourses.json");

            Path dataDir = DataInitializer.getDataDirectory();
            System.out.println("JSON data directory: " + dataDir.toAbsolutePath());

            // Charge la vue principale (MainView.fxml)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/customcourses/views/MainView.fxml"));
            Parent root = loader.load();
            MainController mainController = loader.getController();
            mainController.setApp(this);

            normalScene = new Scene(root);
            StyleUtil.applyUserPreferences(normalScene);

            // Prépare la vue Boundary Shatter (HiddenMainView.fxml)
            FXMLLoader hiddenLoader = new FXMLLoader(getClass().getResource("/com/example/customcourses/views/HiddenMainView.fxml"));
            Parent hiddenRoot = hiddenLoader.load();
            HiddenMainController hiddenController = hiddenLoader.getController();
            hiddenController.setApp(this);

            hiddenScene = new Scene(hiddenRoot);
            StyleUtil.applyBoundaryShatter(hiddenScene);

            primaryStage.setOnCloseRequest(event -> {
                try {
                    UserPreferences.getInstance().saveToFile();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            primaryStage.getIcons().add(new Image(Objects.requireNonNull(App.class.getResourceAsStream("/images/icon.png"))));
            primaryStage.setTitle("vivid/stasis Custom Courses");
            primaryStage.setScene(normalScene);
            primaryStage.setMaximized(true);
            primaryStage.show();

            normalScene.setOnKeyPressed(event -> {
                count++;
                if (event.getCode() == KeyCode.TAB) {
                    event.consume();
                    if (Math.random() < 0.05 || count >= 15) {
                        System.out.println("You broke it...");
                        primaryStage.setTitle("Boundary Shatter");
                        primaryStage.setScene(hiddenScene);
                        primaryStage.setMaximized(false);
                        primaryStage.setMaximized(true);
                        count = 0;
                        try {
                            Set<String> titlesList = loadUnlockedTitleIds();
                            if (!titlesList.contains("miscBreak")){
                                unlockTitle("miscBreak");
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* Revient en mode normal */
    public void restoreNormalMode() {
        primaryStage.setTitle("vivid/stasis Custom Courses");
        primaryStage.setScene(normalScene);
        primaryStage.setMaximized(false);
        primaryStage.setMaximized(true);
    }

    public static Stage getPrimaryStage(){
        return primaryStage;
    }

    public static void main(String[] args) {
        launch();
    }
}
