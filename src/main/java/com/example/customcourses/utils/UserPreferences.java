package com.example.customcourses.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class UserPreferences {private static UserPreferences instance;

    private String theme = "sat";
    private String selectedFont = "Arial";
    private String userName = "";
    private String selectedTitle = "miscIni";

    public static UserPreferences getInstance() throws Exception {
        if (instance == null) {
            instance = loadFromFile();
        }
        return instance;
    }

    private static Path getUserprefsFilePath() throws Exception {
        return DataInitializer.getDataDirectory().resolve("userprefs.json");
    }

    public String getTheme() {
        return theme;
    }
    public String getSelectedFont() {
        return selectedFont;
    }
    public String getUserName() { return userName; }
    public String getSelectedTitle() { return selectedTitle; }

    public void setTheme(String theme) {
        this.theme = theme;
    }
    public void setSelectedFont(String selectedFont) {
        this.selectedFont = selectedFont;
    }
    public void setUserName(String userName) { this.userName = userName; }
    public void setSelectedTitle(String selectedTitle) { this.selectedTitle = selectedTitle; }

    public void saveToFile() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        File file = getUserprefsFilePath().toFile();
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static UserPreferences loadFromFile() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        File file = getUserprefsFilePath().toFile();
        if (file.exists()) {
            try {
                return mapper.readValue(file, UserPreferences.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new UserPreferences(); // valeurs par défaut
    }
}
