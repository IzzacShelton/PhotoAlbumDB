module cm336.albumapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires com.drew.metadata;
    requires java.sql;
    requires atlantafx.base;

    opens cm336.albumapp to javafx.fxml;
    opens cm336.albumapp.controller to javafx.fxml;

    exports cm336.albumapp;
    exports cm336.albumapp.controller;
    exports cm336.albumapp.model;
}