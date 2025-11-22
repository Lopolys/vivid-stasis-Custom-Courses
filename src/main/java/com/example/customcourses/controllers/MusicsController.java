package com.example.customcourses.controllers;

import com.example.customcourses.models.Music;
import com.example.customcourses.models.TitleUnlocker;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.example.customcourses.models.Title.loadUnlockedTitleIds;
import static com.example.customcourses.models.Title.unlockTitle;

public class MusicsController {

    public VBox musicDetailsPane;
    public SplitPane musicPane;
    public ScrollPane musicScrollPane;
    @FXML private TableView<Music> musicTable;
    @FXML private TableColumn<Music, String> titleColumn;
    @FXML private TableColumn<Music, Double> opDiffColumn;
    @FXML private TableColumn<Music, Double> mdDiffColumn;
    @FXML private TableColumn<Music, Double> fnDiffColumn;
    @FXML private TableColumn<Music, Double> ecDiffColumn;
    @FXML private TableColumn<Music, Double> bsDiffColumn;
    @FXML private TableColumn<Music, String> artistColumn;
    @FXML private TableColumn<Music, String> lengthColumn;

    private MainController mainController;
    private final ObservableList<Music> musicList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Initialiser les colonnes
        titleColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTitle()));
        opDiffColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getOpDiff()).asObject());
        mdDiffColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getMdDiff()).asObject());
        fnDiffColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getFnDiff()).asObject());
        ecDiffColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getEcDiff()).asObject());
        bsDiffColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getBsDiff()).asObject());
        artistColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getArtist()));
        lengthColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getLength()));
        musicTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                showDetails(newSelection);
            }
        });

        musicList.addAll(loadMusicFromJson());
        musicTable.setItems(musicList);
        musicTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        titleColumn.setMinWidth(120);
        titleColumn.setMaxWidth(600);
        artistColumn.setMinWidth(90);
        artistColumn.setMaxWidth(600);
        lengthColumn.setMinWidth(50);
        lengthColumn.setMaxWidth(100);
        opDiffColumn.setMinWidth(40);
        opDiffColumn.setMaxWidth(150);
        mdDiffColumn.setMinWidth(40);
        mdDiffColumn.setMaxWidth(100);
        fnDiffColumn.setMinWidth(40);
        fnDiffColumn.setMaxWidth(100);
        ecDiffColumn.setMinWidth(40);
        ecDiffColumn.setMaxWidth(100);
        bsDiffColumn.setMinWidth(40);
        bsDiffColumn.setMaxWidth(200);

        // Double clic pour afficher les détails
        musicTable.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() == 2 && musicTable.getSelectionModel().getSelectedItem() != null) {
                Music selectedMusic = musicTable.getSelectionModel().getSelectedItem();
                showDetails(selectedMusic);
            }
        });

        musicScrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            double deltaY = event.getDeltaY() * 3; // multiplier par un facteur pour accélérer le scroll
            musicScrollPane.setVvalue(musicScrollPane.getVvalue() - deltaY / musicScrollPane.getContent().getBoundsInLocal().getHeight());
            event.consume();  // Empêche le scroll normal pour appliquer le tien
        });
    }

    private List<Music> loadMusicFromJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream input = getClass().getResourceAsStream("/com/example/customcourses/json/musics.json");
            if (input == null) {
                throw new RuntimeException("Fichier musics.json introuvable");
            }

            List<Music> allMusics = mapper.readValue(input, new TypeReference<>() {});

            String excludeBoundary = "Boundary Shatter";
            return allMusics.stream().filter(music -> music.getPack() == null || !music.getPack().equalsIgnoreCase(excludeBoundary)).toList();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of(); // Liste vide si erreur
        }
    }

    private void showCover(String imageName, StackPane coverContainer) {
        coverContainer.getChildren().clear();

        if (imageName == null || imageName.isEmpty()) return;

        String path = "/covers/" + imageName;

        try {
            ImageView imageView = new ImageView();
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(200);
            Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(path)));
            imageView.setImage(image);
            coverContainer.getChildren().add(imageView);
        } catch (Exception e) {
            // Image non récupérée
        }
    }

    private void showDetails(Music music) {
        GridPane grid = new GridPane();
        grid.getStyleClass().add("musicsGridPane");
        VBox.setMargin(grid, new Insets(10, 0, 0, 10));

        int columnCount;

        String[][] data;

        //Vérifie que la musique a ou non une Encore

        if (music.getEcDiff() != 0.0) {
            columnCount = 6;
            //Rempli la matrice data avec les données de la musique avec une Encore
            data = new String[][]{
                    {"Difficulty", "Opening", "Middle", "Finale", "Encore"},
                    {"Level", String.valueOf(music.getOpDiff()), String.valueOf(music.getMdDiff()), String.valueOf(music.getFnDiff()), String.valueOf(music.getEcDiff())},
                    {"Notes", String.valueOf(music.getOpNotes()), String.valueOf(music.getMdNotes()), String.valueOf(music.getFnNotes()), String.valueOf(music.getEcNotes())},
                    {"Charter", music.getOpCharter(), music.getMdCharter(), music.getFnCharter(), music.getEcCharter()},
            };
        }
        else {
            columnCount = 5;
            //Rempli la matrice data avec les données de la musique sans Encore
            data = new String[][]{
                    {"Difficulty", "Opening", "Middle", "Finale"},
                    {"Level", String.valueOf(music.getOpDiff()), String.valueOf(music.getMdDiff()), String.valueOf(music.getFnDiff())},
                    {"Notes", String.valueOf(music.getOpNotes()), String.valueOf(music.getMdNotes()), String.valueOf(music.getFnNotes())},
                    {"Charter", music.getOpCharter(), music.getMdCharter(), music.getFnCharter()},
            };
        }

        //Homogénéisation des colonnes
        for (int i = 0; i < columnCount; i++) {
            ColumnConstraints col = new ColumnConstraints();
            if(i == columnCount - 1){
                col.setMinWidth(200);
                col.setMaxWidth(200);
            }
            else{
                col.setMaxWidth(120);
                col.setHgrow(Priority.ALWAYS);
            }
            grid.getColumnConstraints().add(col);
        }

        //Homogénéisation des lignes
        for (int i = 0; i <= 4; i++) {
            RowConstraints row = new RowConstraints();
            if (i==0) {
                row.setPrefHeight(30);
                row.setMinHeight(20);
                row.setMaxHeight(40);
                row.setVgrow(Priority.NEVER);
            } else {
                row.setPrefHeight(20);
                if (i > 1) {
                    row.setMinHeight(20);
                    row.setMaxHeight(40);
                    row.setVgrow(Priority.ALWAYS);
                }
            }
            grid.getRowConstraints().add(row);
        }

        // Titre des musiques
        Label titleLabel = new Label(music.getTitle());
        titleLabel.setWrapText(true);
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setMinHeight(20);

        StackPane borderedTitleCell = new StackPane(titleLabel);
        borderedTitleCell.getStyleClass().addAll("musicGridCellTop", "musicTitle");
        borderedTitleCell.setAlignment(Pos.CENTER);
        borderedTitleCell.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

        GridPane.setHalignment(borderedTitleCell, HPos.CENTER);
        GridPane.setValignment(borderedTitleCell, VPos.CENTER);
        GridPane.setColumnSpan(borderedTitleCell, columnCount);
        grid.add(borderedTitleCell, 0, 0);

        //Infos + image
        for (int i = 1; i <= data.length; i++) {
            for (int j = 0; j < columnCount - 1; j++) {
                Label cell = new Label(data[i-1][j]);
                cell.setWrapText(true);
                cell.setAlignment(Pos.CENTER);

                StackPane borderedCell = new StackPane(cell);
                borderedCell.getStyleClass().add("musicGridCell");

                GridPane.setHalignment(borderedCell, HPos.CENTER);
                grid.add(borderedCell, j, i);
            }
        }

        StackPane coverContainer = new StackPane();
        GridPane.setRowSpan(coverContainer, 6);
        grid.add(coverContainer, columnCount -1, 1);

        String[][] data2 = {
                {"Artist : ", music.getArtist(), "Jacket Artist : ", music.getJacketArtist(), "", ""},
                {"BPM : ", String.valueOf(music.getBpm()), "Length : ", music.getLength(), "", ""},
        };

        for (int row = 5; row <= 6; row++) {
            for (int col = 0; col < columnCount; col++) {
                Label label = new Label(data2[row-5][col]);
                label.setWrapText(true);
                label.setAlignment(Pos.CENTER);
                label.setMinHeight(30);

                StackPane borderedCell = new StackPane(label);
                if (!(col == columnCount - 1)) {
                    borderedCell.getStyleClass().add("musicGridCell");
                }
                borderedCell.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

                GridPane.setHalignment(borderedCell, HPos.CENTER);
                grid.add(borderedCell, col, row);
            }
        }

        Label footerLabel = new Label("Pack : " + music.getPack());
        footerLabel.setWrapText(true);
        footerLabel.setMinHeight(20);

        StackPane borderedFooterCell = new StackPane(footerLabel);
        borderedFooterCell.getStyleClass().add("musicGridCellBottomLarge");
        borderedFooterCell.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

        GridPane.setHalignment(borderedFooterCell, HPos.CENTER);
        GridPane.setColumnSpan(borderedFooterCell, columnCount);
        grid.add(borderedFooterCell, 0, 7);
        showCover(music.getImage(), coverContainer);

        if (music.getTitle().equalsIgnoreCase("thrinos;pygmalion")) {
            Button secretButton = new Button();
            secretButton.setOpacity(0.0); // bouton invisible
            secretButton.setPrefSize(50, 50); // zone cliquable raisonnable

            // positionne le bouton sur la jaquette
            StackPane.setAlignment(secretButton, Pos.CENTER);

            // Récupère l'image (il faut retrouver l'ImageView)
            ImageView imageView = (ImageView) coverContainer.getChildren().getFirst();

            secretButton.setOnAction(e -> {
                // Crée une animation de rotation
                javafx.animation.RotateTransition rotation = new javafx.animation.RotateTransition(javafx.util.Duration.seconds(2), imageView);
                rotation.setByAngle(360);
                rotation.setCycleCount(1);
                rotation.setAutoReverse(false);
                rotation.play();
                try {
                    Set<String> titlesList = loadUnlockedTitleIds();
                    if (!titlesList.contains("miscWild")) {
                        unlockTitle("miscWild");
                        TitleUnlocker.checkAndUnlockTitles();
                    }
                }
                catch (Exception e2){
                    // Pas de vérification de titre
                }
            });

            // Ajoute le bouton au-dessus de la jaquette
            coverContainer.getChildren().add(secretButton);
        }

        musicDetailsPane.getChildren().clear();
        musicDetailsPane.getChildren().add(grid);


        if (music.getBsDiff() != 0.0){
            GridPane bsGrid = new GridPane();
            bsGrid.getStyleClass().add("musicsGridPane");
            VBox.setMargin(bsGrid, new Insets(10, 0, 0, 10));

            int bsColumnCount = 4;

            for (int i = 0; i < bsColumnCount; i++) {
                ColumnConstraints col = new ColumnConstraints();
                if(i == bsColumnCount - 1){
                    col.setMinWidth(200);
                    col.setMaxWidth(200);
                }
                else{
                    col.setMaxWidth(120);
                    col.setHgrow(Priority.ALWAYS);
                }
                bsGrid.getColumnConstraints().add(col);
            }

            // Titre des Backstages
            Label bsTitleLabel = new Label(music.getBsTitle());
            bsTitleLabel.setWrapText(true);
            bsTitleLabel.setAlignment(Pos.CENTER);
            bsTitleLabel.setMinHeight(30);

            StackPane borderedBsTitleCell = new StackPane(bsTitleLabel);
            borderedBsTitleCell.getStyleClass().addAll("musicGridCellTop", "musicTitle");
            borderedBsTitleCell.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

            GridPane.setHalignment(borderedBsTitleCell, HPos.CENTER);
            GridPane.setColumnSpan(borderedBsTitleCell, bsColumnCount);
            bsGrid.add(borderedBsTitleCell, 0, 0);

            for (int i = 1; i <= 4; i++) {
                RowConstraints row = new RowConstraints();
                row.setMinHeight(20);
                if(i > 1){
                    row.setMinHeight(20);
                    row.setMaxHeight(40);
                    row.setVgrow(Priority.ALWAYS);
                }
                bsGrid.getRowConstraints().add(row);
            }

            String[][] backstageData = {
                    {"Difficulty", "Backstage"},
                    {"Level", String.valueOf(music.getBsDiff())},
                    {"Notes", String.valueOf(music.getBsNotes())},
                    {"Charter", music.getBsCharter()},
            };

            for (int i = 1; i <= backstageData.length; i++) {
                for (int j = 0; j < bsColumnCount - 1; j++) {
                    if(j != 2){
                        Label cell = new Label(backstageData[i-1][j]);
                        cell.setWrapText(true);
                        cell.setMinHeight(20);
                        cell.setAlignment(Pos.CENTER);

                        StackPane borderedBsCell = new StackPane(cell);
                        borderedBsCell.getStyleClass().add("musicGridCell");
                        borderedBsCell.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

                        GridPane.setHalignment(borderedBsCell, HPos.CENTER);
                        if (j == 1)
                            GridPane.setColumnSpan(borderedBsCell, 2);
                        bsGrid.add(borderedBsCell, j, i);
                    }
                }
            }

            String bsImagePath = (music.getBsImage() != null) ? music.getBsImage() : music.getImage();

            Image bsImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/covers/" + bsImagePath)));
            ImageView bsImageView = new ImageView(bsImage);
            bsImageView.getStyleClass().add("musicGridRightCell");
            bsImageView.setFitWidth(200);
            bsImageView.setPreserveRatio(true);
            GridPane.setRowSpan(bsImageView, 4); // lignes 2–5
            bsGrid.add(bsImageView, bsColumnCount - 1, 1);

            String[][] backstageData2 = {
                    {"Artist : ", music.getBsArtist(), "Jacket Artist : ", music.getBsJacketArtist(), "", ""},
                    {"BPM : ", String.valueOf(music.getBsBpm()), "Length : ", music.getBsLength(), "", ""},
            };

            for (int row = 5; row <= 6; row++) {
                for (int col = 0; col < bsColumnCount; col++) {
                    Label label = new Label(backstageData2[row-5][col]);
                    label.setWrapText(true);
                    label.setMinHeight(30);

                    StackPane borderedBsCell = new StackPane(label);
                    if (row == 6) {
                        borderedBsCell.getStyleClass().add("musicGridCellBottom");
                    } else if (col == bsColumnCount - 1) {
                        borderedBsCell.getStyleClass().add("musicGridRightCell");
                    } else {
                        borderedBsCell.getStyleClass().add("musicGridCell");
                    }
                    borderedBsCell.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

                    GridPane.setHalignment(borderedBsCell, HPos.CENTER);
                    if(col==0 && row==6){
                        borderedBsCell.getStyleClass().add("musicGridCellBottomLeft");
                    } else if (col==3 && row==6){
                        borderedBsCell.getStyleClass().add("musicGridCellBottomRight");
                    }
                    bsGrid.add(borderedBsCell, col, row);
                }
            }
            musicDetailsPane.getChildren().add(bsGrid);
        }
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
}
