package com.example.customcourses.models;

import com.example.customcourses.models.conditions.TitleCondition;

import java.util.*;

public class TitleUnlocker {
    public static void checkAndUnlockTitles() {
        try {
            List<Title> allTitles = Title.loadAllTitles();
            Set<String> unlockedIds = Title.loadUnlockedTitleIds();

            boolean unlockedSomething = false;

            for (Title title : allTitles) {
                if (unlockedIds.contains(title.getId())) continue;

                TitleCondition condition = title.getCondition();
                if (condition != null && condition.isSatisfied()) {
                    Title.unlockTitle(title.getId());
                    unlockedSomething = true;
                    System.out.println("Nouveau titre débloqué : " + title.getName());
                }
            }

            if (unlockedSomething) {
                System.out.println("Vérification des titres terminée : nouveaux titres débloqués !");
            } else {
                System.out.println("Aucune nouvelle condition remplie.");
            }

        } catch (Exception e) {
            System.err.println("Erreur lors de la vérification des titres : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
