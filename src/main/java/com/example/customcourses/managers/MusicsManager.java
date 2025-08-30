package com.example.customcourses.managers;

import com.example.customcourses.models.Music;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MusicsManager {

    private static final List<Music> musics = new ArrayList<>();

    public static void loadMusics() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream input = MusicsManager.class.getResourceAsStream("/com/example/customcourses/json/musics.json");
            List<Music> loaded = mapper.readValue(input, new TypeReference<>() {});
            musics.clear();
            musics.addAll(loaded);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadHiddenMusics() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream input = MusicsManager.class.getResourceAsStream("/com/example/customcourses/json/hiddenMusics.json");
            List<Music> loaded = mapper.readValue(input, new TypeReference<>() {});
            musics.clear();
            musics.addAll(loaded);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static MusicSelection getRandomMusic(String pack, double minDiff, double maxDiff) {
        List<MusicSelection> candidates = new ArrayList<>();

        for (Music m : getMusics()) {
            if (pack != null && !pack.equals(m.getPack())) continue;

            if (isInRange(m.getOpDiff(), minDiff, maxDiff))
                candidates.add(new MusicSelection(m, "Opening", m.getOpDiff()));

            if (isInRange(m.getMdDiff(), minDiff, maxDiff))
                candidates.add(new MusicSelection(m, "Middle", m.getMdDiff()));

            if (isInRange(m.getFnDiff(), minDiff, maxDiff))
                candidates.add(new MusicSelection(m, "Finale", m.getFnDiff()));

            // Encore seulement si ecDiff != null
            if (m.getEcDiff() > 0 && isInRange(m.getEcDiff(), minDiff, maxDiff))
                candidates.add(new MusicSelection(m, "Encore", m.getEcDiff()));

            // Backstage seulement si bsDiff != null
            if (m.getBsDiff() > 0 && isInRange(m.getBsDiff(), minDiff, maxDiff))
                candidates.add(new MusicSelection(m, "Backstage", m.getBsDiff()));
        }

        if (candidates.isEmpty()) return null;

        return candidates.get(new Random().nextInt(candidates.size()));
    }


    private static boolean isInRange(Double value, double min, double max) {
        return value != null && value >= min && value <= max;
    }

    public static List<Music> getMusics() {
        return musics;
    }

    public record MusicSelection(Music music, String difficultyType, double difficultyValue) {
    }
}
