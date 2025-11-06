package com.litclub.theme;

import com.litclub.MainApplication;
import com.litclub.session.AppSession;

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

    public void showLanding() {}
}
