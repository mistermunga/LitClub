package com.litclub;

import com.litclub.ui.LandingPage;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApplication extends Application {

    private static MainApplication instance;
    private Stage primaryStage = new Stage();

    public static MainApplication getInstance() {
        return instance;
    }

    @Override
    public void start(Stage primaryStage) {
        instance = this;
        primaryStage.setTitle("LitClub Desktop");
        showLanding();
    }

    public void showLanding() {
        LandingPage landingPage = new LandingPage();
        Scene scene = new Scene(landingPage);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void showLogin() {}

    public static void main(String[] args) {
        launch(args);
    }
}
