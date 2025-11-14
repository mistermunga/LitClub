package com.litclub.ui.main;

import com.litclub.theme.ThemeManager;
import com.litclub.ui.main.shared.HeaderBar;
import javafx.scene.layout.BorderPane;

public class MainPage extends BorderPane {

    private final boolean isPersonal;

    public MainPage(boolean isPersonal) {
        this.isPersonal = isPersonal;
        ThemeManager.getInstance().registerComponent(this);
        this.getStyleClass().add("root");
        showHeader();
    }

    private void showHeader() {
        HeaderBar headerBar = new HeaderBar();
        this.setTop(headerBar);
    }

}
