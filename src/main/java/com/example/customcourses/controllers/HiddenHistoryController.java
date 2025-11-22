package com.example.customcourses.controllers;

import com.example.customcourses.managers.MusicsManager;
import com.example.customcourses.models.Course;
import com.example.customcourses.models.Music;
import com.example.customcourses.models.ScoreEntry;
import com.example.customcourses.utils.RankUtil;
import com.example.customcourses.utils.ScoreExporter;
import com.example.customcourses.utils.ScoreStorage;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class HiddenHistoryController {

    public ScrollPane detailsScrollPane;
    public HBox historyPane;
    public HBox filterBox;
    @FXML private TableView<ScoreEntry> courseHistoryTable;
    @FXML private TableColumn<ScoreEntry, String> courseNameColumn;
    @FXML private TableColumn<ScoreEntry, String> difficultyColumn;
    @FXML private TableColumn<ScoreEntry, String> dateColumn;
    @FXML private TableColumn<ScoreEntry, Integer> scoreColumn;
    @FXML private TableColumn<ScoreEntry, String> rankColumn;
    @FXML private TableColumn<ScoreEntry, Void> detailsColumn;

    @FXML private VBox scoresDetailsPane;

    @FXML private ComboBox<String> monthFilter;
    @FXML private ComboBox<String> courseFilterBox;

    private FilteredList<ScoreEntry> filteredScores;

    private HiddenMainController mainController;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @FXML
    public void initialize() {
        setupColumns();
        setupFilters();
        loadScoresIntoTable();
        filterBox.setPadding(new Insets(0, 10, 5, 10));

        courseHistoryTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        courseNameColumn.setMinWidth(200);
        difficultyColumn.setMinWidth(35);
        dateColumn.setMinWidth(25);
        scoreColumn.setMinWidth(20);
        rankColumn.setMinWidth(2);
        detailsColumn.setMinWidth(100);

        detailsScrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            double deltaY = event.getDeltaY() * 3; // multiplier par un facteur pour accélérer le scroll
            detailsScrollPane.setVvalue(detailsScrollPane.getVvalue() - deltaY / detailsScrollPane.getContent().getBoundsInLocal().getHeight());
            event.consume();  // Empêche le scroll normal
        });
    }

    private void setupColumns() {
        courseNameColumn.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getCourseName()));
        difficultyColumn.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getDifficulty().name()));
        dateColumn.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getDate().format(formatter)));
        scoreColumn.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().getTotalScore()));
        rankColumn.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getTotalRank()));

        // Centrage du texte pour toutes les colonnes simples
        centerTextCells(courseNameColumn);
        centerTextCells(difficultyColumn);
        centerTextCells(dateColumn);
        centerTextCells(scoreColumn);
        centerTextCells(rankColumn);

        rankColumn.setComparator((r1, r2) -> {
            List<String> rankOrder = Arrays.asList("VS", "V+", "V", "SS+", "SS", "S+", "S", "AA", "A", "B", "C", "D", "E");
            int i1 = rankOrder.indexOf(r1);
            int i2 = rankOrder.indexOf(r2);
            return Integer.compare(i1, i2);
        });

        // Centrage du bouton dans la colonne "Détails"
        detailsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Details");

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    btn.setOnAction(event -> {
                        ScoreEntry score = (ScoreEntry) getTableRow().getItem();
                        if (score != null) {
                            courseHistoryTable.getSelectionModel().select(score);

                            showDetails(score);
                            scoresDetailsPane.setVisible(true);
                        }
                    });
                    setGraphic(btn);
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    private <T> void centerTextCells(TableColumn<ScoreEntry, T> column) {
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                    setAlignment(Pos.CENTER_LEFT);
                }
            }
        });
    }

    private Set<String> loadAllCourseNames() {
        ObjectMapper mapper = new ObjectMapper();
        Set<String> courseNames = new TreeSet<>();

        // Liste des fichiers à lire
        String[] file = {"/com/example/customcourses/json/hiddenCourses.json"};

        for (String filePath : file) {
            try (InputStream is = getClass().getResourceAsStream(filePath)) {
                if (is == null) {
                    System.err.println(filePath + " introuvable !");
                    continue;
                }
                List<Course> courses = Arrays.asList(mapper.readValue(is, Course[].class));
                courses.stream()
                        .map(Course::getName)
                        .filter(Objects::nonNull)
                        .forEach(courseNames::add);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return courseNames;
    }

    private void setupFilters() {
        monthFilter.setOnAction(e -> applyFilters());

        Set<String> courseNames = loadAllCourseNames();
        courseFilterBox.getItems().add("Every Courses");
        courseFilterBox.getItems().addAll(courseNames);
        courseFilterBox.getSelectionModel().selectFirst();

        courseFilterBox.setOnAction(e -> applyFilters());
    }

    private void loadScoresIntoTable() {
        try {
            List<ScoreEntry> scores = ScoreStorage.loadHiddenScores();
            Collections.reverse(scores);
            filteredScores = new FilteredList<>(FXCollections.observableArrayList(scores), p -> true);

            SortedList<ScoreEntry> sortedScores = new SortedList<>(filteredScores);
            sortedScores.comparatorProperty().bind(courseHistoryTable.comparatorProperty());
            courseHistoryTable.setItems(sortedScores);


            VBox.setVgrow(courseHistoryTable, Priority.ALWAYS);

            populateMonthFilter(scores);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void populateMonthFilter(List<ScoreEntry> scores) {
        Set<String> months = scores.stream()
                .map(score -> String.format("%02d", score.getDate().getMonthValue()) + "-" + score.getDate().getYear())
                .collect(Collectors.toCollection(TreeSet::new));

        monthFilter.getItems().add("Every Month");
        monthFilter.getItems().addAll(months);
        monthFilter.getSelectionModel().selectFirst();
    }

    private void applyFilters() {
        String selectedMonth = monthFilter.getSelectionModel().getSelectedItem();
        String selectedCourse = courseFilterBox.getValue();

        filteredScores.setPredicate(score -> {
            boolean matchesMonth = selectedMonth == null || selectedMonth.equals("Every Month") ||
                    (String.format("%02d", score.getDate().getMonthValue()) + "-" + score.getDate().getYear()).equals(selectedMonth);

            boolean matchesCourse = selectedCourse == null || selectedCourse.equals("Every Courses") ||
                    score.getCourseName().equalsIgnoreCase(selectedCourse);

            return matchesMonth && matchesCourse;
        });

        scoresDetailsPane.getChildren().clear();
    }

    private void showDetails(ScoreEntry score) {
        GridPane grid = new GridPane();
        grid.getStyleClass().add("scoresGridPane");
        VBox.setMargin(grid, new Insets(10, 0, 0, 10));

        List<Music> allMusics = MusicsManager.getMusics();

        // Définir les colonnes
        for (int i = 0; i < 4; i++) {
            ColumnConstraints col = new ColumnConstraints();
            if (i == 0) {
                col.setPrefWidth(150); // Image
            } else if (i == 1) {
                col.setPrefWidth(200); // Infos musique
            } else {
                col.setPrefWidth(100); // Score / Rang
            }
            col.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(col);
        }

        for (int i = 0; i < 2; i++) {
            RowConstraints row = new RowConstraints();
            row.setVgrow(Priority.NEVER);
            row.setMinHeight(Region.USE_PREF_SIZE);
            grid.getRowConstraints().add(row);
        }

        // Ligne 0 : titre de la course
        Label titleLabel = new Label(score.getCourseName());
        titleLabel.setWrapText(true);
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setMinHeight(20);
        titleLabel.getStyleClass().add("historyTitleCourse");
        titleLabel.setPadding(new Insets(5, 0, 0, 0));

        StackPane borderedTitleCell = new StackPane(titleLabel);
        borderedTitleCell.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

        GridPane.setHalignment(borderedTitleCell, HPos.CENTER);
        GridPane.setColumnSpan(borderedTitleCell, 4);
        grid.add(borderedTitleCell, 0, 0);

        // Ligne 1 : difficulté + date
        Label difficultyLabel = new Label(score.getDifficulty() + " - " + score.getDate().format(formatter));
        difficultyLabel.setWrapText(true);
        difficultyLabel.setAlignment(Pos.CENTER);
        difficultyLabel.setMinHeight(20);
        difficultyLabel.getStyleClass().add("historyCourseIndicators");
        difficultyLabel.setPadding(new Insets(0, 0, 5, 0));

        StackPane borderedDiffLenCell = new StackPane(difficultyLabel);
        borderedDiffLenCell.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

        GridPane.setHalignment(borderedDiffLenCell, HPos.CENTER);
        GridPane.setColumnSpan(borderedDiffLenCell, 4);
        grid.add(borderedDiffLenCell, 0, 1);

        // Récupération des musiques correspondantes
        List<Music> musicList = new ArrayList<>();
        for (String title : score.getMusicTitles()) {
            allMusics.stream()
                    .filter(m -> m.getTitle().equalsIgnoreCase(title))
                    .findFirst()
                    .ifPresent(musicList::add);
        }

        // Ligne 2 à n : chaque musique
        for (int i = 0; i < musicList.size(); i++) {
            Music music = musicList.get(i);
            Course.MusicDifficulty diff = score.getDifficultyLevels().get(i);
            int lineIndex = i + 2;

            // Colonne 0 : Image
            String imageName = switch (diff){
                case BS -> music.getBsImage();
                default -> music.getImage();
            };

            StackPane imageContainer = new StackPane();
            String imagePath = "/covers/" + imageName;

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
            imagePadding.getStyleClass().add("historyMusicCell");
            imagePadding.setMaxWidth(Double.MAX_VALUE);
            imagePadding.setAlignment(Pos.CENTER);
            imagePadding.getChildren().add(imageContainer);

            grid.add(imagePadding, 0, lineIndex);

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
            musicTitleLabel.setTextAlignment(TextAlignment.CENTER);
            StackPane musicTitleWrapper = new StackPane(musicTitleLabel);
            musicTitleWrapper.setAlignment(Pos.CENTER);

            // Difficulty
            Double diffValue = switch (diff) {
                case OP -> music.getOpDiff();
                case MD -> music.getMdDiff();
                case FN -> music.getFnDiff();
                case EC -> music.getEcDiff();
                case BS -> music.getBsDiff();
                case SH -> music.getShDiff();
            };
            Label musicDiffLabel = new Label(diff + " " + diffValue);
            musicDiffLabel.setWrapText(true);
            musicDiffLabel.getStyleClass().add("historyLabel");
            musicDiffLabel.setMaxWidth(Double.MAX_VALUE);
            musicDiffLabel.setAlignment(Pos.CENTER);
            musicDiffLabel.setTextAlignment(TextAlignment.CENTER);
            StackPane musicDiffWrapper = new StackPane(musicDiffLabel);
            musicDiffWrapper.setAlignment(Pos.CENTER);

            // Length
            String length = switch (diff) {
                case BS -> music.getBsLength();
                default -> music.getLength();
            };
            Label lengthLabel = new Label(length);
            lengthLabel.setWrapText(true);
            lengthLabel.getStyleClass().add("historyLabel");
            lengthLabel.setMaxWidth(Double.MAX_VALUE);
            lengthLabel.setAlignment(Pos.CENTER);
            lengthLabel.setTextAlignment(TextAlignment.CENTER);
            StackPane lengthWrapper = new StackPane(lengthLabel);
            lengthWrapper.setAlignment(Pos.CENTER);

            // Charter
            String charter = switch (diff) {
                case OP -> music.getOpCharter();
                case MD -> music.getMdCharter();
                case FN -> music.getFnCharter();
                case EC -> music.getEcCharter();
                case BS -> music.getBsCharter();
                case SH -> music.getShCharter();
            };
            Label charterLabel = new Label(charter);
            charterLabel.setWrapText(true);
            charterLabel.getStyleClass().add("historyLabel");
            charterLabel.setMaxWidth(Double.MAX_VALUE);
            charterLabel.setAlignment(Pos.CENTER);
            charterLabel.setTextAlignment(TextAlignment.CENTER);
            StackPane charterWrapper = new StackPane(charterLabel);
            charterWrapper.setAlignment(Pos.CENTER);

            VBox infoBox = new VBox();
            infoBox.getChildren().addAll(musicTitleWrapper, musicDiffWrapper, charterWrapper, lengthWrapper);
            infoBox.getStyleClass().add("historyMusicCell");
            infoBox.setAlignment(Pos.CENTER);
            grid.add(infoBox, 1, lineIndex);

            // Colonne 2 : Score
            Label scoreLabel = new Label(String.valueOf(score.getIndividualScores().get(i)));
            scoreLabel.getStyleClass().add("historyScore");
            scoreLabel.setAlignment(Pos.CENTER);

            StackPane borderedScoreCell = new StackPane(scoreLabel);
            borderedScoreCell.getStyleClass().add("historyMusicCell");
            borderedScoreCell.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

            GridPane.setHalignment(borderedScoreCell, HPos.CENTER);
            grid.add(borderedScoreCell, 2, lineIndex);

            // Colonne 3 : Rank
            String rank = score.getIndividualRanks().get(i);
            ImageView rankView = new ImageView(RankUtil.getRankImage(rank));
            rankView.setFitHeight(50);
            rankView.setPreserveRatio(true);

            StackPane borderedRankCell = new StackPane(rankView);
            borderedRankCell.getStyleClass().add("historyMusicCell");
            borderedRankCell.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

            GridPane.setHalignment(borderedRankCell, HPos.CENTER);
            grid.add(borderedRankCell, 3, lineIndex);
        }

        // Ligne finale : score total et rang
        int lastRow = musicList.size() + 2;

        Label bottomLabel = new Label("Total Score : " + score.getTotalScore() + " - ");
        bottomLabel.setPadding(new Insets(5));
        bottomLabel.setMaxWidth(Double.MAX_VALUE);
        bottomLabel.setMaxHeight(Double.MAX_VALUE);
        bottomLabel.getStyleClass().add("summaryScoreLabel");
        bottomLabel.setWrapText(true);
        bottomLabel.setAlignment(Pos.CENTER);

        String rank = score.getTotalRank();
        ImageView rankView = new ImageView(RankUtil.getRankImage(rank));
        rankView.setFitHeight(50);
        rankView.setPreserveRatio(true);

        StackPane borderedRankCell = new StackPane(rankView);
        borderedRankCell.setPadding(new Insets(5, 0, 5, 0));
        borderedRankCell.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

        HBox bottomContent = new HBox();
        bottomContent.getChildren().addAll(bottomLabel, borderedRankCell);
        bottomContent.getStyleClass().add("summaryHistoryScore");
        bottomContent.setAlignment(Pos.CENTER);

        GridPane.setColumnSpan(bottomContent, 4);
        GridPane.setHalignment(bottomContent, HPos.CENTER);
        grid.add(bottomContent, 0, lastRow);
        grid.setMaxWidth(700);
        grid.setPrefWidth(700);

        HBox wrapper = new HBox(grid);
        wrapper.setAlignment(Pos.CENTER);  // Centrage horizontal
        wrapper.setPadding(new Insets(20));

        Button exportButton = new Button("Export Score");
        List<Music> scoreMusicList = new ArrayList<>();
        for (String title : score.getMusicTitles()) {
            MusicsManager.getMusics().stream().filter(m -> m.getTitle().equalsIgnoreCase(title)).findFirst().ifPresent(scoreMusicList::add);
        }
        exportButton.setOnAction(e -> {
            try {
                ScoreExporter.exportScore((Stage) exportButton.getScene().getWindow(), score, scoreMusicList);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        VBox finalContainer = new VBox(wrapper, exportButton);
        finalContainer.setAlignment(Pos.CENTER);
        finalContainer.setSpacing(5);

        scoresDetailsPane.getChildren().clear();
        scoresDetailsPane.getChildren().add(finalContainer);
    }

    public void setMainController(HiddenMainController mainController) {
        this.mainController = mainController;
    }
}
