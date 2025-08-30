module com.example.customcourses {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires animated.gif.lib;
    requires java.desktop;
    requires javafx.swing;
    requires com.google.gson;

    opens com.example.customcourses to javafx.fxml;
    exports com.example.customcourses;
    exports com.example.customcourses.controllers;
    opens com.example.customcourses.controllers to javafx.fxml;
    exports com.example.customcourses.utils;
    opens com.example.customcourses.utils to javafx.fxml;
    opens com.example.customcourses.models to com.fasterxml.jackson.databind, com.google.gson;
}