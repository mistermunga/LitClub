package com.litclub.ui;

import com.litclub.theme.ThemeManager;
import com.litclub.ui.component.ThemeToggleBar;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.*;

public class CrossRoadsPage extends VBox {

    private Set<String> clubs = new HashSet<>();
    private static final int COLUMNS = 3;

    public CrossRoadsPage(List<String> clubs) {
        this.clubs.addAll(clubs);

        ThemeManager.getInstance().registerComponent(this);

        this.getStyleClass().add("root");
        this.setAlignment(Pos.CENTER);
        this.setSpacing(40);
        this.setPadding(new Insets(60, 40, 60, 40));

        showSelections();
        showToggleBar();
    }

    private void showSelections() {
        VBox container = new VBox();
        container.setSpacing(15);
        container.setAlignment(Pos.CENTER);

        Queue<String> queue = new ArrayDeque<>(clubs);

        while (!queue.isEmpty()) {
            HBox row = new HBox();
            ThemeManager.getInstance().registerComponent(row);
            row.setSpacing(15);
            row.setAlignment(Pos.CENTER);

            for (int i = 0; i < COLUMNS && !queue.isEmpty(); i++) {
                String clubName = queue.poll();
                Button clubButton = new Button(clubName);
                ThemeManager.getInstance().registerComponent(clubButton);
                clubButton.setMinWidth(150);
                clubButton.setPrefWidth(200);
                clubButton.setMaxWidth(200);
                clubButton.setWrapText(true);
                clubButton.getStyleClass().add("club-button");
                row.getChildren().add(clubButton);
            }

            container.getChildren().add(row);
        }

        this.getChildren().add(container);
    }

    private void showToggleBar() {
        VBox toggleContainer = new ThemeToggleBar();
        this.getChildren().add(toggleContainer);
    }
}