module com.litclub {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.desktop;
    requires com.google.gson;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires java.net.http;
    requires com.fasterxml.jackson.annotation;

    opens com.litclub to javafx.fxml;

    exports com.litclub;
}
