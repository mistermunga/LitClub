package com.litclub;

import com.litclub.session.AppSession;
import com.litclub.session.construct.ClubRecord;
import com.litclub.theme.ThemeManager;
import com.litclub.ui.*;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.List;

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

    public void showLogin() {
        themeManager.clearRegisteredComponents();
        Stage stage = application.getPrimaryStage();
        LoginPage loginPage = new LoginPage();
        Scene scene = new Scene(loginPage);

        stage.setTitle("LitClub Desktop - Login");
        stage.setScene(scene);
        stage.show();
    }

    public void showCrossRoads() {
        // TODO: API call to get user clubs
        List<String> clubs = List.of("Society of the Bard", "Gumshoe Sleuthers");

        themeManager.clearRegisteredComponents();
        Stage stage = application.getPrimaryStage();
        CrossRoadsPage crossRoadsPage = new CrossRoadsPage(clubs);
        Scene scene = new Scene(crossRoadsPage);

        stage.setTitle("LitClub Desktop - My Clubs");
        stage.setScene(scene);
        stage.show();
    }

    public void showClubPage(String clubName) {
        themeManager.clearRegisteredComponents();

        // TODO [CRITICAL]: API call to get club details
        boolean adminPermission = clubName.equals("Society of the Bard");
        ClubRecord cr = new ClubRecord(clubName, adminPermission);
        session.setClubDetails(cr);

        Stage stage = application.getPrimaryStage();
        ClubPage clubPage = new ClubPage();
        Scene scene = new Scene(clubPage);

        stage.setTitle(clubName);
        stage.setMaximized(true);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    public void showMainPage() {
        // TODO: Implement main page navigation
    }
}