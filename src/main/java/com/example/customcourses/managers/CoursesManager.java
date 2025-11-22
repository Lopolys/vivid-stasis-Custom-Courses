package com.example.customcourses.managers;

import com.example.customcourses.models.Course;
import com.example.customcourses.models.Music;
import com.example.customcourses.utils.DataInitializer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class CoursesManager {

    private final Map<String, Course> courses;
    private static final String COURSES_JSON = "courses.json";
    private static final String LEGACY_JSON = "coursesLegacy.json";
    private static final String HIDDEN_JSON = "hiddenCourses.json";

    public CoursesManager() {
        this.courses = new LinkedHashMap<>();
    }

    public Collection<Course> getAllCourses() {
        return courses.values();
    }

    public static class CourseSelection {
        private final Course course;
        private final String difficulty;
        private final int bestScore;

        public CourseSelection(Course course, String difficulty, int bestScore) {
            this.course = course;
            this.difficulty = difficulty;
            this.bestScore = bestScore;
        }

        public Course getCourse() { return course; }
        public String getDifficulty() { return difficulty; }
        public int getBestScore() { return bestScore; }
    }

    public CoursesManager.CourseSelection getRandomCourse(List<Course.CourseDifficulty> allowedDiffs, int maxScore) {
        List<CoursesManager.CourseSelection> candidates = new ArrayList<>();

        for (Course c : courses.values()) {
            if (c.getDifficulties() == null) continue;

            for (Map.Entry<Course.CourseDifficulty, Course.CourseDifficultySection> entry : c.getDifficulties().entrySet()) {
                Course.CourseDifficulty diff = entry.getKey();
                Course.CourseDifficultySection section = entry.getValue();
                if (section == null) continue;

                if (allowedDiffs != null && !allowedDiffs.isEmpty() && !allowedDiffs.contains(diff)) continue;

                if (section.getBestScore() > maxScore) continue;

                candidates.add(new CoursesManager.CourseSelection(c, diff.name(), section.getBestScore()));
            }
        }

        if (candidates.isEmpty()) return null;
        return candidates.get(new Random().nextInt(candidates.size()));
    }

    public void loadCourses(List<Music> allMusics) {
        loadFromJSON(COURSES_JSON, allMusics);
    }

    public void loadLegacyCourses(List<Music> allMusics) {
        loadFromJSON(LEGACY_JSON, allMusics);
    }

    public void loadHiddenCourses(List<Music> allMusics) {
        loadFromJSON(HIDDEN_JSON, allMusics);
    }

    public void loadFromJSON(String filename, List<Music> allMusics) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            Path dataDir = DataInitializer.getDataDirectory();
            Path path = dataDir.resolve(filename);

            // Vérifie que le fichier existe
            if (Files.notExists(path)) {
                System.err.println("Fichier courses.json non trouvé à : " + path.toAbsolutePath());
                return;
            }

            List<Course> loaded = mapper.readValue(path.toFile(), new TypeReference<>() {});
            courses.clear();

            System.out.println("Courses loaded: " + loaded.size());

            for (Course course : loaded) {
                System.out.println("→ Course found: " + course.getName());
                for (Course.CourseDifficultySection section : course.getDifficulties().values()) {
                    section.resolveMusics(allMusics);
                }
                courses.put(course.getName(), course);
            }

            System.out.println("Courses loaded: " + courses.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
