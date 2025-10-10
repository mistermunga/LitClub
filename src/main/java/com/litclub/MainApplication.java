package com.litclub;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MainApplication extends Application {

    private static MainApplication instance;
    private Stage primaryStage;

    public static MainApplication getInstance() {
        return instance;
    }

    @Override
    public void start(Stage primaryStage) {
        instance = this;
        this.primaryStage = primaryStage;

        // One-time stage setup only
        primaryStage.getIcons().add(
                new Image(getClass().getResourceAsStream(
                        "/com/litclub/ui/icons/LitClub_logo_blackText_mini.png"
                ))
        );

        // Delegate to SceneManager for all navigation
        SceneManager.getInstance().showLanding();
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}