package com.example.customcourses.controllers;

import com.example.customcourses.managers.MusicsManager;
import com.example.customcourses.models.Music;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Objects;

public class RandomMusicController {

    public ImageView coverImage;
    public Label titleLabel;
    public Label artistLabel;
    public Label difficultyLabel;
    public ScrollPane randomMusicPane;
    public VBox randomMusicContent;
    public HBox randomMusicSelected;
    @FXML private ComboBox<String> packCombo;
    @FXML private Spinner<Double> minDiffSpinner;
    @FXML private Spinner<Double> maxDiffSpinner;
    @FXML private Button selectButtonMusic;

    @FXML
    public void initialize() {
        // Charger les packs distincts depuis la liste des musiques
        packCombo.getItems().add("All");
        MusicsManager.getMusics().stream()
                .map(Music::getPack)
                .distinct()
                .sorted()
                .forEach(packCombo.getItems()::add);

        packCombo.getSelectionModel().selectFirst();

        minDiffSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 18.1, 5, 0.1));
        maxDiffSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 18.1, 15, 0.1));

        selectButtonMusic.setOnAction(e -> selectRandomMusic());
    }

    private void selectRandomMusic() {
        String pack = packCombo.getValue();
        if ("All".equals(pack)) pack = null;

        double minDiff = minDiffSpinner.getValue();
        double maxDiff = maxDiffSpinner.getValue();

        // Boucle pour éviter de sélectionner une musique dont la difficulté est null
        MusicsManager.MusicSelection selection = null;
        while (selection == null) {
            selection = MusicsManager.getRandomMusic(pack, minDiff, maxDiff);

            if (selection == null) break;

            Double diff = selection.difficultyValue();
            if (diff != null) break; // difficulté valide

            selection = null; // difficulté nulle → refaire un tirage
        }

        if (selection != null) {
            Music m = selection.music();

            // Mise à jour des labels
            String title = switch (selection.difficultyType()) {
                case "Backstage" -> m.getBsTitle();
                default -> m.getTitle();
            };

            String artist = switch (selection.difficultyType()) {
                case "Backstage" -> m.getBsArtist();
                default -> m.getArtist();
            };

            titleLabel.setText(title);
            artistLabel.setText(artist);
            difficultyLabel.setText(String.format("%s : %.1f", selection.difficultyType(), selection.difficultyValue()));

            // Choix de la bonne image selon la difficulté
            String imagePath = switch (selection.difficultyType()) {
                case "Backstage" -> "/covers/" + m.getBsImage();
                default -> "/covers/" + m.getImage();
            };

            // Charger l'image si disponible
            try {
                coverImage.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath))));
            } catch (Exception e) {
                coverImage.setImage(new Image(imagePath));
            }

        } else {
            // Réinitialisation si rien trouvé
            titleLabel.setText("Aucune musique trouvée");
            artistLabel.setText("");
            difficultyLabel.setText("");
            coverImage.setImage(null);
        }
    }

}
