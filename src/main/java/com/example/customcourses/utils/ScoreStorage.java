package com.example.customcourses.utils;

import com.example.customcourses.models.ScoreEntry;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class ScoreStorage {
    private static final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static Path getScoreFilePath() throws Exception {
        return DataInitializer.getDataDirectory().resolve("scores.json");
    }

    private static Path getHiddenScoreFilePath() throws Exception {
        return DataInitializer.getDataDirectory().resolve("hiddenScores.json");
    }

    public static void saveScores(List<ScoreEntry> scores) throws Exception {
        ensureDirectoryExists();
        Path filePath = getScoreFilePath();
        try (var out = Files.newOutputStream(filePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            mapper.writeValue(out, scores);
        }
    }

    public static void saveHiddenScores(List<ScoreEntry> scores) throws Exception {
        ensureDirectoryExists();
        Path filePath = getHiddenScoreFilePath();
        try (var out = Files.newOutputStream(filePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            mapper.writeValue(out, scores);
        }
    }

    public static List<ScoreEntry> loadScores() throws Exception {
        Path filePath = getScoreFilePath();
        if (!Files.exists(filePath)) return new ArrayList<>();
        try (var in = Files.newInputStream(filePath)) {
            return mapper.readValue(in, new TypeReference<>() {});
        }
    }

    public static List<ScoreEntry> loadHiddenScores() throws Exception {
        Path filePath = getHiddenScoreFilePath();
        if (!Files.exists(filePath)) return new ArrayList<>();
        try (var in = Files.newInputStream(filePath)) {
            return mapper.readValue(in, new TypeReference<>() {});
        }
    }

    private static void ensureDirectoryExists() throws Exception {
        File file = getScoreFilePath().toFile();
        File hidden = getHiddenScoreFilePath().toFile();
        File dir = file.getParentFile();
        File hiddenDir = hidden.getParentFile();
        if (!dir.exists()) dir.mkdirs();
        if (!hiddenDir.exists()) hiddenDir.mkdirs();
    }
}
