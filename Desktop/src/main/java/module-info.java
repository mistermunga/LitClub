module com.litclub {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.desktop;
    requires com.google.gson;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires java.net.http;

    opens com.litclub to javafx.fxml, com.google.gson, com.fasterxml.jackson.databind;
    opens com.litclub.client.api to com.fasterxml.jackson.databind, com.google.gson, javafx.fxml, com.fasterxml.jackson.datatype.jsr310;
    opens com.litclub.persistence to com.google.gson;
    opens com.litclub.construct to com.google.gson;
    opens com.litclub.construct.mock to com.google.gson;

    exports com.litclub;
    exports com.litclub.client.api;
    opens com.litclub.construct.record.user to com.google.gson;
}