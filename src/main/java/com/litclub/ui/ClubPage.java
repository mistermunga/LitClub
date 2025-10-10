package com.litclub.ui;

import com.litclub.theme.ThemeManager;
import com.litclub.ui.component.HeaderBar;
import javafx.scene.layout.BorderPane;

public class ClubPage extends BorderPane {

    public ClubPage() {
        ThemeManager.getInstance().registerComponent(this);
        this.getStyleClass().add("root");
        showHeader();
        showDashboard();
    }

    private void showHeader() {
        HeaderBar headerBar = new HeaderBar();
        this.setTop(headerBar);
    }

    public void showDashboard() {
    }
}
