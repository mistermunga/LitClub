package com.litclub.ui.component;

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

    public void showHome() {
        for (Node node : this.getChildren()) {
            if (node instanceof HomeView) {
                node.setVisible(true);
            } else {
                node.setVisible(false);
            }
        }
    }

    public void showLibrary() {
        for (Node node : this.getChildren()) {
            if (node instanceof LibraryView) {
                node.setVisible(true);
            } else {
                node.setVisible(false);
            }
        }
    }

    public void showMeetings() {
        for (Node node : this.getChildren()) {
            if (node instanceof MeetingsView) {
                node.setVisible(true);
            } else {
                node.setVisible(false);
            }
        }
    }

    public void showMembers () {
        for (Node node : this.getChildren()) {
            if (node instanceof MembersView) {
                node.setVisible(true);
            } else {
                node.setVisible(false);
            }
        }
    }

    public void showNotes() {
        for (Node node : this.getChildren()) {
            if (node instanceof NotesView) {
                node.setVisible(true);
            }  else {
                node.setVisible(false);
            }
        }
    }

    public void showRecommendations() {
        for (Node node : this.getChildren()) {
            if (node instanceof RecommendationsView) {
                node.setVisible(true);
            }  else {
                node.setVisible(false);
            }
        }
    }
}