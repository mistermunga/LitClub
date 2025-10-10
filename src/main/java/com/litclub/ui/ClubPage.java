package com.litclub.ui;

import com.litclub.theme.ThemeManager;
import com.litclub.ui.component.ContentArea;
import com.litclub.ui.component.HeaderBar;
import com.litclub.ui.component.NavigationSideBar;
import com.litclub.ui.component.RightPanel;
import com.litclub.ui.component.subunits.ThemeToggleBar;
import javafx.scene.layout.BorderPane;

public class ClubPage extends BorderPane {

    public ClubPage() {
        ThemeManager.getInstance().registerComponent(this);
        this.getStyleClass().add("root");
        showHeader();
        showDashboard();
        showBottom();
    }

    private void showHeader() {
        HeaderBar headerBar = new HeaderBar();
        this.setTop(headerBar);
    }

    public void showDashboard() {
        ContentArea contentArea = new ContentArea();
        RightPanel rightPanel = new RightPanel();
        NavigationSideBar  navigationSideBar = new NavigationSideBar(contentArea);

        this.setCenter(contentArea);
        this.setRight(rightPanel);
        this.setLeft(navigationSideBar);
    }

    public void showBottom() {
        ThemeToggleBar  themeToggleBar = new ThemeToggleBar();
        this.setBottom(themeToggleBar);
    }
}
