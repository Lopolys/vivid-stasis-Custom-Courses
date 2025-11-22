package com.example.customcourses.models.conditions;

import com.example.customcourses.utils.DataInitializer;
import com.example.customcourses.utils.RankUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class CourseCountCondition implements TitleCondition{
    private String diffName;
    private int requiredCount;

    public CourseCountCondition(){}

    public String getDiffName() {
        return diffName;
    }

    public void setDiffName(String diffName) {
        this.diffName = diffName;
    }

    public int getRequiredCount() {
        return requiredCount;
    }

    public void setRequiredCount(int requiredCount){
        this.requiredCount = requiredCount;
    }

    @Override
    public boolean isSatisfied() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        int countV = 0;

        Path[] courseFiles = {
                DataInitializer.getDataDirectory().resolve("courses.json"),
                DataInitializer.getDataDirectory().resolve("coursesLegacy.json")
        };

        for (Path path : courseFiles) {
            if (!path.toFile().exists()) continue;
            List<Map<String, Object>> courses = mapper.readValue(path.toFile(), new TypeReference<>() {});

            for (Map<String, Object> course : courses) {
                Map<String, Map<String, Object>> diffs = (Map<String, Map<String, Object>>) course.get("difficulties");

                if (diffs.containsKey(diffName)) {
                    Map<String, Object> diffData = diffs.get(diffName);
                    String rank = (String) diffData.get("bestRank");

                    if (rank != null && RankUtil.compareRanks(rank, "V")) {
                        countV++;
                    }
                }
            }
        }
        return countV >= requiredCount;
    }
}
