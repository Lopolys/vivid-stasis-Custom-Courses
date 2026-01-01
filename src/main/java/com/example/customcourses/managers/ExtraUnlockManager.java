package com.example.customcourses.managers;

import com.example.customcourses.models.Course;
import com.example.customcourses.utils.UnlockCondition;
import com.fasterxml.jackson.databind.*;

import java.io.InputStream;
import java.util.*;

public final class ExtraUnlockManager {

    private static final Map<String, List<UnlockCondition>> CONDITIONS_BY_COURSE = new HashMap<>();

    private ExtraUnlockManager() {}

    public static void load() {
        CONDITIONS_BY_COURSE.clear();

        try{
            InputStream is = ExtraUnlockManager.class.getResourceAsStream("/com/example/customcourses/json/unlock.json");

            if (is == null) {
                System.err.println("unlock.json not found");
                return;
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(is);

            JsonNode extraUnlocksNode = root.get("extraUnlocks");
            if (extraUnlocksNode == null || !extraUnlocksNode.isObject()) {
                return;
            }

            Iterator<Map.Entry<String, JsonNode>> courses =
                    extraUnlocksNode.fields();

            while (courses.hasNext()) {
                Map.Entry<String, JsonNode> courseEntry = courses.next();
                String courseName = courseEntry.getKey();

                JsonNode conditionsNode =
                        courseEntry.getValue().get("conditions");

                if (conditionsNode == null || !conditionsNode.isArray()) {
                    continue;
                }

                List<UnlockCondition> conditions = new ArrayList<>();

                for (JsonNode conditionNode : conditionsNode) {
                    UnlockCondition condition = mapper.treeToValue(conditionNode, UnlockCondition.class);
                    conditions.add(condition);
                }

                CONDITIONS_BY_COURSE.put(courseName, conditions);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<UnlockCondition> getConditions(String courseName) {
        return CONDITIONS_BY_COURSE.getOrDefault(courseName, List.of());
    }

    public static boolean isExtraUnlocked(Course course) {
        return getConditions(course.getName())
                .stream()
                .allMatch(c -> c.isUnlocked(course));
    }
}
