package com.example.customcourses.utils;

import com.example.customcourses.models.Course;

public class UnlockCondition {

    public enum Type {
        SCORE_AT_LEAST,
        RANK_AT_LEAST
    }

    private String id;
    private Type type;
    private Course.CourseDifficulty difficulty;
    private Integer value;
    private String rank;

    public UnlockCondition() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Course.CourseDifficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Course.CourseDifficulty difficulty) {
        this.difficulty = difficulty;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public boolean isUnlocked(Course course) {
        Course.CourseDifficultySection section = course.getDifficulties().get(difficulty);

        if (section == null) return false;

        return switch (type) {
            case SCORE_AT_LEAST ->
                    section.getBestScore() >= value;

            case RANK_AT_LEAST ->
                    RankUtil.compareRanks(section.getBestRank(), rank);
        };
    }
}
