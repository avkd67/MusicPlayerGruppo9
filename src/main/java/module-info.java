module org.example.musicplayergruppo9 {
    requires javafx.controls;
    requires javafx.fxml;


    requires java.sql; // per il database in SQLite

    opens org.example.musicplayergruppo9 to javafx.fxml;
    exports org.example.musicplayergruppo9;
    exports org.example.musicplayergruppo9.controller;
    opens org.example.musicplayergruppo9.controller to javafx.fxml;
}