package com.example.customcourses.models.conditions;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,          // le champ "type" dans le JSON identifiera la sous-classe
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"                    // exemple : "type": "score"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = CourseCountCondition.class, name = "COURSE_COUNT"),
        @JsonSubTypes.Type(value = CourseMaxCondition.class, name = "COURSE_MAX"),
        @JsonSubTypes.Type(value = CoursePlayCondition.class, name = "COURSE_PLAY"),
        @JsonSubTypes.Type(value = CourseRankCondition.class, name = "COURSE_RANK"),
        @JsonSubTypes.Type(value = CourseScoreCondition.class, name = "COURSE_SCORE"),
        @JsonSubTypes.Type(value = DifficultyRankCondition.class, name = "DIFFICULTY_RANK"),
        @JsonSubTypes.Type(value = NameInsertionCondition.class, name = "NAME_INSERTION")

})
public interface TitleCondition {
    boolean isSatisfied() throws Exception;
}
