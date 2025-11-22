package com.example.customcourses.utils;

import com.example.customcourses.models.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ScoreExporter {

    public static void exportScore(Stage stage, ScoreEntry score, List<Music> musics) {
        try {
            double width = 1920;
            double height = 1080;

            Canvas canvas = new Canvas(width, height);
            GraphicsContext gc = canvas.getGraphicsContext2D();

            // === Fond selon le thème ===
            String theme = UserPreferences.getInstance().getTheme(); // par ex. "red", "blue", ...
            try {
                Image bg = new Image(Objects.requireNonNull(
                        ScoreExporter.class.getResourceAsStream("/com/example/customcourses/themebg/" + theme + "ExportBG.png")));
                gc.drawImage(bg, 0, 0, width, height);
            } catch (Exception e) {
                gc.setFill(Color.web("#ff0000"));
                gc.fillRect(0, 0, width, height);
            }

            // --- Username ---
            String username = UserPreferences.getInstance().getUserName();
            gc.setFill(Color.WHITE);
            Font customFont = loadCustomFont("/fonts/vivid-stasis-default-font.ttf", 54);
            gc.setFont(customFont);
            drawCenteredText(gc, username, 48, 48, 936, 66);

            // --- Title image ---
            Title selectedTitle = Title.getSelectedTitle();
            if (selectedTitle != null) {
                Image titleImage = loadTitleImage(selectedTitle.getImagePath());
                if (titleImage != null) {
                    gc.drawImage(titleImage, 996, 42);
                }
            }

            // === Bloc gauche (informations de course) ===
            double courseX = 36;
            double topCourseY = 174;
            double midCourseY = 540;
            double botCourseY = 984;

            String courseName = score.getCourseName();
            Path coursesFile;
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> courses = null;
            String courseType = null;

            if (courseName.contains("(Legacy")){
                coursesFile = DataInitializer.getDataDirectory().resolve("coursesLegacy.json");
                courses = mapper.readValue(new File(coursesFile.toString()), new TypeReference<>() {});
                courseType = "L";
            }
            else if (!courseName.contains("SAMPLE")){
                coursesFile = DataInitializer.getDataDirectory().resolve("hiddenCourses.json");
                courses = mapper.readValue(new File(coursesFile.toString()), new TypeReference<>() {});
                courseType = "H";
                if (courses.stream().noneMatch(c -> courseName.equals(c.get("name")))) {
                    coursesFile = DataInitializer.getDataDirectory().resolve("courses.json");
                    courses = mapper.readValue(new File(coursesFile.toString()), new TypeReference<>() {});
                    courseType = "C";
                }
            }
            String pre = "";
            if (!courseName.contains("SAMPLE")){
                assert courses != null;
                for (Map<String, Object> course : courses){
                    if (courseName.equals(course.get("name"))) {
                        String index = String.valueOf(courses.indexOf(course) + 1);
                        System.out.println(index);
                        switch (courseType){
                            case "L" -> pre = "lc" + index;
                            case "H" -> pre = "hc" + index;
                            case "C" -> pre = "cc" + index;
                            default -> throw new IllegalStateException("Unexpected value: " + courseType);
                        }
                        System.out.println(pre);
                    }
                }
            }
            else {
                pre = "sample";
                System.out.println(pre);
            }

            Image courseImage = new Image(Objects.requireNonNull(ScoreExporter.class.getResourceAsStream("/com/example/customcourses/coursebg/" + pre +"ExportBG.png")));
            gc.drawImage(courseImage, courseX, topCourseY);

            customFont = loadCustomFont("/fonts/vivid-stasis-default-font.ttf", 72);
            gc.setFont(customFont);
            drawCenteredTextWithOutline(gc, String.valueOf(score.getTotalScore()), courseX + 4, midCourseY + 4, 552, 378, Color.web("#2D0013"), Color.web("#2D0013"), 8);
            drawCenteredTextWithOutline(gc, String.valueOf(score.getTotalScore()), courseX, midCourseY, 552, 378, Color.web("#FF4294"), Color.web("#700030"), 8);

            try {
                Image rankImg = RankUtil.getRankImage(score.getTotalRank());
                drawCenteredImage(gc, rankImg, courseX, midCourseY + 216, 552, 216, true);
            } catch (Exception ignored) {}

            String courseDiffName = score.getDifficulty().toString();
            boolean isExtra = false;

            switch (courseDiffName){
                case "EXPOSITION" -> gc.setFill(Color.ORANGE);
                case "TWIST" -> gc.setFill(Color.web("#00ff21"));
                case "INTERLUDE" -> gc.setFill(Color.web("#0194ff"));
                case "CLIMAX" -> gc.setFill(Color.web("#ff0000"));
                case "APOTHEOSIS" -> gc.setFill(Color.web("#b200ff"));
                case "DESTROYED" -> gc.setFill(Color.WHITE);
                case "EXTRA" -> {
                    isExtra = true;
                    customFont = loadCustomFont("/fonts/vivid-stasis-default-font.ttf", 54);
                    gc.setFont(customFont);

                    drawCenteredMulticolorText(gc, "EXTRA", courseX, botCourseY, 552, 66);
                }
            }

            if (!isExtra) {
                customFont = loadCustomFont("/fonts/vivid-stasis-default-font.ttf", 54);
                gc.setFont(customFont);
                drawCenteredText(gc, score.getDifficulty().toString(), courseX, botCourseY, 552, 66);
            }
            // === Grille des 4 musiques ===
            double leftStartX = 616;
            double rightStartX = 919;
            double topStartY = 184;
            double bottomStartY = 283;

            double leftCellW = 300;
            double rightCellW = 330;
            double topCellH = 96;
            double bottomCellH = 300;

            double leftSpacingX = 355;
            double rightSpacingX = 325;
            double topSpacingY = 351;
            double bottomSpacingY = 147;

            for (int i = 0; i < musics.size(); i++) {
                Music music = musics.get(i);
                Course.MusicDifficulty diff = score.getDifficultyLevels().get(i);

                int row = i / 2;  // 0 or 1
                int col = i % 2;  // 0 or 1

                double leftX   = leftStartX  + col * (leftCellW  + leftSpacingX);
                double topY    = topStartY   + row * (topCellH   + topSpacingY);
                double rightX  = rightStartX + col * (rightCellW + rightSpacingX);
                double bottomY = bottomStartY+ row * (bottomCellH+ bottomSpacingY);

                // Nom musique
                gc.setFill(Color.WHITE);
                customFont = loadCustomFont("/fonts/vivid-stasis-default-font.ttf", 27);
                gc.setFont(customFont);
                drawCenteredText(gc, music.getTitle(), leftX, topY, leftCellW, topCellH / 2);

                gc.setFill(Color.web("#aaaaaa"));
                customFont = loadCustomFont("/fonts/vivid-stasis-default-font.ttf", 18);
                gc.setFont(customFont);
                drawCenteredText(gc, music.getArtist(), leftX, topY + topCellH/2, leftCellW, topCellH/2);

                // Jaquette
                try {
                    String imageName = (diff == Course.MusicDifficulty.BS) ? music.getBsImage() : music.getImage();
                    Image cover = new Image(Objects.requireNonNull(
                            ScoreExporter.class.getResourceAsStream("/covers/" + imageName)));
                    gc.drawImage(cover, leftX, bottomY, 300, 300);
                } catch (Exception ignored) {}

                customFont = loadCustomFont("/fonts/vivid-stasis-default-font.ttf", 45);
                gc.setFont(customFont);
                drawCenteredTextWithOutline(gc, String.valueOf(score.getIndividualScores().get(i)), rightX + 2, bottomY + 2, rightCellW, (bottomCellH * 7) / 8, Color.web("#2D0013"), Color.web("#2D0013"), 5);
                drawCenteredTextWithOutline(gc, String.valueOf(score.getIndividualScores().get(i)), rightX, bottomY, rightCellW, (bottomCellH * 7) / 8, Color.web("#FF4294"), Color.web("#700030"), 5);

                try {
                    Image rank = RankUtil.getRankImage(score.getIndividualRanks().get(i));
                    drawCenteredImage(gc, rank, rightX, bottomY + bottomCellH/2, rightCellW, bottomCellH/2, false);
                } catch (Exception ignored) {}

                // Difficulté musique
                double diffValue = 0.0;
                String diffName = "";
                switch (diff){
                    case OP : {
                        diffValue = music.getOpDiff();
                        diffName = "OPENING";
                        gc.setFill(Color.web("#00ff21"));
                        break;
                    }
                    case MD : {
                        diffValue = music.getMdDiff();
                        diffName = "MIDDLE";
                        gc.setFill(Color.web("#0194ff"));
                        break;
                    }
                    case FN : {
                        diffValue = music.getFnDiff();
                        diffName = "FINALE";
                        gc.setFill(Color.web("#ff0000"));
                        break;
                    }
                    case EC : {
                        diffValue = music.getEcDiff();
                        diffName = "ENCORE";
                        gc.setFill(Color.web("#b200ff"));
                        break;
                    }
                    case BS : {
                        diffValue = music.getBsDiff();
                        diffName = "BACKSTAGE";
                        gc.setFill(Color.web("#ff00e6"));
                        break;
                    }
                    case SH : {
                        diffValue = music.getShDiff();
                        diffName = "SHATTER";
                        gc.setFill(Color.web("#808080"));
                        break;
                    }
                }
                customFont = loadCustomFont("/fonts/vivid-stasis-default-font.ttf", 27);
                gc.setFont(customFont);
                drawCenteredText(gc,diffName + " " + diffValue, rightX, topY, rightCellW, topCellH);
            }

            // === Export PNG ===
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Exporter le score en image");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image PNG", "*.png"));
            fileChooser.setInitialFileName("Score_" + score.getCourseName().replaceAll("\\s+", "_") + ".png");

            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                Image snapshot = canvas.snapshot(null, null);
                ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", file);
                NotificationUtil.showToast(stage, "Score exporté avec succès !", "entrySavedAlert");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void exportScore(Stage stage, List<Integer> enteredScores, List<Music> musics, Course selectedCourse, Course.CourseDifficulty selectedDifficulty){
        try {
            double width = 1920;
            double height = 1080;

            Canvas canvas = new Canvas(width, height);
            GraphicsContext gc = canvas.getGraphicsContext2D();

            Map<Course.CourseDifficulty, Course.CourseDifficultySection> difficulties = selectedCourse.getDifficulties();
            Course.CourseDifficultySection section = difficulties.get(selectedDifficulty);

            // === Fond selon le thème ===
            String theme = UserPreferences.getInstance().getTheme(); // par ex. "red", "blue", ...
            try {
                Image bg = new Image(Objects.requireNonNull(
                        ScoreExporter.class.getResourceAsStream("/com/example/customcourses/themebg/" + theme + "ExportBG.png")));
                gc.drawImage(bg, 0, 0, width, height);
            } catch (Exception e) {
                gc.setFill(Color.web("#ff0000"));
                gc.fillRect(0, 0, width, height);
            }

            // --- Username ---
            String username = UserPreferences.getInstance().getUserName();
            gc.setFill(Color.WHITE);
            Font customFont = loadCustomFont("/fonts/vivid-stasis-default-font.ttf", 54);
            gc.setFont(customFont);
            drawCenteredText(gc, username, 48, 48, 936, 66);

            // --- Title image ---
            Title selectedTitle = Title.getSelectedTitle();
            if (selectedTitle != null) {
                Image titleImage = loadTitleImage(selectedTitle.getImagePath());
                if (titleImage != null) {
                    gc.drawImage(titleImage, 996, 42);
                }
            }

            // === Bloc gauche (informations de course) ===
            double courseX = 36;
            double topCourseY = 174;
            double midCourseY = 540;
            double botCourseY = 984;

            String courseName = selectedCourse.getName();
            Path coursesFile;
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> courses = null;
            String courseType = null;

            if (courseName.contains("(Legacy")){
                coursesFile = DataInitializer.getDataDirectory().resolve("coursesLegacy.json");
                courses = mapper.readValue(new File(coursesFile.toString()), new TypeReference<>() {});
                courseType = "L";
            }
            else if (!courseName.contains("SAMPLE")){
                coursesFile = DataInitializer.getDataDirectory().resolve("hiddenCourses.json");
                courses = mapper.readValue(new File(coursesFile.toString()), new TypeReference<>() {});
                courseType = "H";
                if (courses.stream().noneMatch(c -> courseName.equals(c.get("name")))) {
                    coursesFile = DataInitializer.getDataDirectory().resolve("courses.json");
                    courses = mapper.readValue(new File(coursesFile.toString()), new TypeReference<>() {});
                    courseType = "C";
                }
            }
            String pre = "";
            if (!courseName.contains("SAMPLE")){
                assert courses != null;
                for (Map<String, Object> course : courses){
                    if (courseName.equals(course.get("name"))) {
                        String index = String.valueOf(courses.indexOf(course) + 1);
                        System.out.println(index);
                        switch (courseType){
                            case "L" -> pre = "lc" + index;
                            case "H" -> pre = "hc" + index;
                            case "C" -> pre = "cc" + index;
                            default -> throw new IllegalStateException("Unexpected value: " + courseType);
                        }
                        System.out.println(pre);
                    }
                }
            }
            else {
                pre = "sample";
                System.out.println(pre);
            }

            Image courseImage = new Image(Objects.requireNonNull(ScoreExporter.class.getResourceAsStream("/com/example/customcourses/coursebg/" + pre +"ExportBG.png")));
            gc.drawImage(courseImage, courseX, topCourseY);

            int totalScore = enteredScores.stream().mapToInt(Integer::intValue).sum();
            String totalRank = RankUtil.calculateCourseRank(totalScore, musics.size());

            customFont = loadCustomFont("/fonts/vivid-stasis-default-font.ttf", 72);
            gc.setFont(customFont);
            drawCenteredTextWithOutline(gc, String.valueOf(totalScore), courseX + 4, midCourseY + 4, 552, 378, Color.web("#2D0013"), Color.web("#2D0013"), 8);
            drawCenteredTextWithOutline(gc, String.valueOf(totalScore), courseX, midCourseY, 552, 378, Color.web("#FF4294"), Color.web("#700030"), 8);

            try {
                Image rankImg = RankUtil.getRankImage(totalRank);
                drawCenteredImage(gc, rankImg, courseX, midCourseY + 216, 552, 216, true);
            } catch (Exception ignored) {}

            String courseDiffName = selectedDifficulty.toString();
            boolean isExtra = false;

            switch (courseDiffName){
                case "EXPOSITION" -> gc.setFill(Color.ORANGE);
                case "TWIST" -> gc.setFill(Color.web("#00ff21"));
                case "INTERLUDE" -> gc.setFill(Color.web("#0194ff"));
                case "CLIMAX" -> gc.setFill(Color.web("#ff0000"));
                case "APOTHEOSIS" -> gc.setFill(Color.web("#b200ff"));
                case "DESTROYED" -> gc.setFill(Color.WHITE);
                case "EXTRA" -> {
                    isExtra = true;
                    customFont = loadCustomFont("/fonts/vivid-stasis-default-font.ttf", 54);
                    gc.setFont(customFont);

                    drawCenteredMulticolorText(gc, "EXTRA", courseX, botCourseY, 552, 66);
                }
            }

            if (!isExtra) {
                customFont = loadCustomFont("/fonts/vivid-stasis-default-font.ttf", 54);
                gc.setFont(customFont);
                drawCenteredText(gc, courseDiffName, courseX, botCourseY, 552, 66);
            }
            // === Grille des 4 musiques ===
            double leftStartX = 616;
            double rightStartX = 919;
            double topStartY = 184;
            double bottomStartY = 283;

            double leftCellW = 300;
            double rightCellW = 330;
            double topCellH = 96;
            double bottomCellH = 300;

            double leftSpacingX = 355;
            double rightSpacingX = 325;
            double topSpacingY = 351;
            double bottomSpacingY = 147;

            for (int i = 0; i < musics.size(); i++) {
                Music music = musics.get(i);
                Course.MusicDifficulty diff = section.getDifficultyLevels().get(i);

                int row = i / 2;  // 0 or 1
                int col = i % 2;  // 0 or 1

                double leftX   = leftStartX  + col * (leftCellW  + leftSpacingX);
                double topY    = topStartY   + row * (topCellH   + topSpacingY);
                double rightX  = rightStartX + col * (rightCellW + rightSpacingX);
                double bottomY = bottomStartY+ row * (bottomCellH+ bottomSpacingY);

                // Nom musique
                gc.setFill(Color.WHITE);
                customFont = loadCustomFont("/fonts/vivid-stasis-default-font.ttf", 27);
                gc.setFont(customFont);
                drawCenteredText(gc, music.getTitle(), leftX, topY, leftCellW, topCellH / 2);

                gc.setFill(Color.web("#aaaaaa"));
                customFont = loadCustomFont("/fonts/vivid-stasis-default-font.ttf", 18);
                gc.setFont(customFont);
                drawCenteredText(gc, music.getArtist(), leftX, topY + topCellH/2, leftCellW, topCellH/2);

                // Jaquette
                try {
                    String imageName = (diff == Course.MusicDifficulty.BS) ? music.getBsImage() : music.getImage();
                    Image cover = new Image(Objects.requireNonNull(
                            ScoreExporter.class.getResourceAsStream("/covers/" + imageName)));
                    gc.drawImage(cover, leftX, bottomY, 300, 300);
                } catch (Exception ignored) {}

                customFont = loadCustomFont("/fonts/vivid-stasis-default-font.ttf", 45);
                gc.setFont(customFont);
                drawCenteredTextWithOutline(gc, String.valueOf(enteredScores.get(i)), rightX + 2, bottomY + 2, rightCellW, (bottomCellH * 7) / 8, Color.web("#2D0013"), Color.web("#2D0013"), 5);
                drawCenteredTextWithOutline(gc, String.valueOf(enteredScores.get(i)), rightX, bottomY, rightCellW, (bottomCellH * 7) / 8, Color.web("#FF4294"), Color.web("#700030"), 5);

                String scoreRank = RankUtil.calculateMusicRank(enteredScores.get(i));
                try {
                    Image rank = RankUtil.getRankImage(scoreRank);
                    drawCenteredImage(gc, rank, rightX, bottomY + bottomCellH/2, rightCellW, bottomCellH/2, false);
                } catch (Exception ignored) {}

                // Difficulté musique
                double diffValue = 0.0;
                String diffName = "";
                switch (diff){
                    case OP : {
                        diffValue = music.getOpDiff();
                        diffName = "OPENING";
                        gc.setFill(Color.web("#00ff21"));
                        break;
                    }
                    case MD : {
                        diffValue = music.getMdDiff();
                        diffName = "MIDDLE";
                        gc.setFill(Color.web("#0194ff"));
                        break;
                    }
                    case FN : {
                        diffValue = music.getFnDiff();
                        diffName = "FINALE";
                        gc.setFill(Color.web("#ff0000"));
                        break;
                    }
                    case EC : {
                        diffValue = music.getEcDiff();
                        diffName = "ENCORE";
                        gc.setFill(Color.web("#b200ff"));
                        break;
                    }
                    case BS : {
                        diffValue = music.getBsDiff();
                        diffName = "BACKSTAGE";
                        gc.setFill(Color.web("#ff00e6"));
                        break;
                    }
                    case SH : {
                        diffValue = music.getShDiff();
                        diffName = "SHATTER";
                        gc.setFill(Color.web("#808080"));
                        break;
                    }
                }
                customFont = loadCustomFont("/fonts/vivid-stasis-default-font.ttf", 27);
                gc.setFont(customFont);
                drawCenteredText(gc,diffName + " " + diffValue, rightX, topY, rightCellW, topCellH);
            }

            // === Export PNG ===
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Exporter le score en image");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image PNG", "*.png"));
            fileChooser.setInitialFileName("Score_" + selectedCourse.getName().replaceAll("\\s+", "_") + ".png");

            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                Image snapshot = canvas.snapshot(null, null);
                ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", file);
                NotificationUtil.showToast(stage, "Score exporté avec succès !", "entrySavedAlert");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void drawCenteredText(GraphicsContext gc, String text, double x, double y, double w, double h) {
        Text tempText = new Text(text);
        tempText.setFont(gc.getFont());
        double textWidth = tempText.getLayoutBounds().getWidth();
        double textHeight = tempText.getLayoutBounds().getHeight();

        // Centrer le texte à l'intérieur du rectangle [x, y, w, h]
        double textX = x + (w - textWidth) / 2;
        double textY = y + (h + textHeight) / 2 - textHeight * 0.2; // ajustement vertical léger pour centrer le rendu

        gc.fillText(text, textX, textY);
    }

    private static void drawCenteredMulticolorText(GraphicsContext gc, String text, double x, double y, double w, double h) {
        Color[] colors = { Color.web("#00ff21"), Color.web("#0194ff"), Color.web("#ff0000"), Color.web("#ff00e6"), Color.web("#b200ff")};

        // Calcul de la largeur totale du texte
        Text tempText = new Text(text);
        tempText.setFont(gc.getFont());
        double textWidth = tempText.getLayoutBounds().getWidth();
        double textHeight = tempText.getLayoutBounds().getHeight();

        // Position de départ pour centrer
        double startX = x + (w - textWidth) / 2;
        double baseY = y + (h + textHeight) / 2 - textHeight * 0.2;

        double currentX = startX;

        for (int i = 0; i < text.length(); i++) {
            gc.setFill(colors[i % colors.length]);
            String letter = String.valueOf(text.charAt(i));
            Text t = new Text(letter);
            t.setFont(gc.getFont());
            double letterWidth = t.getLayoutBounds().getWidth();

            gc.fillText(letter, currentX, baseY);
            currentX += letterWidth;
        }
    }

    private static void drawCenteredTextWithOutline(GraphicsContext gc, String text, double x, double y, double w, double h, Color textColor, Color outlineColor, double outlineThickness) {
        Text tempText = new Text(text);
        tempText.setFont(gc.getFont());

        double textWidth = tempText.getLayoutBounds().getWidth();
        double textHeight = tempText.getLayoutBounds().getHeight();

        double textX = x + (w - textWidth) / 2;
        double textY = y + (h + textHeight) / 2 - textHeight * 0.2;

        // Étape 1 : créer un canvas temporaire
        Canvas tempCanvas = new Canvas(textWidth + outlineThickness * 4, textHeight + outlineThickness * 4);
        GraphicsContext tg = tempCanvas.getGraphicsContext2D();
        tg.setFont(gc.getFont());

        double drawX = outlineThickness * 2;

        // Étape 2 : dessiner le texte sur un snapshot pour générer un masque
        tg.setFill(Color.WHITE);
        tg.fillText(text, drawX, textHeight);
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        WritableImage textImage = tempCanvas.snapshot(params, null);

        // Étape 3 : créer une image du contour
        int width = (int) textImage.getWidth();
        int height = (int) textImage.getHeight();
        WritableImage outlineImage = new WritableImage(width, height);
        PixelReader reader = textImage.getPixelReader();
        PixelWriter writer = outlineImage.getPixelWriter();

        for (int y2 = 0; y2 < height; y2++) {
            for (int x2 = 0; x2 < width; x2++) {
                boolean hasOpaqueNeighbor = false;
                for (int oy = -((int) outlineThickness); oy <= outlineThickness && !hasOpaqueNeighbor; oy++) {
                    for (int ox = -((int) outlineThickness); ox <= outlineThickness; ox++) {
                        int nx = x2 + ox, ny = y2 + oy;
                        if (nx >= 0 && ny >= 0 && nx < width && ny < height) {
                            if (reader.getColor(nx, ny).getOpacity() > 0.1) {
                                hasOpaqueNeighbor = true;
                                break;
                            }
                        }
                    }
                }
                if (hasOpaqueNeighbor) writer.setColor(x2, y2, outlineColor);
            }
        }

        // Étape 4 : combiner contour + texte
        tg.clearRect(0, 0, width, height);
        tg.drawImage(outlineImage, 0, 0);
        tg.setFill(textColor);
        tg.fillText(text, drawX, textHeight);
        WritableImage finalImage = tempCanvas.snapshot(params, null);

        // Étape 5 : dessiner le résultat centré sur le GraphicsContext principal
        gc.drawImage(finalImage, textX - outlineThickness * 2, textY - textHeight + outlineThickness * 2);
    }

    private static void drawCenteredImage(GraphicsContext gc, Image img, double x, double y, double w, double h, boolean doubleValue) {
        if (img == null) return;

        double imgWidth = img.getWidth();
        double imgHeight = img.getHeight();

        if (doubleValue){
            imgWidth *= 2;
            imgHeight *= 2;
        }

        // Calcul des coordonnées pour centrer l’image dans la zone donnée
        double imgX = x + (w - imgWidth) / 2;
        double imgY = y + (h - imgHeight) / 2;

        gc.drawImage(img, imgX, imgY, imgWidth, imgHeight);
    }

    public static Font loadCustomFont(String path, double size) {
        try (InputStream fontStream = ScoreExporter.class.getResourceAsStream(path)) {
            if (fontStream != null) {
                return Font.loadFont(fontStream, size);
            } else {
                System.err.println("Police introuvable : " + path);
                return Font.font("Arial", size);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Font.font("Arial", size);
        }
    }

    private static Image loadTitleImage(String imagePath) {
        try {
            InputStream imgStream = ScoreExporter.class.getResourceAsStream(imagePath);
            if (imgStream != null) return new Image(imgStream);

            File file = new File(imagePath);
            if (file.exists()) return new Image(file.toURI().toString());

            System.err.println("Title image unreachable : " + imagePath);
            return null;
        } catch (Exception e) {
            System.err.println("Title image error during loading : " + e.getMessage());
            return null;
        }
    }
}
