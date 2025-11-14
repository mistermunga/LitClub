package com.litclub.ui.crossroads.components.subcomponents;

import com.litclub.construct.Club;
import com.litclub.ui.crossroads.util.DateFormatter;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

/**
 * Card representing a single club
 */
public class ClubCard extends VBox {

    public ClubCard(Club club, Consumer<Club> onNavigate) {
        super(12);
        getStyleClass().add("card");
        setPrefWidth(280);
        setMinHeight(200);
        setAlignment(Pos.TOP_LEFT);
        setPadding(new Insets(20));
        setCursor(javafx.scene.Cursor.HAND);

        // Club icon
        Label icon = new Label("📖");
        icon.setStyle("-fx-font-size: 36px;");

        // Club name
        Label name = new Label(club.getClubName());
        name.getStyleClass().add("section-title");
        name.setStyle("-fx-font-size: 18px;");
        name.setWrapText(true);
        name.setMaxWidth(240);

        // Description
        Label description = new Label(
                club.getDescription() != null && !club.getDescription().isEmpty()
                        ? club.getDescription()
                        : "No description"
        );
        description.getStyleClass().add("section-subtitle");
        description.setWrapText(true);
        description.setMaxWidth(240);
        description.setMaxHeight(60);

        // Stats section
        HBox stats = new HBox(10);
        stats.setAlignment(Pos.CENTER_LEFT);

        // Created date
        if (club.getCreatedAt() != null) {
            Label createdDate = new Label("Created " + DateFormatter.formatRelative(club.getCreatedAt()));
            createdDate.getStyleClass().add("text-muted");
            createdDate.setStyle("-fx-font-size: 11px; -fx-font-style: italic;");
            stats.getChildren().add(createdDate);
        }

        getChildren().addAll(icon, name, description, stats);

        // Click handler
        setOnMouseClicked(e -> onNavigate.accept(club));

        // Hover effects
        setOnMouseEntered(e -> setStyle("-fx-scale-x: 1.03; -fx-scale-y: 1.03;"));
        setOnMouseExited(e -> setStyle("-fx-scale-x: 1.0; -fx-scale-y: 1.0;"));
    }
}
