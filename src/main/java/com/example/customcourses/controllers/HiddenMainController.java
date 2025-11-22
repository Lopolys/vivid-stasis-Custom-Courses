package com.example.customcourses.controllers;

import com.example.customcourses.App;
import com.example.customcourses.managers.MusicsManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import java.io.IOException;
import java.util.ArrayList;

import com.example.customcourses.models.Course;
import com.example.customcourses.models.TitleUnlocker;
import com.example.customcourses.managers.CoursesManager;

public class HiddenMainController {

    @FXML
    private final CoursesManager coursesManager = new CoursesManager();
    private HiddenCoursesDetailsController coursesDetailsController;
    private Course lastCourseShown;

    @FXML private StackPane hiddenMainContent;

    private App app;

    public void setApp(App app) {
        this.app = app;
    }

    @FXML
    private void initialize() throws Exception {
        TitleUnlocker.checkAndUnlockTitles();

        MusicsManager.loadMusics();
        coursesManager.loadHiddenCourses(MusicsManager.getMusics());
        loadView("/com/example/customcourses/views/HiddenCoursesView.fxml");
    }

    @FXML
    private void handleHiddenCourses(ActionEvent event) {
        loadView("/com/example/customcourses/views/HiddenCoursesView.fxml");
    }

    @FXML
    private void handleHiddenScores(ActionEvent event) {
        loadView("/com/example/customcourses/views/HiddenSaveScoresView.fxml");
    }

    @FXML
    private void handleHiddenMusics(ActionEvent event) {
        loadView("/com/example/customcourses/views/HiddenMusicsView.fxml");
    }

    @FXML
    private void handleHiddenHistory(ActionEvent event) {
        loadView("/com/example/customcourses/views/HiddenHistoryView.fxml");
    }

    @FXML
    private void hiddenHandleBack(ActionEvent event) {
        if (app != null) {
            app.restoreNormalMode();
        }
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            Object controller = loader.getController();

            // === Hidden CoursesController ===
            if (controller instanceof HiddenCoursesController coursesController) {
                coursesController.setMainController(this);
                coursesController.setCourses(new ArrayList<>(coursesManager.getAllCourses()));
                refreshCourseDetails();
            }

            // === Hidden MusicsController ===
            if (controller instanceof HiddenMusicsController musicsController) {
                musicsController.setMainController(this);
            }

            // === Hidden SaveScoresController ===
            if (controller instanceof HiddenSaveScoresController saveScoresController) {
                saveScoresController.setMainController(this);
            }

            // === Hidden HistoryController ===
            if (controller instanceof HiddenHistoryController historyController) {
                historyController.setMainController(this);
            }

            hiddenMainContent.getChildren().setAll(view);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refreshCourseDetails() {
        if (coursesDetailsController != null && lastCourseShown != null) {
            // Recharger les courses depuis le fichier
            coursesManager.loadHiddenCourses(MusicsManager.getMusics());

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
        loadView("/com/example/customcourses/views/HiddenCoursesView.fxml");
    }

    public void showDetails(Course course) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/customcourses/views/HiddenCoursesDetailsView.fxml"));
            Parent view = loader.load();

            HiddenCoursesDetailsController controller = loader.getController();
            controller.setCourse(course);
            controller.setMainController(this);

            this.coursesDetailsController = controller;
            this.lastCourseShown = course;

            hiddenMainContent.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showSaveScoresWithCourse(Course course) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/customcourses/views/HiddenSaveScoresView.fxml"));
            Parent view = loader.load();

            HiddenSaveScoresController controller = loader.getController();
            controller.setMainController(this);
            controller.setCourse(course);  // Méthode à créer dans SaveScoresController

            hiddenMainContent.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
