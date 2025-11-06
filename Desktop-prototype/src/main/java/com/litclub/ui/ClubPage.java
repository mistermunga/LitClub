package com.litclub.ui;

import com.litclub.theme.ThemeManager;
import com.litclub.ui.component.ContentArea;
import com.litclub.ui.component.HeaderBar;
import com.litclub.ui.component.NavigationSideBar;
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
        ContentArea contentArea = new ContentArea();
        NavigationSideBar  navigationSideBar = new NavigationSideBar(contentArea);

        this.setCenter(contentArea);
        this.setLeft(navigationSideBar);
    }
}
