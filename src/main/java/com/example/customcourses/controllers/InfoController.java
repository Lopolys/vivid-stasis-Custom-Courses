package com.example.customcourses.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.ScrollEvent;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class InfoController {

    @FXML
    private Button btnInfo1;

    @FXML
    private Button btnInfo2;

    @FXML
    private Button btnInfo3;

    @FXML
    private ScrollPane infoScrollPane;

    @FXML
    private TextFlow textArea;

    @FXML
    public void initialize() {
        btnInfo1.setOnAction(e -> loadTextFile("about.txt"));
        btnInfo2.setOnAction(e -> loadTextFile("terms.txt"));
        btnInfo3.setOnAction(e -> loadTextFile("patchnotes.txt"));

        textArea.setPadding(new Insets(10));

        // Optionnel : charger un texte par défaut au lancement
        loadTextFile("about.txt");

        infoScrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            double deltaY = event.getDeltaY() * 3; // multiplier par un facteur pour accélérer le scroll
            infoScrollPane.setVvalue(infoScrollPane.getVvalue() - deltaY / infoScrollPane.getContent().getBoundsInLocal().getHeight());
            event.consume();  // Empêche le scroll normal pour appliquer le tien
        });
    }

    private void loadTextFile(String filename) {
        textArea.getChildren().clear();

        InputStream is = getClass().getResourceAsStream("/com/example/customcourses/texts/" + filename);
        if (is == null) {
            textArea.getChildren().add(new Text("Fichier " + filename + " non trouvé !"));
            return;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Text text = new Text(line + "\n");
                text.getStyleClass().add("infoText");

                if (line.startsWith("###")) {
                    String cleanLine = line.substring(3).trim();
                    text = new Text(cleanLine + "\n");
                    text.getStyleClass().add("versionLabel");
                } else if (line.startsWith("##")) {
                    String cleanLine = line.substring(2).trim();
                    text = new Text(cleanLine + "\n");
                    text.getStyleClass().add("titleLabel");
                } else if (line.startsWith("#hidden#")) {
                    String cleanLine = line.substring(8).trim();
                    text = new Text(cleanLine + "\n");
                    text.getStyleClass().add("hiddenLabel");
                } else if (line.startsWith("#")) {
                    String cleanLine = line.substring(1).trim();
                    text = new Text(cleanLine + "\n");
                    text.getStyleClass().add("subTitleLabel");
                } else if (line.startsWith("-#- http")) {
                    String cleanLine = line.substring(3).trim();
                    text = new Text(cleanLine + "\n");
                    text.getStyleClass().add("lienLabel");
                    text.setOnMouseClicked(e -> {
                        try {
                            java.awt.Desktop.getDesktop().browse(new java.net.URI(cleanLine));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    });
                } else if (line.startsWith("-#-")) {
                    String cleanLine = line.substring(3).trim();
                    text = new Text(cleanLine + "\n");
                    text.getStyleClass().add("listNoTabLabel");
                } else if (line.startsWith("-#")) {
                    String cleanLine = line.substring(2).trim();
                    text = new Text("\t\t" + cleanLine + "\n");
                    text.getStyleClass().add("listLabel");
                } else {
                    text.getStyleClass().add("normalLabel");
                }

                textArea.getChildren().add(text);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
