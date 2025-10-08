module com.litclub {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    opens com.litclub to javafx.fxml;

    exports com.litclub;
}
