package com.example.customcourses.models.conditions;

import com.example.customcourses.utils.DataInitializer;
import com.example.customcourses.utils.RankUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class DifficultyRankCondition implements TitleCondition{
    private String courseName;
    private String diffName;
    private String rankNeed;

    public DifficultyRankCondition(){}

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getDiffName() {
        return diffName;
    }

    public void setDiffName(String diffName) {
        this.diffName = diffName;
    }

    public String getRankNeed() {
        return rankNeed;
    }

    public void setRankNeed(String rankNeed) {
        this.rankNeed = rankNeed;
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

        for (Map<String, Object> course : courses) {
            if (courseName.equals(course.get("name"))) {
                Map<String, Map<String, Object>> diffs = (Map<String, Map<String, Object>>) course.get("difficulties");

                if (diffs.containsKey(diffName)) {
                    Map<String, Object> diffData = diffs.get(diffName);
                    String rank = (String) diffData.get("bestRank");
                    return RankUtil.compareRanks(rank, rankNeed);
                }
            }
        }
        return false;
    }
}
