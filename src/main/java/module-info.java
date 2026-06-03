module org.example.musicplayergruppo9 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    requires transitive javafx.graphics; 



    requires java.sql;
    requires jlayer; // per il database in SQLite

    opens org.example.musicplayergruppo9 to javafx.fxml;
    exports org.example.musicplayergruppo9;

    exports org.example.musicplayergruppo9.pattern.state;

    exports org.example.musicplayergruppo9.pattern.strategy;
    
    exports org.example.musicplayergruppo9.controller;
    exports org.example.musicplayergruppo9.model;
    opens org.example.musicplayergruppo9.controller to javafx.fxml;
    exports org.example.musicplayergruppo9.service;
    opens org.example.musicplayergruppo9.service to javafx.fxml;
}