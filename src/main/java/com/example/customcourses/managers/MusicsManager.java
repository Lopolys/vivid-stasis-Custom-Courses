package com.example.customcourses.managers;

import com.example.customcourses.models.Music;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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

    public static List<Music> getMusics() {
        return musics;
    }
}
