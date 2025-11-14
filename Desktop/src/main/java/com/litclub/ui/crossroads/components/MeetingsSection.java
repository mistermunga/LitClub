package com.litclub.ui.crossroads.components;

import com.litclub.construct.Meeting;
import com.litclub.ui.crossroads.components.subcomponents.MeetingsIsland;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;

/**
 * Section displaying upcoming meetings
 */
public class MeetingsSection extends VBox {

    private final MeetingsIsland meetingsIsland;

    public MeetingsSection(ObservableList<Meeting> meetings) {
        super(15);
        setAlignment(Pos.CENTER);
        setMaxWidth(900);

        this.meetingsIsland = new MeetingsIsland(meetings);
        getChildren().add(meetingsIsland);
    }

    public void refresh() {
        meetingsIsland.refresh();
    }
}