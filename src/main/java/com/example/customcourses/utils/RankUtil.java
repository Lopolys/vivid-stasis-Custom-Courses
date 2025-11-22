package com.example.customcourses.utils;

import javafx.scene.image.Image;

import java.util.Objects;

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

    public static String calculateMusicRank(int score) {
        if (score > 1_010_000) {
            return "??"; // Score invalide pour une musique
        }
        return getRank(score);
    }

    private static String getRank(int score) {
        for (int i = THRESHOLDS.length - 1; i >= 0; i--) {
            if (score >= THRESHOLDS[i]) {
                return RANKS[i];
            }
        }
        return "E";
    }

    public static Image getRankImage(String rank) {
        return switch (rank) {
            case "E" -> new Image(Objects.requireNonNull(RankUtil.class.getResourceAsStream("/images/ranks/E.png")));
            case "D" -> new Image(Objects.requireNonNull(RankUtil.class.getResourceAsStream("/images/ranks/D.png")));
            case "C" -> new Image(Objects.requireNonNull(RankUtil.class.getResourceAsStream("/images/ranks/C.png")));
            case "B" -> new Image(Objects.requireNonNull(RankUtil.class.getResourceAsStream("/images/ranks/B.png")));
            case "A" -> new Image(Objects.requireNonNull(RankUtil.class.getResourceAsStream("/images/ranks/A.png")));
            case "AA" -> new Image(Objects.requireNonNull(RankUtil.class.getResourceAsStream("/images/ranks/AA.png")));
            case "S" -> new Image(Objects.requireNonNull(RankUtil.class.getResourceAsStream("/images/ranks/S.png")));
            case "S+" -> new Image(Objects.requireNonNull(RankUtil.class.getResourceAsStream("/images/ranks/S+.png")));
            case "SS" -> new Image(Objects.requireNonNull(RankUtil.class.getResourceAsStream("/images/ranks/SS.png")));
            case "SS+" -> new Image(Objects.requireNonNull(RankUtil.class.getResourceAsStream("/images/ranks/SS+.png")));
            case "V" -> new Image(Objects.requireNonNull(RankUtil.class.getResourceAsStream("/images/ranks/V.png")));
            case "V+" -> new Image(Objects.requireNonNull(RankUtil.class.getResourceAsStream("/images/ranks/V+.png")));
            case "VS" -> new Image(Objects.requireNonNull(RankUtil.class.getResourceAsStream("/images/ranks/VS.png")));
            default -> throw new IllegalStateException("Unexpected value: " + rank);
        };
    }

    public static int getRankIndex(String rank) {
        for (int i = 0; i < RANKS.length; i++) {
            if (RANKS[i].equalsIgnoreCase(rank)) {
                return i;
            }
        }
        return -1; // inconnu
    }

    public static boolean compareRanks(String rank1, String rank2) {
        int index1 = getRankIndex(rank1);
        int index2 = getRankIndex(rank2);

        if (index1 == -1 || index2 == -1) {
            throw new IllegalArgumentException("Rang inconnu : " + rank1 + " ou " + rank2);
        }

        return index1 >= index2;
    }
}
