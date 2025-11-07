module com.litclub {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.desktop;
    requires com.google.gson;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.datatype.jdk8;
    requires java.net.http;
    requires com.fasterxml.jackson.annotation;
    requires org.json;

    opens com.litclub to javafx.fxml;
    opens com.litclub.construct to com.fasterxml.jackson.databind, com.google.gson;
    opens com.litclub.construct.interfaces to com.fasterxml.jackson.databind, com.google.gson;
    opens com.litclub.construct.enums to com.fasterxml.jackson.databind, com.google.gson;
    opens com.litclub.construct.compositeKey to com.fasterxml.jackson.databind, com.google.gson;
    opens com.litclub.construct.interfaces.auth to com.fasterxml.jackson.databind, com.google.gson;
    opens com.litclub.construct.interfaces.club to com.fasterxml.jackson.databind, com.google.gson;
    opens com.litclub.construct.interfaces.note to com.fasterxml.jackson.databind, com.google.gson;
    opens com.litclub.construct.interfaces.user to com.fasterxml.jackson.databind, com.google.gson;
    opens com.litclub.construct.interfaces.review to com.fasterxml.jackson.databind, com.google.gson;
    opens com.litclub.construct.interfaces.library to com.fasterxml.jackson.databind, com.google.gson;
    opens com.litclub.construct.interfaces.meeting to com.fasterxml.jackson.databind, com.google.gson;
    opens com.litclub.construct.interfaces.discussion to com.fasterxml.jackson.databind, com.google.gson;

    exports com.litclub;
}
