package com.example.customcourses.controllers;

import com.example.customcourses.App;
import com.example.customcourses.managers.CoursesManager;
import com.example.customcourses.managers.MusicsManager;
import com.example.customcourses.models.Course;
import com.example.customcourses.models.Music;
import com.example.customcourses.utils.NotificationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.*;

public class RandomLegacyController {

    public HBox coverPane;
    public VBox randomLegacyContent;
    public ScrollPane randomLegacyPane;
    public VBox randomLegacySelected;
    private MainController mainController;
    @FXML private CheckBox expBox, twistBox, interBox, climaxBox, apoBox;
    @FXML private Spinner<Integer> maxScoreSpinner;
    @FXML private Button selectButtonLegacy;
    @FXML private Button playButton;

    @FXML private Label nameLabel;
    @FXML private Label difficultyLabel;
    @FXML private Label scoreLabel;

    private Course actualSelectedCourse;
    private Course.CourseDifficulty actualSelectedDifficulty;

    private final CoursesManager coursesManager = new CoursesManager();

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() throws Exception {
        playButton.setVisible(false);
        Stage mainStage = App.getPrimaryStage();
        List<Music> allMusics = MusicsManager.getMusics();
        if (allMusics.isEmpty()) {
            System.err.println("Empty Music list !");
        }

        try {
            coursesManager.loadLegacyCourses(allMusics);
        } catch (Exception e) {
            System.err.println("Charging Courses is impossible : " + e.getMessage());
            e.printStackTrace();
        }

        maxScoreSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 202_000_000, 4_040_000, 100_000)
        );

        selectButtonLegacy.setOnAction(e -> selectRandomCourse());
        playButton.setOnAction(e -> {
            try {
                if (actualSelectedCourse == null || actualSelectedDifficulty == null) {
                    NotificationUtil.showToast(mainStage, "There is no Legacy Course selected by the random selector.", "entryProblemAlert");
                    return;
                }

                System.out.println(actualSelectedCourse.getName() + ", " + actualSelectedDifficulty.toString());
                if (mainController != null) {
                    mainController.showSaveScoresWithCourse(actualSelectedCourse, actualSelectedDifficulty, true);
                }
            } catch (Exception ex) {
                // Problème pour accéder à Save Scores
            }
        });
    }

    private boolean isExtraUnlocked() {
        return coursesManager.getAllCourses().stream().anyMatch(course -> course.hasExtra() && !course.isExtraHidden());
    }

    private void selectRandomCourse() {
        int maxScore = maxScoreSpinner.getValue();

        List<Course.CourseDifficulty> allowedDiffs = new ArrayList<>();
        if (expBox.isSelected()) allowedDiffs.add(Course.CourseDifficulty.EXPOSITION);
        if (twistBox.isSelected()) allowedDiffs.add(Course.CourseDifficulty.TWIST);
        if (interBox.isSelected()) allowedDiffs.add(Course.CourseDifficulty.INTERLUDE);
        if (climaxBox.isSelected()) allowedDiffs.add(Course.CourseDifficulty.CLIMAX);
        if (apoBox.isSelected()) allowedDiffs.add(Course.CourseDifficulty.APOTHEOSIS);

        if (!isExtraUnlocked()) {
            allowedDiffs.remove(Course.CourseDifficulty.EXTRA);
        }

        CoursesManager.CourseSelection selection = coursesManager.getRandomCourse(allowedDiffs, maxScore);

        if (selection == null) {
            nameLabel.setText("Course not found !");
            difficultyLabel.setText("");
            scoreLabel.setText("");
            coverPane.getChildren().clear();
            System.out.println("Couldn't find corresponding Course.");
            playButton.setVisible(false);
            return;
        }

        nameLabel.setText(selection.getCourse().getName());
        difficultyLabel.setText("Difficulty : " + selection.getDifficulty());
        scoreLabel.setText("Best Score : " + selection.getBestScore());

        Course.CourseDifficulty diff = Course.CourseDifficulty.valueOf(selection.getDifficulty());
        Course.CourseDifficultySection section = selection.getCourse().getDifficulties().get(diff);

        if (diff == Course.CourseDifficulty.EXTRA && section.isHidden()) {
            playButton.setVisible(false);
            nameLabel.setText("It seems there has been a problem with the randomization...");
            difficultyLabel.setText("");
            scoreLabel.setText("");
            coverPane.getChildren().clear();
            return;
        }

        actualSelectedCourse = selection.getCourse();
        actualSelectedDifficulty = diff;
        playButton.setVisible(true);

        List<Music> musics = section.getMusics();
        List<Course.MusicDifficulty> diffs = section.getDifficultyLevels();

        coverPane.getChildren().clear();

        for (int i = 0; i < musics.size(); i++) {
            Music music = musics.get(i);
            Course.MusicDifficulty musicDiff = diffs.get(i);

            String coverPath = switch (musicDiff) {
                case BS -> music.getBsImage();
                default -> music.getImage();
            };

            try {
                InputStream is = getClass().getResourceAsStream("/covers/" + coverPath);
                if (is == null) {
                    System.err.println("File unreachable : /covers/" + coverPath);
                    continue;
                }

                ImageView imgView = new ImageView(new Image(is));
                imgView.setFitHeight(100);
                imgView.setFitWidth(100);
                imgView.setPreserveRatio(true);

                coverPane.getChildren().add(imgView);
                System.out.println("Jacket charged : " + coverPath);
            } catch (Exception e) {
                System.err.println("Couldn't charge jacket for : " + music.getTitle());
                e.printStackTrace();
            }
        }
    }
}
