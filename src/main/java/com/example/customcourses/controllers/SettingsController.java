package com.example.customcourses.controllers;

import com.example.customcourses.models.Title;
import com.example.customcourses.models.TitleUnlocker;
import com.example.customcourses.utils.UserPreferences;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.*;
import java.io.InputStream;

import static com.example.customcourses.models.Title.*;

public class SettingsController {

    @FXML private VBox settingsBox;
    @FXML private ComboBox<String> themeComboBox;
    @FXML private ComboBox<String> fontComboBox;
    @FXML private VBox titlesContainer;

    private TextField nameField;
    private Label currentTitleName;
    private Label currentTitleDescription;
    private ImageView currentTitleImage;
    private Button chooseTitleButton;

    private Scene scene;
    private MainController mainController;
    private List<Title> titles;
    private int currentTitleIndex = 0;

    private VBox titleList;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void initialize() throws Exception {
        setupThemeAndFontSection();
        setupUserNameSection();
        setupTitlesSection();
        refreshTitlesDisplay();
    }

    private void setupThemeAndFontSection() throws Exception {
        themeComboBox.getItems().addAll("Saturday", "Allison", "Eri", "Kotomi", "Chiyo", "Tsuki", "Dawn", "Storyteller", "Kanshi", "Tori", "Miri");
        fontComboBox.getItems().addAll("Arial", "Calibri", "Comic Sans MS", "Courier New", "Liberation Serif", "Times New Roman", "Verdana");

        UserPreferences prefs = UserPreferences.getInstance();

        fontComboBox.getSelectionModel().select(prefs.getSelectedFont());
        themeComboBox.getSelectionModel().select(mapThemeCodeToLabel(prefs.getTheme()));

        themeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                try {
                    savePreferencesAndApply();
                    TitleUnlocker.checkAndUnlockTitles();
                    refreshTitlesDisplay();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        fontComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) try { savePreferencesAndApply(); } catch (Exception e) { e.printStackTrace(); }
        });
    }

    private void setupUserNameSection() throws Exception {
        Label nameLabel = new Label("Username");
        nameLabel.getStyleClass().add("settingsLabel");

        nameField = new TextField();
        nameField.setPromptText("Enter a surname");

        Button saveNameButton = new Button("Save");
        saveNameButton.setOnAction(e -> {
            try {
                UserPreferences prefs = UserPreferences.getInstance();
                prefs.setUserName(nameField.getText());
                prefs.saveToFile();

                TitleUnlocker.checkAndUnlockTitles();
                refreshTitlesDisplay();
            } catch (Exception ex) {
                // Préférences non sauvegardées
            }
        });

        // Charger nom existant
        UserPreferences prefs = UserPreferences.getInstance();
        if (prefs.getUserName() != null) nameField.setText(prefs.getUserName());

        settingsBox.getChildren().addAll(nameLabel, nameField, saveNameButton);
    }

    private void setupTitlesSection() throws Exception {
        titles = Title.loadDisplayableTitles();

        HBox navBox = new HBox(20);
        navBox.setAlignment(Pos.CENTER);
        navBox.setPadding(new Insets(5));

        Button prevButton = new Button("◀");
        prevButton.setPrefHeight(150);
        prevButton.setOnAction(e -> showPreviousTitle());
        prevButton.getStyleClass().add("titleBoxNavButton");

        currentTitleImage = new ImageView();
        currentTitleImage.setFitWidth(500);
        currentTitleImage.setFitHeight(180);
        currentTitleImage.setPreserveRatio(true);

        currentTitleName = new Label();
        currentTitleName.getStyleClass().add("settingsLabel");

        currentTitleDescription = new Label();
        currentTitleDescription.setWrapText(true);
        currentTitleDescription.setMaxWidth(400);
        currentTitleDescription.setAlignment(Pos.CENTER);
        currentTitleDescription.setTextAlignment(TextAlignment.CENTER);

        chooseTitleButton = new Button("Choose this title");
        chooseTitleButton.setOnAction(e -> chooseCurrentTitle());

        StackPane centerBox = new StackPane();

        StackPane.setAlignment(currentTitleImage, Pos.TOP_CENTER);

        VBox textContainer = new VBox(10);
        textContainer.setAlignment(Pos.TOP_CENTER);

        currentTitleName.setMaxWidth(Double.MAX_VALUE);
        currentTitleName.setAlignment(Pos.CENTER);

        VBox.setVgrow(currentTitleDescription, Priority.ALWAYS);

        textContainer.getChildren().addAll(currentTitleName, currentTitleDescription);

        StackPane.setAlignment(textContainer, Pos.CENTER);
        StackPane.setMargin(textContainer, new Insets(50, 0, 0, 0));

        StackPane.setAlignment(chooseTitleButton,Pos.BOTTOM_CENTER);

        centerBox.getChildren().addAll(currentTitleImage, textContainer, chooseTitleButton);

        Button nextButton = new Button("▶");
        nextButton.setPrefHeight(150);
        nextButton.setOnAction(e -> showNextTitle());
        nextButton.getStyleClass().add("titleBoxNavButton");

        navBox.getChildren().addAll(prevButton, centerBox, nextButton);
        navBox.getStyleClass().add("titleBox");

        titlesContainer.setAlignment(Pos.TOP_CENTER);

        titleList = new VBox();
        titleList.setFillWidth(true);
        titleList.getStyleClass().add("titleListBox");

        ScrollPane scroll = new ScrollPane(titleList);
        scroll.setPadding(new Insets(5));
        scroll.setFitToWidth(true);
        scroll.setMaxHeight(250);
        scroll.getStyleClass().add("titleListPane");

        titlesContainer.getChildren().addAll(navBox, scroll);

        updateTitleDisplay();
        refreshTitlesList();
    }

    private void updateTitleDisplay() {
        if (titles == null || titles.isEmpty()) return;

        Title current = titles.get(currentTitleIndex);

        try {
            InputStream imgStream = getClass().getResourceAsStream(current.getImagePath());
            if (imgStream != null) {
                currentTitleImage.setImage(new Image(imgStream));
            } else {
                System.err.println("Image missing : " + current.getImagePath());
                currentTitleImage.setImage(null);
            }
        } catch (Exception e) {
            System.err.println("Image loading error : " + e.getMessage());
        }

        currentTitleName.setText(current.getName());
        currentTitleDescription.setText(current.getDescription());

        try {
            UserPreferences prefs = UserPreferences.getInstance();
            String selectedTitle = prefs.getSelectedTitle();

            boolean isUnlocked = current.isUnlocked();
            boolean isAlreadySelected = selectedTitle != null && selectedTitle.equals(current.getName());

            chooseTitleButton.setDisable(!isUnlocked || isAlreadySelected);
            chooseTitleButton.setText(isAlreadySelected ? "Selected" : "Choose this title");
        } catch (Exception e) {
            chooseTitleButton.setDisable(true);
        }
    }

    private void showNextTitle() {
        if (currentTitleIndex < titles.size() - 1) {
            currentTitleIndex++;
            updateTitleDisplay();
        } else if (currentTitleIndex == titles.size() - 1){
            currentTitleIndex = 0;
            updateTitleDisplay();
        }
    }

    private void showPreviousTitle() {
        if (currentTitleIndex > 0) {
            currentTitleIndex--;
            updateTitleDisplay();
        }else if (currentTitleIndex == 0){
            currentTitleIndex = titles.size() - 1;
            updateTitleDisplay();
        }
    }

    private void chooseCurrentTitle() {
        Title current = titles.get(currentTitleIndex);
        if (!current.isUnlocked()) return;

        try {
            UserPreferences prefs = UserPreferences.getInstance();
            prefs.setSelectedTitle(current.getName());
            prefs.saveToFile();

            updateTitleDisplay();
            refreshTitlesList();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refreshTitlesDisplay() {
        try {
            titlesContainer.getChildren().clear();

            titles = Title.loadDisplayableTitles();
            Title t = getSelectedTitle();

            int id = 0;
            for (int j = 0; j < titles.size(); j++) {
                try{
                    if (t.getName() != null){
                        if (titles.get(j).getName().equals(t.getName())) {
                            id = j;
                            break;
                        }
                    }
                } catch (NullPointerException _){
                }
            }
            currentTitleIndex = id;
            setupTitlesSection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshTitlesList() throws Exception {
        titleList.getChildren().clear();
        List<Title> allTitles = loadAllTitles();

        UserPreferences prefs;
        String selectedTitle = null;

        try {
            prefs = UserPreferences.getInstance();
            selectedTitle = prefs.getSelectedTitle();
        } catch (Exception _) {
        }

        for (int i = 0; i < titles.size(); i++) {
            Title t = titles.get(i);

            int id = -1;
            for (int j = 0; j < allTitles.size(); j++) {
                if (allTitles.get(j).getName().equals(t.getName())) {
                    id = j;
                    break;
                }
            }

            HBox line = new HBox(10);
            line.setAlignment(Pos.CENTER_LEFT);
            line.setPadding(new Insets(5));

            Label idLabel = new Label("#" + id);
            idLabel.getStyleClass().add("titleListText");

            Label nameLabel = new Label(t.getName());
            nameLabel.getStyleClass().add("titleListText");

            Label selectedIndicator = new Label();
            if (selectedTitle != null && selectedTitle.equals(t.getName())) {
                selectedIndicator.setText("✔");
                selectedIndicator.getStyleClass().add("titleListText");
            }

            line.getChildren().addAll(idLabel, nameLabel, selectedIndicator);

            if (i == currentTitleIndex) {
                line.getStyleClass().add("titleListObjectSelected");
            } else {
                line.getStyleClass().add("titleListObject");
            }

            int index = i;
            line.setOnMouseClicked(e -> {
                currentTitleIndex = index;
                updateTitleDisplay();
                try {
                    refreshTitlesList();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            });

            titleList.getChildren().add(line);
        }
    }

    private void savePreferencesAndApply() throws Exception {
        UserPreferences prefs = UserPreferences.getInstance();

        prefs.setTheme(mapLabelToThemeCode(themeComboBox.getValue()));

        prefs.setSelectedFont(fontComboBox.getValue());
        prefs.saveToFile();

        if (mainController != null) {
            mainController.applyUserPreferences();
        }
    }

    @FXML
    private void handleExportData() throws Exception {
        Set<String> titlesList = loadUnlockedTitleIds();
        if (!titlesList.contains("miscCool")){
            unlockTitle("miscCool");
            TitleUnlocker.checkAndUnlockTitles();
            refreshTitlesDisplay();
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
/*
    public void loadCustomFont(String fontResourcePath) {
        InputStream fontStream = getClass().getResourceAsStream(fontResourcePath);
        if (fontStream != null) {
            Font font = Font.loadFont(fontStream, 14); // 14 = taille par défaut
            if (font != null) {
                System.out.println("Font charged : " + font.getName());
                fontComboBox.getItems().add(font.getName());
            } else {
                System.out.println("Font is impossible to charge from : " + fontResourcePath);
            }
        } else {
            System.out.println("Font file missing : " + fontResourcePath);
        }
    }*/
}
