package com.litclub.ui.crossroads.components;

import com.litclub.construct.Club;
import com.litclub.ui.crossroads.components.subcomponents.ClubCard;
import com.litclub.ui.crossroads.components.subcomponents.InviteCard;
import com.litclub.ui.crossroads.components.subcomponents.MeCard;
import com.litclub.ui.crossroads.service.CrossRoadsService;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

/**
 * Grid displaying user's clubs and "Me" card
 */
public class CardsGrid extends VBox {

    private final CrossRoadsService service;
    private final FlowPane grid;
    private final Runnable onNavigateToPersonal;
    private final Consumer<Club> onNavigateToClub;
    private final Runnable redeemInvite;

    public CardsGrid(CrossRoadsService service,
                     Runnable onNavigateToPersonal,
                     Consumer<Club> onNavigateToClub,
                     Runnable redeemInvite) {
        super(20);
        this.service = service;
        this.onNavigateToPersonal = onNavigateToPersonal;
        this.onNavigateToClub = onNavigateToClub;
        this.redeemInvite = redeemInvite;

        setAlignment(Pos.TOP_CENTER);
        setMaxWidth(1200);

        Label sectionTitle = new Label("My Clubs & Library");
        sectionTitle.getStyleClass().add("section-title");

        grid = new FlowPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setAlignment(Pos.CENTER);
        grid.setPrefWrapLength(1200);

        populateGrid();

        // Listen for club changes
        service.getClubs().addListener((javafx.collections.ListChangeListener.Change<? extends Club> c) -> {
            populateGrid();
        });

        getChildren().addAll(sectionTitle, grid);
    }

    private void populateGrid() {
        System.out.println(">>> populateGrid() called - Stack trace:");
        Thread.dumpStack();

        grid.getChildren().clear();

        // Add "Me" card first
        MeCard meCard = new MeCard(service, onNavigateToPersonal);
        grid.getChildren().add(meCard);

        // Add club cards
        for (Club club : service.getClubs()) {
            if (club == null) continue;
            ClubCard clubCard = new ClubCard(club, onNavigateToClub);
            grid.getChildren().add(clubCard);
        }

        // Add Invite Card last
        InviteCard inviteCard = new InviteCard(redeemInvite);
        grid.getChildren().add(inviteCard);
    }
}
