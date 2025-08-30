package com.example.customcourses.controllers;

import com.example.customcourses.managers.CoursesManager;
import com.example.customcourses.managers.MusicsManager;
import com.example.customcourses.models.Course;
import com.example.customcourses.models.Music;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.InputStream;
import java.util.*;

public class RandomCourseController {

    public HBox coverPane;
    public ScrollPane randomCoursePane;
    public VBox randomCourseContent;
    public VBox randomCourseSelected;
    @FXML private CheckBox expBox, twistBox, interBox, climaxBox, apoBox;
    @FXML private Spinner<Integer> maxScoreSpinner;
    @FXML private Button selectButtonCourse;

    @FXML private Label nameLabel;
    @FXML private Label difficultyLabel;
    @FXML private Label scoreLabel;

    private final CoursesManager coursesManager = new CoursesManager();

    @FXML
    public void initialize() {
        List<Music> allMusics = MusicsManager.getMusics();
        if (allMusics.isEmpty()) {
            System.err.println("⚠️ Liste des musiques vide !");
        }

        try {
            coursesManager.loadCourses(allMusics);
        } catch (Exception e) {
            System.err.println("Impossible de charger les courses : " + e.getMessage());
            e.printStackTrace();
        }

        maxScoreSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 202_000_000, 4_040_000, 10_000)
        );

        selectButtonCourse.setOnAction(e -> selectRandomCourse());
    }

    private void selectRandomCourse() {
        int maxScore = maxScoreSpinner.getValue();

        List<Course.CourseDifficulty> allowedDiffs = new ArrayList<>();
        if (expBox.isSelected()) allowedDiffs.add(Course.CourseDifficulty.EXPOSITION);
        if (twistBox.isSelected()) allowedDiffs.add(Course.CourseDifficulty.TWIST);
        if (interBox.isSelected()) allowedDiffs.add(Course.CourseDifficulty.INTERLUDE);
        if (climaxBox.isSelected()) allowedDiffs.add(Course.CourseDifficulty.CLIMAX);
        if (apoBox.isSelected()) allowedDiffs.add(Course.CourseDifficulty.APOTHEOSIS);

        CoursesManager.CourseSelection selection = coursesManager.getRandomCourse(allowedDiffs, maxScore);

        if (selection == null) {
            nameLabel.setText("Aucune course trouvée !");
            difficultyLabel.setText("");
            scoreLabel.setText("");
            coverPane.getChildren().clear();
            System.out.println("Aucune course correspondante trouvée.");
            return;
        }

        nameLabel.setText(selection.getCourse().getName());
        difficultyLabel.setText("Difficulty : " + selection.getDifficulty());
        scoreLabel.setText("Best Score : " + selection.getBestScore());

        Course.CourseDifficulty diff = Course.CourseDifficulty.valueOf(selection.getDifficulty());
        Course.CourseDifficultySection section = selection.getCourse().getDifficulties().get(diff);

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
                    System.err.println("Fichier introuvable : /covers/" + coverPath);
                    continue;
                }

                ImageView imgView = new ImageView(new Image(is));
                imgView.setFitHeight(100);
                imgView.setFitWidth(100);
                imgView.setPreserveRatio(true);

                coverPane.getChildren().add(imgView);
                System.out.println("Jaquette chargée : " + coverPath);
            } catch (Exception e) {
                System.err.println("Impossible de charger la jaquette pour : " + music.getTitle());
                e.printStackTrace();
            }
        }
    }
}
