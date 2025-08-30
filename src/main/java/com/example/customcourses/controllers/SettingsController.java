package com.example.customcourses.controllers;

import com.example.customcourses.utils.UserPreferences;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Objects;
import javafx.scene.text.Font;
import java.io.InputStream;

public class SettingsController {

    public VBox settingsBox;
    @FXML public ComboBox<String> themeComboBox;
    @FXML private ComboBox<String> fontComboBox;

    private Scene scene;

    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void initialize() throws Exception {
        themeComboBox.getItems().addAll("Saturday", "Allison", "Eri", "Kotomi", "Chiyo", "Tsuki", "Dawn", "Storyteller", "Kanshi", "Tori", "Miri");

        // Charger les polices dans la ComboBox
        fontComboBox.getItems().addAll("Arial", "Calibri", "Comic Sans MS", "Courier New", "Liberation Serif", "Times New Roman", "Verdana");

        // Charger les préférences sauvegardées et les appliquer dans les contrôles
        UserPreferences prefs = UserPreferences.getInstance();

        fontComboBox.getSelectionModel().select(prefs.getSelectedFont());

        String savedTheme = prefs.getTheme();
        String themeLabel = mapThemeCodeToLabel(savedTheme);
        themeComboBox.getSelectionModel().select(themeLabel);

        // Listeners pour les ComboBox
        themeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                applyTheme();
                try {
                    savePreferencesAndApply();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        // Ecoute changement de la police
        fontComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                applyFont();
                try {
                    savePreferencesAndApply();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void savePreferencesAndApply() throws Exception {
        UserPreferences prefs = UserPreferences.getInstance();

        String themeLabel = themeComboBox.getValue();
        prefs.setTheme(mapLabelToThemeCode(themeLabel));

        prefs.setSelectedFont(fontComboBox.getValue());
        prefs.saveToFile();

        if (mainController != null) {
            mainController.applyUserPreferences();
        }
    }

    private void applyTheme() {
        if (scene == null) return;

        scene.getStylesheets().clear();

        String selectedLabel = themeComboBox.getValue();
        if (selectedLabel != null) {
            String themeCode = mapLabelToThemeCode(selectedLabel);
            String themeCss = "/com/example/customcourses/styles/" + themeCode + "-theme.css";
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(themeCss)).toExternalForm());
        }
    }


    private void applyFont() {
        if (scene == null) return;

        String font = fontComboBox.getValue();
        scene.getRoot().setStyle("-fx-font-family: '" + font + "';");
    }

    @FXML
    private void handleExportData() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter les données");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers texte", "*.txt"));
        Stage stage = new Stage();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            System.out.println("Exporter vers : " + file.getAbsolutePath());
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Export effectué vers : " + file.getAbsolutePath(), ButtonType.OK);
            alert.showAndWait();
        }
    }

    private String mapLabelToThemeCode(String label) {
        return switch (label) {
            case "Allison" -> "alli";
            case "Eri" -> "eri";
            case "Kotomi" -> "kotomi";
            case "Chiyo" -> "chiyo";
            case "Tsuki" -> "tsuki";
            case "Dawn" -> "dawn";
            case "Storyteller" -> "story";
            case "Kanshi" -> "kanshi";
            case "Tori" -> "tori";
            case "Miri" -> "miri";
            default -> "sat";
        };
    }

    private String mapThemeCodeToLabel(String code) {
        return switch (code) {
            case "alli" -> "Allison";
            case "eri" -> "Eri";
            case "kotomi" -> "Kotomi";
            case "chiyo" -> "Chiyo";
            case "tsuki" -> "Tsuki";
            case "dawn" -> "Dawn";
            case "story" -> "Storyteller";
            case "kanshi" -> "Kanshi";
            case "tori" -> "Tori";
            case "miri" -> "Miri";
            default -> "Saturday";
        };
    }

    public void loadCustomFont(String fontResourcePath) {
        InputStream fontStream = getClass().getResourceAsStream(fontResourcePath);
        if (fontStream != null) {
            Font font = Font.loadFont(fontStream, 14); // 14 = taille par défaut
            if (font != null) {
                System.out.println("Police chargée : " + font.getName());
                fontComboBox.getItems().add(font.getName());
            } else {
                System.out.println("Impossible de charger la police depuis : " + fontResourcePath);
            }
        } else {
            System.out.println("Fichier de police introuvable : " + fontResourcePath);
        }
    }
}
