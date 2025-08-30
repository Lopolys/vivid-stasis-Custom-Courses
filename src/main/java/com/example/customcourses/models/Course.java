package com.example.customcourses.models;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Course {

    private String name;
    private Map<CourseDifficulty, CourseDifficultySection> difficulties;

    public Course() {
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public enum CourseDifficulty {
        EXPOSITION,
        TWIST,
        INTERLUDE,
        CLIMAX,
        APOTHEOSIS
    }

    public enum MusicDifficulty {
        OP, MD, FN, EC, BS;

        public double getDifficultyValue(Music music) {
            return switch (this) {
                case OP -> music.getOpDiff();
                case MD -> music.getMdDiff();
                case FN -> music.getFnDiff();
                case EC -> music.getEcDiff();
                case BS -> music.getBsDiff();
            };
        }
    }

    public static class CourseDifficultySection {
        private List<String> musicTitles; // ➤ titres à résoudre
        private List<Music> musics;       // ➤ résolus à partir de MusicManager
        private List<Course.MusicDifficulty> difficultyLevels;
        private int bestScore;
        private String bestRank;

        public void resolveMusics(List<Music> allMusics) {
            this.musics = new ArrayList<>();
            for (String title : musicTitles) {
                allMusics.stream()
                        .filter(m -> m.getTitle().equalsIgnoreCase(title))
                        .findFirst()
                        .ifPresent(musics::add);
            }
        }

        public CourseDifficultySection() {
        }

        public List<String> getMusicTitles() {
            return musicTitles;
        }

        public void setMusicTitles(List<String> musicTitles) {
            this.musicTitles = musicTitles;
        }

        public List<MusicDifficulty> getDifficultyLevels() {
            return difficultyLevels;
        }

        public void setDifficultyLevels(List<MusicDifficulty> difficultyLevels) {
            this.difficultyLevels = difficultyLevels;
        }

        public List<Music> getMusics() {
            return musics;
        }

        public void setMusics(List<Music> musics) {
            this.musics = musics;
        }

        public int getBestScore() {
            return bestScore;
        }

        public void setBestScore(int bestScore) {
            this.bestScore = bestScore;
        }

        public String getBestRank() {
            return bestRank;
        }

        public void setBestRank(String bestRank) {
            this.bestRank = bestRank;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<CourseDifficulty, CourseDifficultySection> getDifficulties() {
        return difficulties;
    }

    public void setDifficulties(Map<CourseDifficulty, CourseDifficultySection> difficulties) {
        this.difficulties = difficulties;
    }
}
