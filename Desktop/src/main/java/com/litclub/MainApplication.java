package com.litclub;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

public class MainApplication extends Application {

    private static MainApplication instance;
    private Stage primaryStage;

    public static MainApplication getInstance() {
        return instance;
    }

    static {System.setProperty("file.encoding", "UTF-8");}

    @Override
    public void start(Stage primaryStage) {
        instance = this;
        this.primaryStage = primaryStage;

        primaryStage.getIcons().add(
                new Image(Objects.requireNonNull(getClass().getResourceAsStream(
                        "/com/litclub/ui/icons/LitClub_logo_blackText_mini.png"
                ))
        ));

        SceneManager.getInstance().showLanding();
    }

    @Override
    public void stop() {
        CacheManager.getInstance().clearCache();
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}