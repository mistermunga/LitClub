package com.litclub.ui.main.shared;

import com.litclub.session.AppSession;
import com.litclub.ui.main.shared.view.*;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import java.util.LinkedHashMap;
import java.util.Map;

public class ContentArea extends StackPane {

    private final Map<String, Node> views = new LinkedHashMap<>();
    private final boolean isPersonal;

    public ContentArea(boolean isPersonal) {
        this.isPersonal = isPersonal;
        initializeViews();
        showHome();
    }

    private void initializeViews() {

        if (isPersonal) {
            addView("Home", new HomeView(true));
            addView("Library", new LibraryView(true));
            addView("Notes", new NotesView(true));
            addView("Recommendations", new RecommendationsView());

            if (AppSession.getInstance().isAdmin()) {
                addView("Admin Actions", new AdminActionsView());
            }

        } else {
            addView("Club Home", new HomeView(false));
            addView("Discussion", new DiscussionView());
            addView("Notes", new NotesView(false));
            addView("Meetings", new MeetingsView());
            addView("Members", new MembersView());
            addView("Actions", new ClubActions());
        }

        // Put all views on the StackPane
        getChildren().addAll(views.values());
    }

    private void addView(String key, Node view) {
        view.setVisible(false);
        views.put(key, view);
    }

    private void showOnly(Node viewToShow) {
        for (Node node : getChildren()) {
            node.setVisible(node == viewToShow);
        }
    }

    public void showView(String name) {
        Node view = views.get(name);
        if (view != null) {
            showOnly(view);
        } else {
            showHome();
        }
    }

    public void showHome() {
        showView(isPersonal ? "Home" : "Club Home");
    }
}
