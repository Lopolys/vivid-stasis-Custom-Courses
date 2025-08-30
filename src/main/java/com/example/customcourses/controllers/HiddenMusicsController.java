package com.example.customcourses.controllers;

import com.example.customcourses.models.Music;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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

public class HiddenMusicsController {

    public VBox musicDetailsPane;
    public SplitPane musicPane;
    public ScrollPane musicScrollPane;
    @FXML private TableView<Music> musicTable;
    @FXML private TableColumn<Music, String> titleColumn;
    @FXML private TableColumn<Music, Double> shDiffColumn;
    @FXML private TableColumn<Music, String> artistColumn;
    @FXML private TableColumn<Music, String> lengthColumn;

    private HiddenMainController mainController;
    private final ObservableList<Music> musicList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Initialiser les colonnes
        titleColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTitle()));
        shDiffColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getShDiff()).asObject());
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
        shDiffColumn.setMinWidth(40);
        shDiffColumn.setMaxWidth(200);

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

            String includeBoundary = "Boundary Shatter";
            return allMusics.stream().filter(music -> music.getPack() == null || music.getPack().equalsIgnoreCase(includeBoundary)).toList();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of(); // Liste vide si erreur
        }
    }

    private void showCover(String imageName, StackPane coverContainer) {
        coverContainer.getChildren().clear();

        if (imageName == null || imageName.isEmpty()) return;

        String path = "/covers/" + imageName;
        String lower = imageName.toLowerCase();

        try {
            ImageView imageView = new ImageView();
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(200);
            Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(path)));
            imageView.setImage(image);
            coverContainer.getChildren().add(imageView);
        } catch (Exception e) {

        }

    }

    private void showDetails(Music music) {
        GridPane grid = new GridPane();
        grid.getStyleClass().add("musicsGridPane");

        VBox.setMargin(grid, new Insets(10, 0, 0, 10));

        int columnCount = 4;

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

        // Titre des Backstages
        Label titleLabel = new Label(music.getTitle());
        titleLabel.setWrapText(true);
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setMinHeight(30);

        StackPane borderedBsTitleCell = new StackPane(titleLabel);
        borderedBsTitleCell.getStyleClass().addAll("musicGridCellTop", "musicTitle");
        borderedBsTitleCell.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

        GridPane.setHalignment(borderedBsTitleCell, HPos.CENTER);
        GridPane.setColumnSpan(borderedBsTitleCell, columnCount);
        grid.add(borderedBsTitleCell, 0, 0);

        for (int i = 1; i <= 4; i++) {
            RowConstraints row = new RowConstraints();
            row.setMinHeight(20);
            if(i > 1){
                row.setMinHeight(20);
                row.setMaxHeight(40);
                row.setVgrow(Priority.ALWAYS);
            }
            grid.getRowConstraints().add(row);
        }

        String[][] shatterData = {
                {"Difficulty", "SHATTER"},
                {"Level", String.valueOf(music.getShDiff())},
                {"Notes", String.valueOf(music.getShNotes())},
                {"Charter", music.getShCharter()},
        };

        for (int i = 1; i <= shatterData.length; i++) {
            for (int j = 0; j < columnCount - 1; j++) {
                if(j != 2){
                    Label cell = new Label(shatterData[i-1][j]);
                    cell.setWrapText(true);
                    cell.setMinHeight(20);
                    cell.setAlignment(Pos.CENTER);

                    StackPane borderedBsCell = new StackPane(cell);
                    borderedBsCell.getStyleClass().add("musicGridCell");
                    borderedBsCell.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

                    GridPane.setHalignment(borderedBsCell, HPos.CENTER);
                    if (j == 1)
                        GridPane.setColumnSpan(borderedBsCell, 2);
                    grid.add(borderedBsCell, j, i);
                }
            }
        }

        String imagePath = (music.getBsImage() != null) ? music.getBsImage() : music.getImage();

        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/covers/" + imagePath)));
        ImageView imageView = new ImageView(image);
        imageView.getStyleClass().add("musicGridRightCell");
        imageView.setFitWidth(200);
        imageView.setPreserveRatio(true);
        GridPane.setRowSpan(imageView, 4); // lignes 2–5
        grid.add(imageView, columnCount - 1, 1);

        String[][] shatterData2 = {
                {"Artist : ", music.getArtist(), "Jacket Artist : ", music.getJacketArtist(), "", ""},
                {"BPM : ", String.valueOf(music.getBpm()), "Length : ", music.getLength(), "", ""},
        };

        for (int row = 5; row <= 6; row++) {
            for (int col = 0; col < columnCount; col++) {
                Label label = new Label(shatterData2[row-5][col]);
                label.setWrapText(true);
                label.setMinHeight(30);

                StackPane borderedBsCell = new StackPane(label);
                if (row == 6) {
                    borderedBsCell.getStyleClass().add("musicGridCellBottom");
                } else if (col == columnCount - 1) {
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
                grid.add(borderedBsCell, col, row);
            }
        }

        musicDetailsPane.getChildren().clear();
        musicDetailsPane.getChildren().add(grid);
    }

    public void setMainController(HiddenMainController mainController) {
        this.mainController = mainController;
    }
}
