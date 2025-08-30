package com.example.customcourses.utils;

import java.io.InputStream;
import java.nio.file.*;

public class DataInitializer {

    public static Path getDataDirectory() throws Exception {
        String os = System.getProperty("os.name").toLowerCase();
        Path dataDir;

        // Utilisé pour les versions application
        if (os.contains("win")) {
            String appData = System.getenv("LOCALAPPDATA");
            dataDir = Paths.get(appData, "vividcustomcourses", "json");
        } else if (os.contains("mac")) {
            String userHome = System.getProperty("user.home");
            dataDir = Paths.get(userHome, "Library", "Application Support", "vividcustomcourses", "json");
        } else {
            String userHome = System.getProperty("user.home");
            dataDir = Paths.get(userHome, ".vividcustomcourses", "json");
        }

        /* Utilisé pour les versions test
        if (os.contains("win")) {
            String appData = System.getenv("LOCALAPPDATA");
            dataDir = Paths.get(appData, "vividcustomcourses_TEST", "json");
        } else if (os.contains("mac")) {
            String userHome = System.getProperty("user.home");
            dataDir = Paths.get(userHome, "Library", "Application Support", "vividcustomcourses_TEST", "json");
        } else {
            String userHome = System.getProperty("user.home");
            dataDir = Paths.get(userHome, ".vividcustomcourses_TEST", "json");
        } */

        Files.createDirectories(dataDir);
        return dataDir;
    }

    public static void initializeJsonFiles() throws Exception {
        Path dataDir = getDataDirectory();

        String[] fichiers = {"courses.json", "coursesLegacy.json", "scores.json", "userprefs.json", "README.txt"};

        for (String nom : fichiers) {
            Path dest = dataDir.resolve(nom);
            if (Files.notExists(dest)) {
                try (InputStream in = DataInitializer.class.getResourceAsStream("/com/example/customcourses/json/" + nom)) {
                    if (in != null) {
                        Files.copy(in, dest);
                    } else {
                        System.err.println("Fichier par défaut introuvable : " + nom);
                    }
                }
            }
        }
    }
}
