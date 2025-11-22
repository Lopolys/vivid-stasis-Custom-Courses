package com.example.customcourses.models.conditions;

import com.example.customcourses.utils.DataInitializer;
import com.example.customcourses.utils.RankUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.hansolo.tilesfx.tools.Rank;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class CourseMaxCondition implements TitleCondition{
    private int count;
    private String courseName;

    public CourseMaxCondition(){}

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
        else{
            coursesFile = DataInitializer.getDataDirectory().resolve("courses.json");
        }
        ObjectMapper mapper = new ObjectMapper();

        List<Map<String, Object>> courses = mapper.readValue(new File(coursesFile.toString()), new TypeReference<>() {}
        );

        if(count == 1){
            for (Map<String, Object> course : courses) {
                if (courseName.equals(course.get("name"))) {
                    Map<String, Map<String, Object>> difficulties = (Map<String, Map<String, Object>>) course.get("difficulties");

                    int totalVS = 0;
                    for (Map.Entry<String, Map<String, Object>> diff : difficulties.entrySet()) {
                        Object rank = diff.getValue().get("bestRank");
                        if (rank != null && "VS".equalsIgnoreCase(rank.toString())) {
                            totalVS++;
                        }
                    }
                    return totalVS >= count;
                }
            }
        }
        else if(count == 5){
            for (Map<String, Object> course : courses) {
                if (courseName.equals(course.get("name"))) {
                    Map<String, Map<String, Object>> difficulties = (Map<String, Map<String, Object>>) course.get("difficulties");

                    int totalSS = 0;
                    for (Map.Entry<String, Map<String, Object>> diff : difficulties.entrySet()) {
                        Object rank = diff.getValue().get("bestRank");
                        if (rank != null && RankUtil.compareRanks(rank.toString(), "SS")) {
                            totalSS++;
                        }
                    }
                    return totalSS >= count;
                }
            }
        }
        return false;
    }
}
