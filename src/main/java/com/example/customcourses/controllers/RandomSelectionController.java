package com.example.customcourses.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class RandomSelectionController {

    public ScrollPane randomSelectPane;
    public VBox randomSelectContent;
    @FXML private VBox contentPane;

    @FXML
    public void initialize() {

    }

    @FXML
    private void showRandomMusic() {
        loadView("/com/example/customcourses/views/RandomMusicView.fxml");
    }

    @FXML
    private void showRandomCourse() {
        loadView("/com/example/customcourses/views/RandomCourseView.fxml");
    }

    @FXML
    private void showRandomLegacy() {
        loadView("/com/example/customcourses/views/RandomLegacyView.fxml");
    }

    private void loadView(String fxmlPath) {
        try {
            ScrollPane view = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentPane.getChildren().setAll(view);;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
