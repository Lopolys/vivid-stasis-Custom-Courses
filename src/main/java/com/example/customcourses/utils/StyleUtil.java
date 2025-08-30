package com.example.customcourses.utils;

import javafx.scene.Scene;

import java.util.Objects;

public class StyleUtil {

    public static void applyUserPreferences(Scene scene) throws Exception {
        UserPreferences prefs = UserPreferences.getInstance();
        if (scene == null || prefs == null) return;

        scene.getStylesheets().removeIf(s -> s.contains("-theme.css"));

        String theme = prefs.getTheme();
        String themeCss = "/com/example/customcourses/styles/" + theme + "-theme.css";

        scene.getStylesheets().add(Objects.requireNonNull(StyleUtil.class.getResource(themeCss)).toExternalForm());

        scene.getRoot().setStyle("-fx-font-family: '" + prefs.getSelectedFont() + "';");
    }
}
