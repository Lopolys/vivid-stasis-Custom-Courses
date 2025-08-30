package com.example.customcourses.utils;

import com.example.customcourses.models.ScoreEntry;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ScoreStorage {
    private static final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule()); // Support LocalDate et autres Java 8 dates.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) // Format lisible "2025-07-24".enable(SerializationFeature.INDENT_OUTPUT); // JSON indenté

    private static Path getScoreFilePath() throws Exception {
        return DataInitializer.getDataDirectory().resolve("scores.json");
    }

    public static void saveScores(List<ScoreEntry> scores) throws Exception {
        ensureDirectoryExists();
        File file = getScoreFilePath().toFile();
        mapper.writeValue(file, scores);
    }

    public static List<ScoreEntry> loadScores() throws Exception {
        File file = getScoreFilePath().toFile();
        if (!file.exists()) return new ArrayList<>();
        return mapper.readValue(file, new TypeReference<>() {});
    }

    private static void ensureDirectoryExists() throws Exception {
        File file = getScoreFilePath().toFile();
        File dir = file.getParentFile();
        if (!dir.exists()) dir.mkdirs();
    }
}
