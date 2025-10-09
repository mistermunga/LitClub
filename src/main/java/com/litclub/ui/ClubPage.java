package com.litclub.ui;

import com.litclub.theme.ThemeManager;
import com.litclub.ui.component.HeaderBar;
import javafx.scene.layout.VBox;

public class ClubPage extends VBox {

    public ClubPage() {
        ThemeManager.getInstance().registerComponent(this);
        this.getStyleClass().add("root");
        showHeader();
    }

    private void showHeader() {
        HeaderBar headerBar = new HeaderBar();
        this.getChildren().add(headerBar);
    }
}
