package com.example.customcourses.utils;

import com.example.customcourses.models.Course;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class CourseLoader {

    public static List<Course> loadCoursesFromFile(Path jsonFilePath) throws IOException {
        String json = Files.readString(jsonFilePath);
        Gson gson = new Gson();
        Course[] courses = gson.fromJson(json, Course[].class);
        return Arrays.asList(courses);
    }
}
