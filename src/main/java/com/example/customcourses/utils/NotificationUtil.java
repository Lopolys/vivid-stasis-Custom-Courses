package com.example.customcourses.utils;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

public class NotificationUtil {

    // 🔔 File d'attente de notifications actives
    private static final Queue<Popup> activePopups = new LinkedList<>();

    // 🔧 Paramètres par défaut
    private static final double WIDTH = 350;
    private static final double SPACING = 12;
    private static final double MARGIN_BOTTOM = 80;
    private static final int MAX_NOTIFICATIONS = 5;

    /**
     * Affiche une notification animée empilable ("toast") dans la fenêtre principale.
     */
    public static void showToast(Stage stage, String message, String styleClass) throws Exception {
        String theme = UserPreferences.getInstance().getTheme();
        Platform.runLater(() -> {
            if (stage == null || stage.getScene() == null) return;

            Popup popup = new Popup();
            popup.setAutoHide(true);

            ImageView icon = null;
            try {
                String imagePath = switch (styleClass) {
                    case "entryProblemAlert" -> "/images/alerts/" + theme + "Error.png";
                    case "entrySavedAlert", "titleUnlockedAlert" -> "/images/alerts/" + theme + "Saved.png";
                    default -> throw new IllegalStateException("Unexpected value: " + styleClass);
                };
                icon = new ImageView(new Image(Objects.requireNonNull(NotificationUtil.class.getResourceAsStream(imagePath))));
                icon.setFitWidth(50);
                icon.setFitHeight(50);
            } catch (Exception ignored) {}

            Label label = new Label(message);
            label.setWrapText(true);

            // --- 🔹 Conteneur horizontal : image + texte ---
            HBox contentBox = new HBox(10);
            contentBox.setAlignment(Pos.CENTER_LEFT);
            contentBox.setPadding(new Insets(10, 15, 10, 15));
            contentBox.setPrefWidth(WIDTH);
            if (icon != null) contentBox.getChildren().add(icon);
            contentBox.getChildren().add(label);

            // --- 🔹 Style du fond ---
            contentBox.getStyleClass().add("personalizedPopup");

            StackPane container = new StackPane(contentBox);
            container.setPadding(new Insets(5));
            container.setAlignment(Pos.CENTER_RIGHT);
            popup.getContent().add(container);

            Scene scene = stage.getScene();
            double baseX = scene.getWindow().getX() + scene.getWidth() - WIDTH - 40;
            double baseY = scene.getWindow().getY() + scene.getHeight() - MARGIN_BOTTOM;

            // Décalage selon le nombre de notifications existantes
            double offsetY = activePopups.size() * (label.getHeight() + SPACING);
            popup.show(stage, baseX, baseY - offsetY);

            // Animation d’apparition (slide + fade)
            container.setOpacity(0);
            container.setTranslateY(20);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(250), container);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            TranslateTransition slideUp = new TranslateTransition(Duration.millis(300), container);
            slideUp.setFromY(20);
            slideUp.setToY(0);

            ParallelTransition appear = new ParallelTransition(fadeIn, slideUp);
            appear.play();

            // Enregistrer la popup
            activePopups.add(popup);
            repositionPopups(stage);

            // Disparition après 3 secondes
            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(_ -> removePopup(popup, stage));
            pause.play();

            // Supprime les plus anciennes si dépassement du max
            if (activePopups.size() > MAX_NOTIFICATIONS) {
                Popup oldest = activePopups.poll();
                if (oldest != null) oldest.hide();
            }
        });
    }

    /**
     * Supprime une popup avec animation et repositionne les restantes.
     */
    private static void removePopup(Popup popup, Stage stage) {
        Platform.runLater(() -> {
            if (!activePopups.contains(popup)) return;

            StackPane container = (StackPane) popup.getContent().getFirst();
            FadeTransition fadeOut = new FadeTransition(Duration.millis(400), container);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(_ -> {
                popup.hide();
                activePopups.remove(popup);
                repositionPopups(stage);
            });
            fadeOut.play();
        });
    }

    /**
     * Réorganise les notifications restantes (pour qu’elles montent proprement)
     */
    private static void repositionPopups(Stage stage) {
        if (stage == null || stage.getScene() == null) return;

        Scene scene = stage.getScene();
        double baseX = scene.getWindow().getX() + scene.getWidth() - WIDTH - 40;
        double baseY = scene.getWindow().getY() + scene.getHeight() - MARGIN_BOTTOM;

        int index = 0;
        for (Popup popup : activePopups) {
            double targetY = baseY - (index * (70 + SPACING));
            popup.setX(baseX);
            popup.setY(targetY);
            index++;
        }
    }
}
