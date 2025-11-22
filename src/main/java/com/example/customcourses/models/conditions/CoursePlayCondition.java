package com.example.customcourses.models.conditions;

import com.example.customcourses.utils.DataInitializer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CoursePlayCondition implements TitleCondition{
    private String courseName;
    private int count;

    public CoursePlayCondition(){}

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public boolean isSatisfied() throws Exception {
        Path coursesFile;
        if (courseName.contains("(Legacy")){
            coursesFile = DataInitializer.getDataDirectory().resolve("coursesLegacy.json");
        }
        else {
            coursesFile = DataInitializer.getDataDirectory().resolve("courses.json");
        }
        ObjectMapper mapper = new ObjectMapper();

        List<Map<String, Object>> courses = mapper.readValue(new File(coursesFile.toString()), new TypeReference<>() {});

        if (!"any".equalsIgnoreCase(courseName) && courses.stream().noneMatch(c -> courseName.equals(c.get("name")))) {
            Path hiddenFile = DataInitializer.getDataDirectory().resolve("hiddenCourses.json");
            if (hiddenFile.toFile().exists()) {
                courses = mapper.readValue(hiddenFile.toFile(), new TypeReference<>() {});
            }
        }

        if (!"any".equalsIgnoreCase(courseName)) {
            for (Map<String, Object> course : courses) {
                if (courseName.equals(course.get("name"))) {
                    Map<String, Map<String, Object>> diffs = (Map<String, Map<String, Object>>) course.get("difficulties");

                    int totalPlayed = 0;
                    for (Map.Entry<String, Map<String, Object>> diff : diffs.entrySet()) {
                        int score = (int) diff.getValue().get("bestScore");
                        if (score > 0) {
                            totalPlayed++;
                        }
                    }
                    return totalPlayed >= count;
                }
            }
        }
        else {
            Set<String> playedDifficulties = new HashSet<>();

            for (Map<String, Object> course : courses) {
                Map<String, Map<String, Object>> diffs =
                        (Map<String, Map<String, Object>>) course.get("difficulties");

                for (Map.Entry<String, Map<String, Object>> entry : diffs.entrySet()) {
                    String difficultyName = entry.getKey(); // ex: EXPOSITION, TWIST, CLIMAX
                    Object scoreObj = entry.getValue().get("bestScore");

                    if (scoreObj instanceof Number && ((Number) scoreObj).intValue() > 0) {
                        playedDifficulties.add(difficultyName); // évite les doublons
                    }
                }
            }
            return playedDifficulties.size() >= count;
        }
        return false;
    }
}
