package com.litclub;

import com.litclub.session.AppSession;
import com.litclub.theme.ThemeManager;
import com.litclub.ui.LandingPage;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneManager {

    private static SceneManager instance;
    private final MainApplication application;
    private final AppSession session;
    private final ThemeManager themeManager;

    private SceneManager() {
        this.application = MainApplication.getInstance();
        this.session = AppSession.getInstance();
        this.themeManager = ThemeManager.getInstance();
    }

    public static SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    public void showLanding() {
        Stage stage = application.getPrimaryStage();
        LandingPage landingPage = new LandingPage();
        Scene scene = new Scene(landingPage);

        // Reset to default window settings
        stage.setTitle("LitClub Desktop");
        stage.setMaximized(false);
        stage.setResizable(true);
        stage.setHeight(600);
        stage.setWidth(550);

        stage.setScene(scene);
        stage.show();
    }
}
