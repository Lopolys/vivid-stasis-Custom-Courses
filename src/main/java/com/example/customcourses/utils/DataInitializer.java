package com.example.customcourses.utils;

import java.io.InputStream;
import java.nio.file.*;
import java.util.Arrays;

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
        }*/

        Files.createDirectories(dataDir);
        return dataDir;
    }

    public static void initializeJsonFiles() throws Exception {
        Path dataDir = getDataDirectory();

        String[] fichiers = {"courses.json", "coursesLegacy.json", "hiddenCourses.json", "scores.json", "hiddenScores.json", "userprefs.json", "README.txt"};
        String[] hiddenFiles = {"hiddenCourses.json", "hiddenScores.json"};

        String os = System.getProperty("os.name").toLowerCase();

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

            if (Arrays.asList(hiddenFiles).contains(nom)) {
                if (os.contains("win")) {
                    // Windows : attribut hidden
                    Files.setAttribute(dest, "dos:hidden", true, LinkOption.NOFOLLOW_LINKS);
                } else {
                    // Linux / macOS : renommer avec un point au début
                    Path hiddenDest = dest.resolveSibling("." + dest.getFileName().toString());
                    Files.move(dest, hiddenDest, StandardCopyOption.REPLACE_EXISTING);
                    dest = hiddenDest; // met à jour la référence si besoin après renommage
                }
            }
        }
    }
}
