package com.litclub.ui.main.personal;

import javafx.scene.layout.BorderPane;

public class MainPersonalPage extends BorderPane {

    private final boolean isAdmin;

    public MainPersonalPage(boolean isAdministrator) {
        this.isAdmin = isAdministrator;
    }
}
