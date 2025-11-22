package com.example.customcourses.models;

import com.example.customcourses.models.conditions.TitleCondition;
import com.example.customcourses.utils.DataInitializer;
import com.example.customcourses.App;
import com.example.customcourses.utils.NotificationUtil;
import com.example.customcourses.utils.UserPreferences;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.stage.Stage;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class Title {
    private String id;
    private String name;
    private String description;
    private String imagePath;
    private boolean unlocked;
    private TitleCondition condition;

    public Title() {}
    public Title(String id, String name, String description, String imagePath, boolean unlocked, TitleCondition condition) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imagePath = imagePath;
        this.unlocked = unlocked;
        this.condition = condition;
    }

    public String getId() { return this.id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getImagePath() { return imagePath; }
    public boolean isUnlocked() {return unlocked;}
    public void setUnlocked(boolean unlocked){ this.unlocked = unlocked;}
    public TitleCondition getCondition() { return this.condition; }

    public static List<Title> loadAllTitles() throws Exception{
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream inputStream = Title.class.getResourceAsStream("/com/example/customcourses/json/titleList.json")) {
            if (inputStream == null) {
                throw new IllegalStateException("Fichier titleList.json introuvable dans les ressources !");
            }
            return mapper.readValue(inputStream, new TypeReference<>() {});
        }
    }

    public static Path ensureTitlesFileExists() throws Exception {
        Path dataDir = DataInitializer.getDataDirectory();
        Path titlesFile = dataDir.resolve("titles.json");

        if (Files.notExists(titlesFile)) {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, List<String>> defaultData = Map.of("unlockedTitles", List.of("miscIni"));
            mapper.writerWithDefaultPrettyPrinter().writeValue(titlesFile.toFile(), defaultData);
            System.out.println("Création de titles.json par défaut dans AppData.");
        }
        return titlesFile;
    }

    public static Set<String> loadUnlockedTitleIds() throws Exception {
        Path titlesFile = ensureTitlesFileExists();

        ObjectMapper mapper = new ObjectMapper();
        Map<String, List<String>> data = mapper.readValue(titlesFile.toFile(), new TypeReference<>() {});
        return new HashSet<>(data.getOrDefault("unlockedTitles", List.of()));
    }

    public static void saveUnlockedTitles(Set<String> unlockedIds) throws Exception {
        Path titlesFile = ensureTitlesFileExists();

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> data = new HashMap<>();
        data.put("unlockedTitles", unlockedIds);

        mapper.writerWithDefaultPrettyPrinter().writeValue(titlesFile.toFile(), data);
    }

    public static List<Title> loadDisplayableTitles() throws Exception {
        List<Title> allTitles = loadAllTitles();
        Set<String> unlockedIds = loadUnlockedTitleIds();

        return allTitles.stream()
                .peek(t -> t.setUnlocked(unlockedIds.contains(t.getId())))
                .filter(Title::isUnlocked)
                .collect(Collectors.toList());
    }

    public static void unlockTitle(String titleId) throws Exception {
        List<Title> allTitles = loadAllTitles();
        Set<String> unlocked = loadUnlockedTitleIds();

        String titleName = allTitles.stream().filter(t -> Objects.equals(t.getId(), titleId)).findFirst().map(Title::getName).orElse(null);
        if (unlocked.add(titleId)) {
            saveUnlockedTitles(unlocked);
            System.out.println("🏅 Titre débloqué : " + titleId);
            Stage mainStage = App.getPrimaryStage();
            NotificationUtil.showToast(mainStage, "Nouveau titre débloqué : \n" + titleName, "titleUnlockedAlert");
        }
    }

    public static Title getSelectedTitle() {
        try {
            String selectedTitleName = UserPreferences.getInstance().getSelectedTitle();
            if (selectedTitleName == null) return null;

            List<Title> titles = loadDisplayableTitles();
            return titles.stream().filter(t -> t.getName().equalsIgnoreCase(selectedTitleName)).findFirst().orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
