package com.example.customcourses.models;

import java.time.LocalDate;
import java.util.List;

public class ScoreEntry {
    private String courseName;
    private Course.CourseDifficulty difficulty;
    private List<String> musicTitles;  // noms des musiques
    private List<Course.MusicDifficulty> difficultyLevels;
    private List<Integer> individualScores;
    private List<String> individualRanks;
    private int totalScore;
    private String totalRank;
    private LocalDate date;


    public ScoreEntry() {
        // Obligatoire pour Jackson
    }

    // Constructeur complet
    public ScoreEntry(String courseName, Course.CourseDifficulty difficulty, List<String> musicTitles, List<Course.MusicDifficulty> difficultyLevels, List<Integer> individualScores, List<String> individualRanks, int totalScore, String totalRank, LocalDate date) {
        this.courseName = courseName;
        this.difficulty = difficulty;
        this.musicTitles = musicTitles;
        this.difficultyLevels = difficultyLevels;
        this.individualScores = individualScores;
        this.individualRanks = individualRanks;
        this.totalScore = totalScore;
        this.totalRank = totalRank;
        this.date = date;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public Course.CourseDifficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Course.CourseDifficulty difficulty) {
        this.difficulty = difficulty;
    }

    public List<String> getMusicTitles() {
        return musicTitles;
    }

    public void setMusicTitles(List<String> musicTitles) {
        this.musicTitles = musicTitles;
    }

    public List<Course.MusicDifficulty> getDifficultyLevels() {
        return difficultyLevels;
    }

    public void setDifficultyLevels(List<Course.MusicDifficulty> difficultyLevels) {
        this.difficultyLevels = difficultyLevels;
    }

    public List<Integer> getIndividualScores() {
        return individualScores;
    }

    public void setIndividualScores(List<Integer> individualScores) {
        this.individualScores = individualScores;
    }

    public List<String> getIndividualRanks() {
        return individualRanks;
    }

    public void setIndividualRanks(List<String> individualRanks) {
        this.individualRanks = individualRanks;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public String getTotalRank() {
        return totalRank;
    }

    public void setTotalRank(String totalRank) {
        this.totalRank = totalRank;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
