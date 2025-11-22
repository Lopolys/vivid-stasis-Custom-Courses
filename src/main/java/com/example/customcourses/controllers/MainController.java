package com.example.customcourses.controllers;

import com.example.customcourses.App;
import com.example.customcourses.managers.CoursesManager;
import com.example.customcourses.managers.MusicsManager;
import com.example.customcourses.models.Course;
import com.example.customcourses.models.TitleUnlocker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import com.example.customcourses.utils.StyleUtil;

import static com.example.customcourses.models.Title.loadUnlockedTitleIds;
import static com.example.customcourses.models.Title.unlockTitle;

public class MainController {
    private final CoursesManager coursesManager = new CoursesManager();
    private CoursesDetailsController coursesDetailsController;
    private Course lastCourseShown;

    @FXML private StackPane mainContent;
    @FXML private BorderPane rootPane;
    @FXML private Label versionLabel;
    private int versionClickCount;

    private App app;

    public void setApp(App app) {
        this.app = app;
    }

    // Cette méthode est appelée automatiquement après le chargement FXML
    @FXML
    private void initialize() throws Exception {
        applyUserPreferences();
        TitleUnlocker.checkAndUnlockTitles();

        MusicsManager.loadMusics();
        coursesManager.loadCourses(MusicsManager.getMusics());
        loadView("/com/example/customcourses/views/CoursesView.fxml");

        versionLabel.setOnMouseClicked(event -> {
            versionClickCount++;

            System.out.println("Clics sur le label : " + versionClickCount);
            try {
                Set<String> titlesList = loadUnlockedTitleIds();
                if (versionClickCount == 12 && !titlesList.contains("misc12")) {
                    unlockTitle("misc12");
                    TitleUnlocker.checkAndUnlockTitles();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    void applyUserPreferences() throws Exception {
        Scene scene = rootPane.getScene();
        if (scene == null) {
            rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    try {
                        StyleUtil.applyUserPreferences(newScene);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } else {
            StyleUtil.applyUserPreferences(scene);
        }
    }

    // Méthodes appelées par les boutons (via onAction="#handleX")
    @FXML
    private void handleCourses(ActionEvent event) {
        loadView("/com/example/customcourses/views/CoursesView.fxml");
    }

    @FXML
    private void handleScores(ActionEvent event) {
        loadView("/com/example/customcourses/views/SaveScoresView.fxml");
    }

    @FXML
    private void handleMusics(ActionEvent event) {
        loadView("/com/example/customcourses/views/MusicsView.fxml");
    }

    @FXML
    private void handleHistory(ActionEvent event) {
        loadView("/com/example/customcourses/views/HistoryView.fxml");
    }

    @FXML
    private void handleSettings(ActionEvent event) {
        loadView("/com/example/customcourses/views/SettingsView.fxml");
    }

    @FXML
    private void handleInfo(ActionEvent event) {
        loadView("/com/example/customcourses/views/InfoView.fxml");
    }

    @FXML
    private void handleRandom(ActionEvent actionEvent) {
        loadView("/com/example/customcourses/views/RandomSelectionView.fxml");
    }

    // Méthode générique pour charger une vue FXML dans mainContent
    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            // Récupérer le contrôleur de la vue chargée
            Object controller = loader.getController();

            // Si c'est CoursesController, lui passer une référence à ce MainController
            if (controller instanceof CoursesController coursesController) {
                coursesController.setMainController(this);
                coursesController.setCourses(new ArrayList<>(coursesManager.getAllCourses()));
                refreshCourseDetails();
            }

            if (controller instanceof CoursesDetailsController detailsController) {
                detailsController.setMainController(this);
            }

            // Si c'est MusicsController, lui passer une référence à ce MainController
            if (controller instanceof MusicsController musicsController) {
                musicsController.setMainController(this);
            }

            // Si c'est SaveScoresController, lui passer une référence à ce MainController
            if (controller instanceof SaveScoresController saveScoresController) {
                saveScoresController.setMainController(this);
            }

            // Si c'est HistoryController, lui passer une référence à ce MainController
            if (controller instanceof HistoryController historyController) {
                historyController.setMainController(this);
            }

            // Si c’est SettingsController, lui passer une référence à ce MainController
            if (controller instanceof SettingsController settingsController) {
                settingsController.refreshTitlesDisplay();
                settingsController.setMainController(this);
            }

            // Si c’est RandomSelectionController, lui passer une référence à ce MainController
            if (controller instanceof RandomSelectionController randomSelectController) {
                randomSelectController.setMainController(this);
            }

            mainContent.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void showDetails(Course course) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/customcourses/views/CoursesDetailsView.fxml"));
            Parent view = loader.load();

            CoursesDetailsController controller = loader.getController();
            controller.setCourse(course);
            controller.setMainController(this);

            this.coursesDetailsController = controller;
            this.lastCourseShown = course;

            mainContent.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refreshCourseDetails() {
        if (coursesDetailsController != null && lastCourseShown != null) {
            // Recharger les courses depuis le fichier
            coursesManager.loadCourses(MusicsManager.getMusics());

            // Retrouver une nouvelle instance d'une même course
            Course updatedCourse = coursesManager.getAllCourses().stream()
                    .filter(c -> c.getName().equals(lastCourseShown.getName()))
                    .findFirst()
                    .orElse(null);

            if (updatedCourse != null) {
                this.lastCourseShown = updatedCourse; // met à jour le champ
                coursesDetailsController.setCourse(updatedCourse); // déclenche le populateGrid() via setCourse()
            }
        }
    }

    public void showCoursesView() {
        loadView("/com/example/customcourses/views/CoursesView.fxml");
    }

    public void showSaveScoresWithCourse(Course course, boolean isLegacy) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/customcourses/views/SaveScoresView.fxml"));
            Parent view = loader.load();

            SaveScoresController controller = loader.getController();
            controller.setMainController(this);
            controller.setCourse(course, isLegacy);  // Méthode à créer dans SaveScoresController

            mainContent.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void showSaveScoresWithCourse(Course course, Course.CourseDifficulty difficulty, boolean isLegacy) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/customcourses/views/SaveScoresView.fxml"));
            Parent view = loader.load();

            SaveScoresController controller = loader.getController();
            controller.setMainController(this);
            controller.setCourse(course, difficulty, isLegacy);  // Méthode existante à adapter dans SaveScoresController

            mainContent.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}