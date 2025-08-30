package com.example.customcourses.utils;

public class RankUtil {

    private static final int[] THRESHOLDS = {
            0,        // E
            600_000,  // D
            800_000,  // C
            850_000,  // B
            900_000,  // A
            950_000,  // AA
            980_000,  // S
            990_000,  // S+
            1_000_000, // SS
            1_004_000, // SS+
            1_008_000, // V
            1_009_000, // V+
            1_010_000  // VS
    };

    private static final String[] RANKS = {
            "E", "D", "C", "B", "A", "AA", "S", "S+", "SS", "SS+", "V", "V+", "VS"
    };

    public static String calculateCourseRank(int totalScore, int musicCount) {
        int maxPossible = 1_010_000 * musicCount;
        if (totalScore > maxPossible) {
            return "??";
        }

        for (int i = THRESHOLDS.length - 1; i >= 0; i--) {
            if (totalScore >= THRESHOLDS[i] * musicCount) {
                return RANKS[i];
            }
        }
        return "E";
    }

    public static String calculateCourseRank(int score) {
        if (score > 4_040_000) {
            return "??"; // Score invalide pour une course (au-dessus du max possible)
        }
        return getRank(score, false);
    }

    public static String calculateMusicRank(int score) {
        if (score > 1_010_000) {
            return "??"; // Score invalide pour une musique
        }
        return getRank(score, true);
    }

    private static String getRank(int score, boolean isMusic) {
        for (int i = THRESHOLDS.length - 1; i >= 0; i--) {
            if (score >= THRESHOLDS[i]) {
                return RANKS[i];
            }
        }
        return "E";
    }
}
