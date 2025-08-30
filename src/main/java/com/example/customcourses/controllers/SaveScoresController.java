package com.example.customcourses.controllers;

import com.example.customcourses.utils.UserPreferences;
import com.example.customcourses.models.*;
import com.example.customcourses.managers.*;
import com.example.customcourses.utils.DataInitializer;
import com.example.customcourses.utils.RankUtil;
import com.example.customcourses.utils.ScoreStorage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;

public class SaveScoresController {
    public HBox courseSelectionBox;
    public VBox saveScoresPane;
    public ScrollPane saveScrollPane;
    public HBox fileHandlerPane;
    @FXML private ComboBox<Course> courseCombo;
    @FXML private HBox difficultyRadioButtonsBox;
    @FXML private VBox mainContentBox;
    @FXML private HBox buttonsBox;

    private MainController mainController;
    private final CoursesManager coursesManager = new CoursesManager();
    private Course selectedCourse;
    private Course.CourseDifficulty selectedDifficulty;

    private final List<TextField> scoreFields = new ArrayList<>();
    private final List<Label> rankLabels = new ArrayList<>();
    private final List<Integer> enteredScores = new ArrayList<>();
    private List<Music> currentMusics = new ArrayList<>();

    private ToggleGroup difficultyToggleGroup;
    private final ObjectMapper objectMapper = new ObjectMapper();


    @FXML private Button loadCoursesBtn;
    @FXML private Button loadLegacyBtn;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        MusicsManager.loadMusics();
        coursesManager.loadCourses(MusicsManager.getMusics());

        courseCombo.getItems().setAll(coursesManager.getAllCourses());

        // Setup bouton Courses
        loadCoursesBtn.setOnAction(e -> {
            coursesManager.loadCourses(MusicsManager.getMusics()); // Charge le fichier "normal"
            courseCombo.getItems().setAll(coursesManager.getAllCourses());
            courseCombo.getSelectionModel().clearSelection();
            clearScoreInputs();
        });

        // Setup bouton Legacy Courses
        loadLegacyBtn.setOnAction(e -> {
            coursesManager.loadLegacyCourses(MusicsManager.getMusics()); // Charge le fichier legacy
            courseCombo.getItems().setAll(coursesManager.getAllCourses());
            courseCombo.getSelectionModel().clearSelection();
            clearScoreInputs();
        });

        courseCombo.setCellFactory(comboBox -> new ListCell<>() {
            @Override
            protected void updateItem(Course course, boolean empty) {
                super.updateItem(course, empty);
                setText((empty || course == null) ? null : course.getName());
            }
        });

        courseCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Course course, boolean empty) {
                super.updateItem(course, empty);
                setText((empty || course == null) ? null : course.getName());
            }
        });

        courseCombo.setOnAction(e -> onCourseSelected());

        difficultyToggleGroup = new ToggleGroup();
        difficultyToggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle != null) {
                RadioButton selectedRadio = (RadioButton) newToggle;
                selectedDifficulty = Course.CourseDifficulty.valueOf(selectedRadio.getText().toUpperCase());
                onDifficultySelected();
            } else {
                selectedDifficulty = null;
                clearScoreInputs();
            }
        });

        saveScrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            double deltaY = event.getDeltaY() * 3; // multiplier par un facteur pour accélérer le scroll
            saveScrollPane.setVvalue(saveScrollPane.getVvalue() - deltaY / saveScrollPane.getContent().getBoundsInLocal().getHeight());
            event.consume();  // Empêche le scroll normal pour appliquer le tien
        });
    }

    private void onCourseSelected() {
        selectedCourse = courseCombo.getValue();

        difficultyRadioButtonsBox.getChildren().clear();
        difficultyToggleGroup.getToggles().clear();

        enteredScores.clear();
        scoreFields.clear();
        rankLabels.clear();
        mainContentBox.getChildren().clear();
        buttonsBox.getChildren().clear();

        if (selectedCourse != null) {
            // Créer les RadioButtons pour chaque difficulté disponible dans la course
            for (Course.CourseDifficulty diff : selectedCourse.getDifficulties().keySet()) {
                RadioButton rb = new RadioButton(diff.name());
                rb.getStyleClass().add("scoresButton");
                rb.setToggleGroup(difficultyToggleGroup);
                difficultyRadioButtonsBox.getChildren().add(rb);
            }
        }
    }

    private void onDifficultySelected() {
        enteredScores.clear();
        scoreFields.clear();
        rankLabels.clear();
        mainContentBox.getChildren().clear();
        buttonsBox.getChildren().clear();

        if (selectedDifficulty != null && selectedCourse != null) {
            currentMusics = selectedCourse.getDifficulties().get(selectedDifficulty).getMusics();
            createScoreInputs();
        }
    }

    private void clearScoreInputs() {
        enteredScores.clear();
        scoreFields.clear();
        rankLabels.clear();
        mainContentBox.getChildren().clear();
        buttonsBox.getChildren().clear();
    }

    private void createScoreInputs() {
        scoreFields.clear();
        rankLabels.clear();
        mainContentBox.getChildren().clear();
        buttonsBox.getChildren().clear();

        List<Course.MusicDifficulty> diffs = selectedCourse.getDifficulties().get(selectedDifficulty).getDifficultyLevels();

        GridPane grid = new GridPane();
        grid.getStyleClass().add("scoreEntryGrid");
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        grid.setAlignment(Pos.CENTER);

        for (int i = 0; i < currentMusics.size(); i++) {
            Music music = currentMusics.get(i);
            Course.MusicDifficulty musicDiff = diffs.get(i);

            int baseRow = i * 2;

            // == Colonne 0 : Jaquette sur 2 lignes ==
            String imageName = switch (musicDiff){
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
                grid.add(imageView, 0, baseRow, 1, 2); // colspan 1, rowspan 2
            } catch (Exception e) {

            }

            // == Colonne 1, Ligne baseRow : Infos ==
            VBox info = new VBox(2);
            Label titleLabel = new Label(music.getTitle());
            titleLabel.getStyleClass().add("musicTitle");
            Label diffLabel = new Label(musicDiff + " " + musicDiff.getDifficultyValue(music));
            diffLabel.getStyleClass().add("scoreLabel");
            info.getChildren().addAll(titleLabel, diffLabel);
            info.setAlignment(Pos.CENTER);
            GridPane.setHalignment(info, HPos.CENTER);
            GridPane.setValignment(info, VPos.BOTTOM);
            grid.add(info, 1, baseRow);

            // == Colonne 1, Ligne baseRow+1 : Champ score ==
            TextField tf = new TextField();
            tf.setPromptText("0–1 010 000");
            tf.setPrefWidth(100);
            scoreFields.add(tf);
            grid.add(tf, 1, baseRow + 1);

            // == Colonne 2, Ligne baseRow+1 : Rang ==
            Label rankLabel = new Label("???");
            rankLabel.getStyleClass().add("entryScore");
            rankLabel.setPrefWidth(60);
            rankLabel.setAlignment(Pos.CENTER);
            rankLabels.add(rankLabel);
            grid.add(rankLabel, 2, baseRow + 1);

            // == Listener de rang dynamique ==
            tf.textProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    int score = Integer.parseInt(newValue.replaceAll("[^0-9]", ""));
                    if (score < 0 || score > 1_010_000) {
                        rankLabel.setText("???");
                    } else {
                        rankLabel.setText(RankUtil.calculateMusicRank(score));
                    }
                } catch (NumberFormatException e) {
                    rankLabel.setText("???");
                }
            });
        }

        for (int i = 0; i < currentMusics.size(); i++) {
            RowConstraints row1 = new RowConstraints();
            row1.setMinHeight(60);
            row1.setVgrow(Priority.NEVER);
            row1.setValignment(VPos.CENTER);

            RowConstraints row2 = new RowConstraints();
            row2.setMaxHeight(60);
            row2.setVgrow(Priority.NEVER);
            row2.setValignment(VPos.TOP);

            grid.getRowConstraints().addAll(row1, row2);
        }
        ColumnConstraints col0 = new ColumnConstraints();
        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        col1.setMinWidth(200);
        grid.getColumnConstraints().addAll(col0, col1, col2);
        grid.setMaxWidth(Region.USE_PREF_SIZE);
        grid.setPrefWidth(Region.USE_COMPUTED_SIZE);

        mainContentBox.getChildren().add(grid);

        Button nextBtn = new Button("Next");
        nextBtn.setOnAction(e -> {
            try {
                handleNext();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
        buttonsBox.getChildren().add(nextBtn);
    }

    private void handleNext() throws Exception {
        enteredScores.clear();
        for (TextField tf : scoreFields) {
            try {
                int sc = Integer.parseInt(tf.getText());
                if (sc < 0 || sc > 1_010_000) throw new NumberFormatException();
                enteredScores.add(sc);
            } catch (NumberFormatException ex) {
                showAlert("Invalid score", "Scores must be integers between 0 and 1 010 000", "entryProblemAlert");
                return;
            }
        }
        showSummary();
    }

    private void showSummary() {
        courseSelectionBox.setVisible(false);
        courseSelectionBox.setManaged(false);
        difficultyRadioButtonsBox.setVisible(false);
        difficultyRadioButtonsBox.setManaged(false);
        fileHandlerPane.setVisible(false);
        fileHandlerPane.setManaged(false);

        mainContentBox.getChildren().clear();
        buttonsBox.getChildren().clear();

        int totalScore = enteredScores.stream().mapToInt(Integer::intValue).sum();
        String rank = RankUtil.calculateCourseRank(totalScore, currentMusics.size());

        GridPane gridSummary = new GridPane();
        gridSummary.getStyleClass().add("scoresGridPane");
        VBox.setMargin(gridSummary, new Insets(10, 0, 0, 10));

        List<Music> allMusics = MusicsManager.getMusics();

        // Définir les colonnes
        for (int i = 0; i < 3; i++) {
            ColumnConstraints col = new ColumnConstraints();
            if (i == 0) {
                col.setPrefWidth(150); // Image
            } else if (i == 1) {
                col.setPrefWidth(200); // Infos musique
            } else {
                col.setPrefWidth(100); // Score / Rang
            }
            col.setHgrow(Priority.ALWAYS);
            gridSummary.getColumnConstraints().add(col);
        }

        for (int i = 0; i < 2; i++) {
            RowConstraints row = new RowConstraints();
            row.setVgrow(Priority.NEVER);
            row.setMinHeight(Region.USE_PREF_SIZE);
            gridSummary.getRowConstraints().add(row);
        }

        // Ligne 0 : titre de la course
        Label titleLabel = new Label(selectedCourse.getName());
        titleLabel.setWrapText(true);
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setMinHeight(20);
        titleLabel.getStyleClass().add("summaryTitleCourse");
        titleLabel.setPadding(new Insets(5, 0, 0, 0));

        StackPane borderedTitleCell = new StackPane(titleLabel);
        borderedTitleCell.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

        GridPane.setHalignment(borderedTitleCell, HPos.CENTER);
        GridPane.setColumnSpan(borderedTitleCell, 3);
        gridSummary.add(borderedTitleCell, 0, 0);

        // Ligne 1 : difficulté + date
        Label difficultyLabel = new Label(selectedDifficulty.toString());
        difficultyLabel.setWrapText(true);
        difficultyLabel.setAlignment(Pos.CENTER);
        difficultyLabel.setMinHeight(20);
        difficultyLabel.getStyleClass().add("summaryCourseIndicators");
        difficultyLabel.setPadding(new Insets(0, 0, 5, 0));

        StackPane borderedDiffLenCell = new StackPane(difficultyLabel);
        borderedDiffLenCell.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

        GridPane.setHalignment(borderedDiffLenCell, HPos.CENTER);
        GridPane.setColumnSpan(borderedDiffLenCell, 3);
        gridSummary.add(borderedDiffLenCell, 0, 1);

        // Récupération des musiques correspondantes
        List<Course.MusicDifficulty> diffs = selectedCourse.getDifficulties().get(selectedDifficulty).getDifficultyLevels();

        // Ligne 2 à n : chaque musique
        for (int i = 0; i < currentMusics.size(); i++) {
            Music music = currentMusics.get(i);
            int score = enteredScores.get(i);
            String scoreRank = RankUtil.calculateMusicRank(score);
            Course.MusicDifficulty diff = diffs.get(i);
            int lineIndex = i + 2;

            // Colonne 0 : Image
            String imageName = switch (diff){
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

            HBox imagePadding = new HBox(10);
            imagePadding.setPadding(new Insets(5));
            imagePadding.getStyleClass().add("summaryMusicCell");
            imagePadding.setMaxWidth(Double.MAX_VALUE);
            imagePadding.setAlignment(Pos.CENTER);
            imagePadding.getChildren().add(imageContainer);

            gridSummary.add(imagePadding, 0, lineIndex);

            // Colonne 1 : Infos musique

            // Title
            String title = switch (diff) {
                case BS -> music.getBsTitle();
                default -> music.getTitle();
            };
            Label musicTitleLabel = new Label(title);
            musicTitleLabel.setWrapText(true);
            musicTitleLabel.getStyleClass().add("musicTitle");
            musicTitleLabel.setMaxWidth(Double.MAX_VALUE);
            musicTitleLabel.setAlignment(Pos.CENTER);

            // Difficulty
            Double diffValue = switch (diff) {
                case OP -> music.getOpDiff();
                case MD -> music.getMdDiff();
                case FN -> music.getFnDiff();
                case EC -> music.getEcDiff();
                case BS -> music.getBsDiff();
            };
            Label musicDiffLabel = new Label(diff + " " + diffValue);
            musicDiffLabel.setWrapText(true);
            musicDiffLabel.getStyleClass().add("scoreLabel");
            musicDiffLabel.setMaxWidth(Double.MAX_VALUE);
            musicDiffLabel.setAlignment(Pos.CENTER);

            Label scoreLabel = new Label("Score : " + score);
            scoreLabel.getStyleClass().add("entryScore");
            scoreLabel.setAlignment(Pos.CENTER);

            Label rankLabel = new Label("Rank : " + scoreRank);
            rankLabel.getStyleClass().add("entryScore");
            rankLabel.setAlignment(Pos.CENTER);

            VBox infoBox = new VBox();
            infoBox.getChildren().addAll(musicTitleLabel, musicDiffLabel);
            infoBox.getStyleClass().add("summaryMusicCell");
            infoBox.setAlignment(Pos.CENTER);
            gridSummary.add(infoBox, 1, lineIndex);

            VBox scoreBox = new VBox();
            scoreBox.getChildren().addAll(scoreLabel, rankLabel);
            scoreBox.getStyleClass().add("summaryMusicCell");
            scoreBox.setAlignment(Pos.CENTER);
            gridSummary.add(scoreBox, 2, lineIndex);
        }

        // Ligne finale : score total et rang
        int lastRow = currentMusics.size() + 2;

        Label bottomLabel = new Label("Total Score : " + totalScore + " - " + rank);
        bottomLabel.setPadding(new Insets(5));
        bottomLabel.setMaxWidth(Double.MAX_VALUE);
        bottomLabel.setMaxHeight(Double.MAX_VALUE);
        bottomLabel.getStyleClass().add("summaryEntryScore");
        bottomLabel.setWrapText(true);
        bottomLabel.setAlignment(Pos.CENTER);

        GridPane.setColumnSpan(bottomLabel, 3);
        gridSummary.add(bottomLabel, 0, lastRow);
        gridSummary.setMaxWidth(700);
        gridSummary.setPrefWidth(700);

        HBox wrapper = new HBox(gridSummary);
        wrapper.setAlignment(Pos.CENTER);  // Centrage horizontal
        wrapper.setPadding(new Insets(20));

        mainContentBox.getChildren().add(wrapper);

        Button confirm = new Button("Confirm");
        Button redo = new Button("Modify");
        buttonsBox.getChildren().addAll(confirm, redo);

        confirm.setOnAction(e -> {
            try {
                handleConfirm(totalScore, rank);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            courseSelectionBox.setVisible(true);
            courseSelectionBox.setManaged(true);
            difficultyRadioButtonsBox.setVisible(true);
            difficultyRadioButtonsBox.setManaged(true);
            fileHandlerPane.setVisible(true);
            fileHandlerPane.setManaged(true);
        });
        redo.setOnAction(e -> {
            courseSelectionBox.setVisible(true);
            courseSelectionBox.setManaged(true);
            difficultyRadioButtonsBox.setVisible(true);
            difficultyRadioButtonsBox.setManaged(true);
            fileHandlerPane.setVisible(true);
            fileHandlerPane.setManaged(true);

            // Garde les anciennes valeurs dans les champs score :
            createScoreInputs();
            // Réinjecter les scores précédemment entrés si possible
            for (int i = 0; i < Math.min(enteredScores.size(), scoreFields.size()); i++) {
                scoreFields.get(i).setText(String.valueOf(enteredScores.get(i)));
            }
        });
    }

    private boolean isCourseInFile(String courseName, String filename) {
        try {
            List<Course> courses = objectMapper.readValue(new File(filename), new TypeReference<List<Course>>() {});
            for (Course course : courses) {
                if (course.getName().equals(courseName)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void handleConfirm(int total, String rank) throws Exception {
        try {
            List<ScoreEntry> savedScores = ScoreStorage.loadScores();

            List<Music> musics = selectedCourse.getDifficulties().get(selectedDifficulty).getMusics();
            List<Course.MusicDifficulty> diffs = selectedCourse.getDifficulties().get(selectedDifficulty).getDifficultyLevels();
            List<String> musicTitles = musics.stream().map(Music::getTitle).toList();
            List<String> individualRanks = enteredScores.stream()
                    .map(RankUtil::calculateMusicRank)
                    .toList();

            ScoreEntry newEntry = new ScoreEntry(
                    selectedCourse.getName(),
                    selectedDifficulty,
                    musicTitles,
                    diffs,
                    List.copyOf(enteredScores),
                    individualRanks,
                    total,
                    rank,
                    LocalDate.now()
            );

            savedScores.add(newEntry);
            ScoreStorage.saveScores(savedScores);

            Course.CourseDifficultySection section = selectedCourse.getDifficulties().get(selectedDifficulty);
            if (total > section.getBestScore()) {
                section.setBestScore(total);
                section.setBestRank(rank);

                Path dataDir = DataInitializer.getDataDirectory();
                Path coursesPath = dataDir.resolve("courses.json");
                Path legacyPath = dataDir.resolve("coursesLegacy.json");

                // Déterminer dans quel fichier écrire (courses.json ou coursesLegacy.json)
                String courseFile = isCourseInFile(selectedCourse.getName(), coursesPath.toString()) ? coursesPath.toString() : legacyPath.toString();

                // Charger les bonnes courses depuis ce fichier
                List<Course> courses = objectMapper.readValue(new File(courseFile), new TypeReference<List<Course>>() {});
                for (Course course : courses) {
                    if (course.getName().equals(selectedCourse.getName())) {
                        course.getDifficulties().get(selectedDifficulty).setBestScore(total);
                        course.getDifficulties().get(selectedDifficulty).setBestRank(rank);
                        break;
                    }
                }

                // Écrire les modifications dans le bon fichier
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(courseFile), courses);
            }

            showAlert("Done", "Score saved!\nTotal Score : " + total + "\nRank : " + rank, "entrySavedAlert");
        } catch (IOException ex) {
            showAlert("Save Error", "Could not save score to file:\n" + ex.getMessage(), "entryProblemAlert");
            ex.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        onCourseSelected();
        difficultyToggleGroup.selectToggle(null);
        mainController.refreshCourseDetails();
    }


    private void showAlert(String title, String msg, String styleClass) throws Exception {
        String theme = UserPreferences.getInstance().getTheme();

        Stage alertStage = new Stage();
        alertStage.initModality(Modality.APPLICATION_MODAL);
        alertStage.setTitle(title);
        alertStage.setResizable(false);

        // Texte
        Label messageLabel = new Label(msg);
        messageLabel.getStyleClass().add("alert-message");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(400);

        // Bouton centré
        Button okButton = new Button("OK");
        okButton.setOnAction(e -> alertStage.close());

        VBox centerVBox = new VBox(20, messageLabel, okButton);
        centerVBox.setAlignment(Pos.CENTER);
        centerVBox.setPadding(new Insets(20));

        // Image à droite (optionnelle)
        ImageView icon = null;
        try {
            String imagePath = switch (styleClass) {
                case "entryProblemAlert" -> "/images/alerts/" + theme + "Error.png";
                case "entrySavedAlert" -> "/images/alerts/" + theme + "Saved.png";
                default -> throw new IllegalStateException("Unexpected value: " + styleClass);
            };
            icon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath))));
            icon.setFitWidth(80);
            icon.setFitHeight(80);
        } catch (Exception ignored) {}

        // Conteneur principal avec superposition
        StackPane root = new StackPane();

        if (icon != null) {
            HBox iconContainer = new HBox(icon);
            iconContainer.setAlignment(Pos.BOTTOM_RIGHT);
            iconContainer.setPadding(new Insets(10));
            StackPane.setAlignment(iconContainer, Pos.BOTTOM_RIGHT);
            iconContainer.setMouseTransparent(true); // <<== permet l'interaction avec ce qui est en dessous
            root.getChildren().add(iconContainer);
        }

        root.getStyleClass().add(styleClass);
        root.getChildren().add(centerVBox); // bouton centré

        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/example/customcourses/styles/" + theme + "-theme.css")).toExternalForm());

        alertStage.setScene(scene);
        alertStage.showAndWait();
    }


    public void setCourse(Course courseToSelect, boolean isLegacy) {
        if (isLegacy) {
            coursesManager.loadLegacyCourses(MusicsManager.getMusics());
        } else {
            coursesManager.loadCourses(MusicsManager.getMusics());
        }

        courseCombo.getItems().setAll(coursesManager.getAllCourses());

        Optional<Course> courseInList = courseCombo.getItems().stream().filter(c -> c.getName().equals(courseToSelect.getName())).findFirst();

        if (courseInList.isPresent()) {
            courseCombo.getSelectionModel().select(courseInList.get());

            difficultyRadioButtonsBox.getChildren().clear();
            difficultyToggleGroup.getToggles().clear();

            selectedCourse = courseCombo.getValue();
            selectedDifficulty = null;

            clearScoreInputs();

            if (selectedCourse != null) {
                onCourseSelected();
            }
        } else {
            // La course n'existe pas dans la liste, on peut gérer ça (ex: ajout ou message d'erreur)
            System.out.println("Course not found in ComboBox items: " + courseToSelect.getName());
        }
    }

    public void setCourse(Course courseToSelect, Course.CourseDifficulty difficultyToSelect, boolean isLegacy) {
        if (isLegacy) {
            coursesManager.loadLegacyCourses(MusicsManager.getMusics());
        } else {
            coursesManager.loadCourses(MusicsManager.getMusics());
        }

        courseCombo.getItems().setAll(coursesManager.getAllCourses());

        Optional<Course> courseInList = courseCombo.getItems().stream().filter(c -> c.getName().equals(courseToSelect.getName())).findFirst();

        if (courseInList.isPresent()) {
            courseCombo.getSelectionModel().select(courseInList.get());

            difficultyRadioButtonsBox.getChildren().clear();
            difficultyToggleGroup.getToggles().clear();

            selectedCourse = courseCombo.getValue();
            selectedDifficulty = null;

            clearScoreInputs();

            if (selectedCourse != null) {
                onCourseSelected();  // va recréer les RadioButtons des difficultés

                // Sélectionner la difficulté demandée (après onCourseSelected)
                for (Toggle toggle : difficultyToggleGroup.getToggles()) {
                    if (toggle instanceof RadioButton rb && rb.getText().equalsIgnoreCase(difficultyToSelect.name())) {
                        difficultyToggleGroup.selectToggle(rb);
                        break;
                    }
                }
            }
        } else {
            // La course n'existe pas dans la liste, on peut gérer ça (ex: ajout ou message d'erreur)
            System.out.println("Course not found in ComboBox items: " + courseToSelect.getName());
        }
    }
}