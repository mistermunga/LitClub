package com.litclub.ui.crossroads.components.subcomponents;

import com.litclub.ui.crossroads.service.CrossRoadsService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

/**
 * Personal card for accessing user's library and settings
 */
public class MeCard extends VBox {

    public MeCard(CrossRoadsService service, Runnable onNavigate) {
        super(15);
        getStyleClass().add("card");
        setPrefWidth(280);
        setMinHeight(200);
        setAlignment(Pos.TOP_CENTER);
        setPadding(new Insets(20));
        setCursor(javafx.scene.Cursor.HAND);

        // Icon/Avatar
        Label icon = new Label("📚");
        icon.setStyle("-fx-font-size: 48px;");

        // Title
        Label title = new Label("Me");
        title.getStyleClass().add("section-title");
        title.setStyle("-fx-font-size: 22px;");

        // Subtitle
        Label subtitle = new Label("Personal Library & Settings");
        subtitle.getStyleClass().add("section-subtitle");
        subtitle.setWrapText(true);
        subtitle.setTextAlignment(TextAlignment.CENTER);
        subtitle.setMaxWidth(240);

        // Stats
        VBox stats = new VBox(5);
        stats.setAlignment(Pos.CENTER);

        Label clubCount = new Label("Member of " + service.getClubs().size() + " clubs");
        clubCount.getStyleClass().add("text-muted");
        clubCount.setStyle("-fx-font-size: 12px;");

        stats.getChildren().add(clubCount);

        // Admin badge (if applicable)
        if (service.isAdmin()) {
            Label adminBadge = new Label("Administrator");
            adminBadge.getStyleClass().add("meeting-club-badge");
            adminBadge.setStyle("-fx-font-size: 11px;");
            stats.getChildren().add(adminBadge);
        }

        getChildren().addAll(icon, title, subtitle, stats);

        // Click handler
        setOnMouseClicked(e -> {
            System.out.println("MeCard clicked! Event: " + e);
            System.out.println("Component still in scene: " + (getScene() != null));
            onNavigate.run();
        });

        // Hover effects
        setOnMouseEntered(e -> setStyle("-fx-scale-x: 1.03; -fx-scale-y: 1.03;"));
        setOnMouseExited(e -> setStyle("-fx-scale-x: 1.0; -fx-scale-y: 1.0;"));
    }
}
