module com.litclub {
    requires javafx.controls;
    requires javafx.fxml;

    // If you later use FXML controllers, this allows FXMLLoader to access them reflectively
    opens com.litclub to javafx.fxml;

    // Export your main package so JavaFX can find MainApplication
    exports com.litclub;
}
