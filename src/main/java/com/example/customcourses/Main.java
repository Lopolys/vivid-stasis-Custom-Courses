package com.example.customcourses;

public class Main {
    public static void main(String[] args) {
        try {
            App.main(args);
        } catch (Exception e) {
            e.printStackTrace(); // Affiche l'erreur
            System.exit(1);      // Quitte le programme avec un code d'erreur
        }
    }
}