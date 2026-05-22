module org.example.musicplayergruppo9 {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.musicplayergruppo9 to javafx.fxml;
    exports org.example.musicplayergruppo9;
}