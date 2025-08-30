package com.example.customcourses.utils;

import com.example.customcourses.models.Course;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CoursesSynchronizer {

    private static final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public static void syncCourses(String fileName) throws Exception {
        Path dataDir = DataInitializer.getDataDirectory();
        Path localFile = dataDir.resolve(fileName);

        // Charger la version locale
        List<Course> localCourses = readCourses(localFile);

        // Charger la version référence (ressources)
        try (InputStream in = CoursesSynchronizer.class.getResourceAsStream("/com/example/customcourses/json/" + fileName)) {
            if (in == null) throw new FileNotFoundException("Fichier référence introuvable : " + fileName);
            List<Course> referenceCourses = mapper.readValue(in, new TypeReference<>() {});

            // Synchroniser la version locale avec la version référence
            for (Course refCourse : referenceCourses) {
                Optional<Course> optLocal = localCourses.stream().filter(c -> c.getName().equalsIgnoreCase(refCourse.getName())).findFirst();

                if (optLocal.isPresent()) {
                    // Course existante, gestion à part
                    Course local = optLocal.get();
                    syncCourse(local, refCourse);
                } else {
                    // Nouvelle course → ajout direct
                    localCourses.add(refCourse);
                }
            }
            // Suppression des courses qui n'existent plus dans ce fichier.
            localCourses.removeIf(localCourse -> referenceCourses.stream().noneMatch(refCourse -> refCourse.getName().equalsIgnoreCase(localCourse.getName())));

            // Sauvegarde des modifications
            mapper.writeValue(localFile.toFile(), localCourses);
        }
    }

    private static void syncCourse(Course local, Course reference) {
        for (Course.CourseDifficulty diff : reference.getDifficulties().keySet()) {
            Course.CourseDifficultySection refSection = reference.getDifficulties().get(diff);
            Course.CourseDifficultySection localSection = local.getDifficulties().get(diff);

            if (localSection == null) {
                // Nouvelle section → ajout direct
                local.getDifficulties().put(diff, refSection);
                continue;
            }

            boolean sameMusicOrder = localSection.getMusicTitles().equals(refSection.getMusicTitles());
            boolean sameDifficulties = haveSameDifficulties(localSection.getDifficultyLevels(), refSection.getDifficultyLevels());

            if (!sameMusicOrder && sameDifficulties) {
                // Ordre changé mais mêmes difficultés → garder score et rang
                localSection.setMusicTitles(refSection.getMusicTitles());
                localSection.setDifficultyLevels(refSection.getDifficultyLevels());
            }
            else if (!sameDifficulties) {
                // Difficulté changée → réinitialiser score et rang
                localSection.setMusicTitles(refSection.getMusicTitles());
                localSection.setDifficultyLevels(refSection.getDifficultyLevels());
                localSection.setBestScore(0);
                localSection.setBestRank("E");
            }
        }
    }

    private static boolean haveSameDifficulties(List<Course.MusicDifficulty> list1, List<Course.MusicDifficulty> list2) {
        if (list1.size() != list2.size()) {
            return false;
        }

        // Comptage des occurrences pour chaque liste
        var count1 = list1.stream().collect(Collectors.groupingBy(d -> d, Collectors.counting()));

        var count2 = list2.stream().collect(Collectors.groupingBy(d -> d, Collectors.counting()));

        return count1.equals(count2);
    }

    private static List<Course> readCourses(Path path) throws IOException {
        if (!Files.exists(path)) return new ArrayList<>();
        return mapper.readValue(path.toFile(), new TypeReference<>() {});
    }
}
