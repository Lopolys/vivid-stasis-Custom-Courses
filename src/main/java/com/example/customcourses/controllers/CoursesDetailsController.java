package com.example.customcourses.controllers;

import com.example.customcourses.models.Course;
import com.example.customcourses.models.Music;
import com.example.customcourses.models.Course.CourseDifficulty;
import com.example.customcourses.models.Course.CourseDifficultySection;
import com.example.customcourses.models.Course.MusicDifficulty;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;

import java.util.*;

public class CoursesDetailsController {

    public VBox courseDetailsBox;
    public ScrollPane detailsScrollPane;
    public Button detailsBackButton;
    @FXML private GridPane detailsCourseGrid;

    private Course course;
    private MainController mainController;

    public void setCourse(Course course) {
        this.course = course;
        populateGrid();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void initialize() {
        detailsScrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            double deltaY = event.getDeltaY() * 3; // multiplier par un facteur pour accélérer le scroll
            detailsScrollPane.setVvalue(detailsScrollPane.getVvalue() - deltaY / detailsScrollPane.getContent().getBoundsInLocal().getHeight());
            event.consume();  // Empêche le scroll normal pour appliquer le tien
        });
    }

    private void populateGrid() {
        detailsCourseGrid.getChildren().clear();
        detailsCourseGrid.getColumnConstraints().clear();
        detailsCourseGrid.getRowConstraints().clear();
        detailsCourseGrid.getStyleClass().add("detailsGridPane");

        Map<CourseDifficulty, CourseDifficultySection> difficulties = course.getDifficulties();
        List<CourseDifficulty> difficultyOrder = new ArrayList<>(difficulties.keySet());
        int columnCount = difficultyOrder.size()+1;

        // Fixer la largeur de la première colonne
        ColumnConstraints firstCol = new ColumnConstraints();
        firstCol.setPercentWidth(8);
        detailsCourseGrid.getColumnConstraints().add(firstCol);

        // Ajouter des contraintes génériques pour les autres colonnes (les difficultés)
        for (int i = 1; i < columnCount; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setHgrow(Priority.ALWAYS); // Permet d'étendre les colonnes restantes
            col.setPercentWidth((100.0 - firstCol.getPercentWidth()) / (columnCount - 1)); // Réparti l'espace restant
            detailsCourseGrid.getColumnConstraints().add(col);
        }

        int currentRow = 0;

        // === Ligne 1 : Titre de la course (fusionné)
        Label titleLabel = new Label(course.getName());
        titleLabel.setWrapText(true);
        titleLabel.getStyleClass().addAll("detailsCourseTitle", "detailGridCell", "topCell");
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        titleLabel.setAlignment(Pos.CENTER);
        GridPane.setColumnSpan(titleLabel, columnCount);
        detailsCourseGrid.add(titleLabel, 0, currentRow++);

        // === Ligne 2 : Noms des difficultés
        for (int col = 0; col < columnCount; col++) {
            if (col == 0){
                Label diffLabel = new Label("Difficulty");
                diffLabel.getStyleClass().addAll("detailsCourseIndicators", "detailGridCell");
                diffLabel.setMaxWidth(Double.MAX_VALUE);
                diffLabel.setMaxHeight(Double.MAX_VALUE);
                diffLabel.setAlignment(Pos.CENTER);
                detailsCourseGrid.add(diffLabel, col, currentRow);
            }
            else{
                CourseDifficulty difficulty = difficultyOrder.get(col-1);
                Label diffLabel = new Label(difficulty.name());
                if (col == columnCount - 1){
                    diffLabel.getStyleClass().addAll("detailsCourseIndicators", "detailRightCell");

                } else {
                    diffLabel.getStyleClass().addAll("detailsCourseIndicators", "detailGridCell");
                }
                diffLabel.setMaxWidth(Double.MAX_VALUE);
                diffLabel.setMaxHeight(Double.MAX_VALUE);
                diffLabel.setAlignment(Pos.CENTER);
                detailsCourseGrid.add(diffLabel, col, currentRow);
            }
        }
        currentRow++;

        // === Lignes 3 à X : les musiques
        int maxMusicCount = difficulties.values().stream().mapToInt(section -> section.getMusics().size()).max().orElse(0);

        for (int i = 0; i < maxMusicCount; i++) {
            for (int col = 0; col < columnCount; col++) {
                if (col == 0) {
                    Label musicLabel = new Label("Music " + (i + 1));
                    musicLabel.getStyleClass().addAll("detailsCourseIndicators", "detailGridCell");
                    musicLabel.setMaxWidth(Double.MAX_VALUE);
                    musicLabel.setMaxHeight(Double.MAX_VALUE);
                    musicLabel.setAlignment(Pos.CENTER);
                    detailsCourseGrid.add(musicLabel, col, currentRow);
                } else {
                    CourseDifficulty difficulty = difficultyOrder.get(col - 1);
                    CourseDifficultySection section = difficulties.get(difficulty);

                    // Si l’index de musique est hors de portée, on ignore la cellule
                    if (i >= section.getMusics().size()) continue;

                    Music music = section.getMusics().get(i);
                    MusicDifficulty level = section.getDifficultyLevels().get(i);

                    HBox detailsMusicContent = new HBox(10);
                    detailsMusicContent.setPadding(new Insets(5));
                    detailsMusicContent.setMaxWidth(Double.MAX_VALUE);
                    detailsMusicContent.setAlignment(Pos.CENTER_LEFT);

                    if (col == columnCount - 1){
                        detailsMusicContent.getStyleClass().add("detailRightCell");

                    } else {
                        detailsMusicContent.getStyleClass().add("detailGridCell");
                    }
                    // Image
                    String imageName = switch (level){
                        case BS -> music.getBsImage();
                        default -> music.getImage();
                    };

                    StackPane imageContainer = new StackPane();
                    String imagePath = "/covers/" + imageName;
                    String lower = imageName.toLowerCase();

                    try {
                        ImageView imageView = new ImageView();
                        imageView.setPreserveRatio(true);
                        imageView.setFitWidth(120);
                        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath)));
                        imageView.setImage(image);
                        imageContainer.getChildren().add(imageView);
                    } catch (Exception e) {

                    }

                    detailsMusicContent.getChildren().add(imageContainer);

                    // Infos texte
                    String musicTitle = switch (level){
                        case BS -> music.getBsTitle();
                        default -> music.getTitle();
                    };

                    Label musicTitleLabel = new Label(musicTitle);
                    musicTitleLabel.setWrapText(true);
                    musicTitleLabel.getStyleClass().add("musicTitle");
                    musicTitleLabel.setMaxWidth(Double.MAX_VALUE);
                    musicTitleLabel.setAlignment(Pos.CENTER);

                    Label diffValue = new Label(level + " " + level.getDifficultyValue(music));

                    String charter = switch (level) {
                        case OP -> music.getOpCharter();
                        case MD -> music.getMdCharter();
                        case FN -> music.getFnCharter();
                        case EC -> music.getEcCharter();
                        case BS -> music.getBsCharter();
                    };
                    Label charterLabel = new Label("Charter: " + charter);
                    charterLabel.setWrapText(true);

                    String length = switch (level){
                        case BS -> music.getBsLength();
                        default -> music.getLength();
                    };
                    Label lengthLabel = new Label("Length: " + length);

                    VBox textBox = new VBox(2, musicTitleLabel, diffValue, charterLabel, lengthLabel);
                    textBox.setAlignment(Pos.CENTER);
                    textBox.setMaxWidth(Double.MAX_VALUE);

                    HBox.setHgrow(textBox, Priority.ALWAYS);
                    detailsMusicContent.getChildren().add(textBox);

                    detailsCourseGrid.add(detailsMusicContent, col, currentRow);
                }
            }
            currentRow++;
        }

        // === Dernière ligne : Best score & rank
        for (int col = 0; col < columnCount; col++) {
            if (col == 0){
                Label scoreLabel = new Label("Best Score");
                scoreLabel.setWrapText(true);
                scoreLabel.getStyleClass().addAll("detailsCourseIndicators", "bottomLeftCell");
                scoreLabel.setMaxWidth(Double.MAX_VALUE);
                scoreLabel.setMaxHeight(Double.MAX_VALUE);
                scoreLabel.setAlignment(Pos.CENTER);
                detailsCourseGrid.add(scoreLabel, col, currentRow);
            }
            else {
                CourseDifficulty difficulty = difficultyOrder.get(col-1);
                CourseDifficultySection section = difficulties.get(difficulty);
                String text = section.getBestScore() + " ( " + section.getBestRank() + " )";
                Label scoreLabel = new Label(text);
                scoreLabel.setMaxWidth(Double.MAX_VALUE);
                scoreLabel.setMaxHeight(Double.MAX_VALUE);
                scoreLabel.setWrapText(true);
                scoreLabel.setAlignment(Pos.CENTER);
                if (col == columnCount - 1) {
                    scoreLabel.getStyleClass().add("bottomRightCell");
                } else {
                    scoreLabel.getStyleClass().add("detailScoreCell");
                }
                detailsCourseGrid.add(scoreLabel, col, currentRow);
            }
        }
        currentRow++;

        // === Boutons "Enter New Score"
        for (int col = 1; col < columnCount; col++) { // commence à 1 car 0 est colonne des labels
            CourseDifficulty difficulty = difficultyOrder.get(col - 1);
            Button button = new Button("Enter New Score");
            button.setAlignment(Pos.CENTER);
            GridPane.setHalignment(button, HPos.CENTER);

            // Ajouter le listener au bouton
            button.setOnAction(e -> {
                if (mainController != null) {
                    boolean isLegacy = CoursesController.getIsLegacy();
                    mainController.showSaveScoresWithCourse(course, difficulty, isLegacy);
                }
            });

            detailsCourseGrid.add(button, col, currentRow);
        }
    }

    public void refresh() {
        if (this.course != null) {
            populateGrid(); // Recharge les données
        }
    }

    @FXML
    private void onBack() {
        if (mainController != null) {
            mainController.showCoursesView();
        }
    }
}
