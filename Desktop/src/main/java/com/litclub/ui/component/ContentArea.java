package com.litclub.ui.component;

import com.litclub.session.construct.mock.MockDataPopulator;
import com.litclub.ui.component.content.*;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

public class ContentArea extends StackPane {

    private HomeView homeView;
    private LibraryView libraryView;
    private MeetingsView meetingsView;
    private MembersView membersView;
    private NotesView notesView;
    private RecommendationsView recommendationsView;

    public ContentArea() {
        addAllViews();
        showHome();
    }

    private void addAllViews(){
        MockDataPopulator populator = new MockDataPopulator();
        populator.populateClub();

        this.homeView = new HomeView();
        this.libraryView = new LibraryView();
        this.meetingsView = new MeetingsView();
        this.membersView = new MembersView();
        this.notesView = new NotesView();
        this.recommendationsView = new RecommendationsView();

        this.getChildren().addAll(
                homeView,
                libraryView,
                meetingsView,
                membersView,
                notesView,
                recommendationsView
        );
    }

    private void showOnly(Node viewToShow) {
        for (Node node : this.getChildren()) {
            node.setVisible(node == viewToShow);
        }
    }

    public void showHome() {
        showOnly(homeView);
    }

    public void showLibrary() {
        showOnly(libraryView);
    }

    public void showMeetings() {
        showOnly(meetingsView);
    }

    public void showMembers() {
        showOnly(membersView);
    }

    public void showNotes() {
        showOnly(notesView);
    }

    public void showRecommendations() {
        showOnly(recommendationsView);
    }

    public void showView(String text) {
        switch (text) {
            case "Library" -> showLibrary();
            case "Meetings" -> showMeetings();
            case "Members" -> showMembers();
            case "Notes" -> showNotes();
            case "Recommendations" -> showRecommendations();
            default -> showHome();
        }
    }
}