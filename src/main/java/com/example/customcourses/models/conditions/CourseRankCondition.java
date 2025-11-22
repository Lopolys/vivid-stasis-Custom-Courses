package com.example.customcourses.models.conditions;

import com.example.customcourses.utils.DataInitializer;
import com.example.customcourses.utils.RankUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

public class CourseRankCondition implements TitleCondition{
    private String courseName;
    private String rankMin;

    public CourseRankCondition(){}

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getRankMin() {
        return rankMin;
    }

    public void setRankMin(String rankMin) {
        this.rankMin = rankMin;
    }

    @Override
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

                    String rank = "E";
                    for (Map.Entry<String, Map<String, Object>> diff : diffs.entrySet()) {
                        rank = diff.getValue().get("bestRank").toString();
                    }
                    return RankUtil.compareRanks(rank, rankMin);
                }
            }
        }
        else {
            for (Map<String, Object> course : courses) {
                Map<String, Map<String, Object>> diffs = (Map<String, Map<String, Object>>) course.get("difficulties");

                for (Map<String, Object> diff : diffs.values()) {
                    String rank = (String) diff.get("bestRank");
                    if (rank != null && RankUtil.compareRanks(rank, rankMin)) {
                        return true;
                    }
                }
            }

            Path legacyFile = DataInitializer.getDataDirectory().resolve("coursesLegacy.json");
            if (legacyFile.toFile().exists()) {
                List<Map<String, Object>> legacyCourses =
                        mapper.readValue(legacyFile.toFile(), new TypeReference<>() {});

                for (Map<String, Object> course : legacyCourses) {
                    Map<String, Map<String, Object>> diffs = (Map<String, Map<String, Object>>) course.get("difficulties");

                    for (Map<String, Object> diff : diffs.values()) {
                        String rank = (String) diff.get("bestRank");
                        if (rank != null && RankUtil.compareRanks(rank, rankMin)) {
                            return true;
                        }
                    }
                }
            }

            Path hiddenFile = DataInitializer.getDataDirectory().resolve("hiddenCourses.json");
            if (hiddenFile.toFile().exists()) {
                List<Map<String, Object>> hiddenCourses =
                        mapper.readValue(hiddenFile.toFile(), new TypeReference<>() {});

                for (Map<String, Object> course : hiddenCourses) {
                    Map<String, Map<String, Object>> diffs = (Map<String, Map<String, Object>>) course.get("difficulties");

                    for (Map<String, Object> diff : diffs.values()) {
                        String rank = (String) diff.get("bestRank");
                        if (rank != null && RankUtil.compareRanks(rank, rankMin)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
