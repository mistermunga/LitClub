module com.litclub {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.desktop;
    requires com.google.gson;
    requires com.fasterxml.jackson.databind;


    opens com.litclub to javafx.fxml, com.google.gson, com.fasterxml.jackson.databind;
    opens com.litclub.persistence to com.google.gson;

    exports com.litclub;
}
