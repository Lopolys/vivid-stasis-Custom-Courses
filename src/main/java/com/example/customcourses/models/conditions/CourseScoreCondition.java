package com.example.customcourses.models.conditions;

import com.example.customcourses.utils.DataInitializer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class CourseScoreCondition implements TitleCondition{
    private int scoreMin;
    private int scoreMax;

    public CourseScoreCondition(){}

    public int getScoreMin() {
        return scoreMin;
    }

    public void setScoreMin(int scoreMin) {
        this.scoreMin = scoreMin;
    }

    public int getScoreMax() {
        return scoreMax;
    }

    public void setScoreMax(int scoreMax) {
        this.scoreMax = scoreMax;
    }

    @Override
    public boolean isSatisfied() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        Path[] scoreFiles = {
                DataInitializer.getDataDirectory().resolve("scores.json"),
                DataInitializer.getDataDirectory().resolve("hiddenScores.json")
        };

        for (Path path : scoreFiles) {
            if (!path.toFile().exists()) continue;
            List<Map<String, Object>> scores = mapper.readValue(path.toFile(), new TypeReference<>() {});

            for (Map<String, Object> score : scores) {
                int scoreValue = (int) score.get("totalScore");
                if (scoreValue >= scoreMin && scoreValue <= scoreMax){
                    return true;
                }
            }
        }
        return false;
    }
}
