package com.example.customcourses.utils;

import com.example.customcourses.models.Course;
import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class CourseLoader {

    public static List<Course> loadCoursesFromFile(Path jsonFilePath) throws IOException {
        String json = Files.readString(jsonFilePath);
        // Exemple avec Gson :
        Gson gson = new Gson();
        Course[] courses = gson.fromJson(json, Course[].class);
        return Arrays.asList(courses);
    }
}
