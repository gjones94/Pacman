module org.pacman {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires javafx.graphics;
    requires java.desktop;

    opens org.pacman to javafx.fxml;
    exports org.pacman;
}