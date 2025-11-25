package com.litclub;

import com.litclub.session.AppSession;
import com.litclub.theme.ThemeManager;
import com.litclub.ui.crossroads.CrossRoadsPage;
import com.litclub.ui.LandingPage;
import com.litclub.ui.authentication.LoginPage;
import com.litclub.ui.authentication.RegistrationPage;
import com.litclub.ui.main.MainPage;
import com.litclub.ui.main.shared.event.EventBus;
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
        stage.setWidth(560);

        stage.setScene(scene);
        stage.show();
    }

    public void showLogin() {
        themeManager.clearRegisteredComponents();
        Stage stage = application.getPrimaryStage();
        LoginPage loginPage = new LoginPage();
        Scene scene = new Scene(loginPage);

        stage.setTitle("LitClub Desktop - Login");
        stage.setMaximized(false);
        stage.setResizable(true);
        stage.setHeight(600);
        stage.setWidth(560);
        stage.setScene(scene);
        stage.show();
    }

    public void showRegistration() {
        themeManager.clearRegisteredComponents();
        Stage stage = application.getPrimaryStage();
        RegistrationPage registrationPage = new RegistrationPage();
        Scene scene = new Scene(registrationPage);

        stage.setTitle("LitClub Desktop - Registration");
        stage.setScene(scene);
        stage.show();
    }

    public void showCrossRoads() {
        themeManager.clearRegisteredComponents();
        Stage stage = application.getPrimaryStage();
        CrossRoadsPage crossRoadsPage = new CrossRoadsPage();
        Scene scene = new Scene(crossRoadsPage);

        stage.setTitle("LitClub Desktop - Crossroads");
        stage.setMaximized(true);
        stage.setResizable(true);
        stage.setScene(scene);
        stage.show();
    }

    public void showMainPage(boolean isPersonal) {
        themeManager.clearRegisteredComponents();
        Stage stage = application.getPrimaryStage();
        MainPage mainPage = new MainPage(isPersonal);
        Scene scene = new Scene(mainPage);

        if (isPersonal) {
            EventBus.getInstance().off(EventBus.clubEvents());
            stage.setTitle("LitClub Desktop - " + AppSession.getInstance().getUserRecord().username());
        } else {
            EventBus.getInstance().off(EventBus.personalEvents());
            stage.setTitle("LitClub Desktop - " + AppSession.getInstance().getCurrentClub().getClubName());
        }
        stage.setMaximized(true);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }
}
