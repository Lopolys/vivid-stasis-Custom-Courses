package com.example.customcourses.managers;

import com.example.customcourses.models.Course;
import com.example.customcourses.models.Music;
import com.example.customcourses.utils.DataInitializer;
import com.example.customcourses.utils.RankUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
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

    public void addCourse(Course course) {
        courses.put(course.getName(), course);
    }

    public Course getCourse(String name) {
        return courses.get(name);
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

        // ✅ on parcourt bien courses.values(), pas courses
        for (Course c : courses.values()) {
            if (c.getDifficulties() == null) continue;

            for (Map.Entry<Course.CourseDifficulty, Course.CourseDifficultySection> entry : c.getDifficulties().entrySet()) {
                Course.CourseDifficulty diff = entry.getKey();
                Course.CourseDifficultySection section = entry.getValue();
                if (section == null) continue;

                // ✅ Si aucune difficulté cochée → on accepte tout
                if (allowedDiffs != null && !allowedDiffs.isEmpty() && !allowedDiffs.contains(diff)) continue;

                // ✅ Filtre sur le score
                if (section.getBestScore() > maxScore) continue;

                candidates.add(new CoursesManager.CourseSelection(c, diff.name(), section.getBestScore()));
            }
        }

        if (candidates.isEmpty()) return null;
        return candidates.get(new Random().nextInt(candidates.size()));
    }


    public void updateBestScoreIfHigher(String courseName, Course.CourseDifficulty difficulty, List<Music> musics, List<Course.MusicDifficulty> difficultyLevels, List<Integer> scores) {
        Course course = courses.get(courseName);
        if (course == null) return;

        Course.CourseDifficultySection section = course.getDifficulties().get(difficulty);
        if (section == null) return;

        int totalScore = scores.stream().mapToInt(Integer::intValue).sum();

        if (totalScore > section.getBestScore() && totalScore < 4040000) {
            section.setBestScore(totalScore);
            section.setBestRank(RankUtil.calculateCourseRank(totalScore));
            section.setMusics(musics);
            section.setDifficultyLevels(difficultyLevels);
        }
    }

    public void saveCourses() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);

            // Récupère le dossier AppData via DataInitializer
            Path dataDir = DataInitializer.getDataDirectory();
            Path filePath = dataDir.resolve(COURSES_JSON);
            File file = filePath.toFile();

            // Sauvegarde le fichier JSON dans AppData
            mapper.writeValue(file, new ArrayList<>(courses.values()));

            System.out.println("✅ courses.json mis à jour à : " + filePath.toAbsolutePath());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void printCourseDetails(String courseName) {
        Course course = getCourse(courseName);
        if (course == null) {
            System.out.println("Course not found.");
            return;
        }

        System.out.println("Course: " + course.getName());
        for (Course.CourseDifficulty diff : Course.CourseDifficulty.values()) {
            Course.CourseDifficultySection section = course.getDifficulties().get(diff);
            if (section != null) {
                System.out.println("- " + diff.name());
                List<Music> musics = section.getMusics();
                List<Course.MusicDifficulty> levels = section.getDifficultyLevels();
                for (int i = 0; i < musics.size(); i++) {
                    Music music = musics.get(i);
                    Course.MusicDifficulty level = levels.get(i);
                    double difficultyValue = level.getDifficultyValue(music);
                    System.out.printf("  • %s [%s] | %.1f★\n", music.getTitle(), level.name(), difficultyValue);
                }
                System.out.println("  Best Score: " + section.getBestScore() +
                        " | Rank: " + section.getBestRank());
            }
        }
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

            // 📍 Nouveau chemin vers le fichier courses.json
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
