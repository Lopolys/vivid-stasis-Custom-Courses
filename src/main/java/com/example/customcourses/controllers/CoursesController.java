package com.example.customcourses.controllers;

import com.example.customcourses.managers.MusicsManager;
import com.example.customcourses.models.Course;
import com.example.customcourses.models.Music;
import com.example.customcourses.utils.DataInitializer;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CoursesController {

    @FXML private ScrollPane coursePane;
    @FXML private Button loadCoursesBtn;
    @FXML private Button loadLegacyBtn;
    @FXML private FlowPane courseListContainer;
    private MainController mainController;
    private List<Course> courses;
    private static boolean isLegacy;

    public static boolean getIsLegacy() {
        return isLegacy;
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
        populateCourses();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void initialize() throws Exception {
        DataInitializer.initializeJsonFiles();

        // Par défaut, charger le fichier courses.json
        Path dataDir = DataInitializer.getDataDirectory();

        loadCoursesFromFile(dataDir.resolve("courses.json"), false);

        // Attacher les actions des boutons
        loadCoursesBtn.setOnAction(_ -> loadCoursesFromFile(dataDir.resolve("courses.json"), false));
        loadLegacyBtn.setOnAction(_ -> loadCoursesFromFile(dataDir.resolve("coursesLegacy.json"), true));

        coursePane.addEventFilter(ScrollEvent.SCROLL, event -> {
            double deltaY = event.getDeltaY() * 3; // multiplier par un facteur pour accélérer le scroll
            coursePane.setVvalue(coursePane.getVvalue() - deltaY / coursePane.getContent().getBoundsInLocal().getHeight());
            event.consume();  // Empêche le scroll normal pour appliquer le tien
        });
    }

    private void loadCoursesFromFile(Path jsonFilePath, boolean isLegacy) {
        try {
            List<Course> loadedCourses = com.example.customcourses.utils.CourseLoader.loadCoursesFromFile(jsonFilePath);

            for (Course course : loadedCourses) {
                for (Course.CourseDifficultySection section : course.getDifficulties().values()) {
                    section.resolveMusics(MusicsManager.getMusics());
                }
            }
            setCourses(loadedCourses);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.isLegacy = isLegacy;
    }

    private void populateCourses() {
        courseListContainer.getChildren().clear();

        for (Course course : courses) {
            VBox courseBox = new VBox(10);
            courseBox.setPadding(new Insets(10));

            Label nameLabel = new Label(course.getName());
            nameLabel.setWrapText(true);
            nameLabel.setAlignment(Pos.CENTER);

            HBox labelWrapper = new HBox(nameLabel);
            labelWrapper.setAlignment(Pos.CENTER);
            List<Music> musics;

            Course.CourseDifficultySection section = course.getDifficulties().get(Course.CourseDifficulty.EXPOSITION);
            if (section != null) {
                musics = section.getMusics();
            } else {
                musics = course.getDifficulties().get(Course.CourseDifficulty.DESTROYED).getMusics();
            }

            VBox jacketRows = new VBox(10);
            HBox jacketRow = new HBox(10);
            int musicCount = 0;

            for (Music music : musics) {
                ImageView jacket = new ImageView();
                try {
                    Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/covers/" + music.getImage())));
                    jacket.setImage(image);
                } catch (Exception e) {
                    // Image non trouvée, on ignore
                }
                jacket.setFitWidth(100);
                jacket.setFitHeight(100);
                jacketRow.getChildren().add(jacket);
                jacketRow.setAlignment(Pos.CENTER);
                musicCount++;

                if (musicCount == 9) {
                    jacketRows.getChildren().add(jacketRow);
                    jacketRow = new HBox(10); // ← recrée un NOUVEAU jacketRow !
                    musicCount = 0;
                }
            }

            if (!jacketRow.getChildren().isEmpty()) {
                jacketRows.getChildren().add(jacketRow);
            }
            jacketRows.setAlignment(Pos.CENTER);

            HBox buttonRow = new HBox(10);
            Button enterScoreBtn = new Button("Enter Score");
            Button detailsBtn = new Button("Details");

            detailsBtn.setOnAction(_ -> showDetails(course));
            enterScoreBtn.setOnAction(_ -> enterScore(course));

            buttonRow.getChildren().addAll(enterScoreBtn, detailsBtn);
            buttonRow.setAlignment(Pos.CENTER);

            courseBox.getChildren().addAll(labelWrapper, jacketRows, buttonRow);
            courseBox.setAlignment(Pos.CENTER);
            courseBox.getStyleClass().add("courseBox");
            courseListContainer.getChildren().add(courseBox);
            courseListContainer.setAlignment(Pos.CENTER);
        }
    }

    private void showDetails(Course course) {
        if (mainController != null) {
            mainController.showDetails(course);
        }
    }

    private void enterScore(Course course) {
        if (mainController != null) {
            mainController.showSaveScoresWithCourse(course, isLegacy);
        }
    }
}
