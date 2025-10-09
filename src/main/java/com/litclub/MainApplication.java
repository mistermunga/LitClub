package com.litclub;

import com.litclub.session.AppSession;
import com.litclub.session.construct.ClubRecord;
import com.litclub.theme.ThemeManager;
import com.litclub.ui.ClubPage;
import com.litclub.ui.CrossRoadsPage;
import com.litclub.ui.LandingPage;
import com.litclub.ui.LoginPage;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.List;

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
        primaryStage.setTitle("LitClub Desktop");

        primaryStage.setHeight(600);
        primaryStage.setWidth(550);
        showLanding();
    }

    public void showLanding() {
        LandingPage landingPage = new LandingPage();
        Scene scene = new Scene(landingPage);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void showLogin() {
        ThemeManager.getInstance().clearRegisteredComponents();
        LoginPage loginPage = new LoginPage();
        Scene scene = new Scene(loginPage);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // TODO Another API call, find all user clubs
    public void showCrossRoads() {
        List<String> clubs = List.of("Society of the Bard", "Gumshoe Sleuthers");
        ThemeManager.getInstance().clearRegisteredComponents();
        CrossRoadsPage crossRoadsPage = new CrossRoadsPage(clubs);
        Scene scene = new Scene(crossRoadsPage);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void showClubPage(String clubName) {
        ThemeManager.getInstance().clearRegisteredComponents();

        // TODO [CRITICAL] API call, get club details
        boolean adminPermission = clubName.equals("Society of the Bard");
        ClubRecord cr = new ClubRecord(clubName, adminPermission);
        AppSession.getInstance().setClubDetails(cr);

        ClubPage clubPage = new ClubPage();
        Scene scene = new Scene(clubPage);
        primaryStage.setTitle(clubName);
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.resizableProperty().setValue(false);
        primaryStage.show();
    }

    public void showMainPage() {}

    public static void main(String[] args) {
        launch(args);
    }
}
