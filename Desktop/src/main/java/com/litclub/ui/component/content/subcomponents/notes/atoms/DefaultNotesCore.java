package com.litclub.ui.component.content.subcomponents.notes.atoms;

import com.litclub.persistence.DataRepository;
import com.litclub.session.construct.Note;
import com.litclub.theme.ThemeManager;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

public class DefaultNotesCore extends ScrollPane {

    protected final VBox container;
    protected final DataRepository dataRepository;
    protected static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM, yyyy");

    public DefaultNotesCore() {
        ThemeManager.getInstance().registerComponent(this);
        this.getStyleClass().addAll("notes-core", "scroll-pane");

        this.dataRepository = DataRepository.getInstance();
        this.container = new VBox(30);
        this.container.setPadding(new Insets(20));
        this.container.setFillWidth(true);
        this.container.getStyleClass().add("container");

        this.setVvalue(0);
        this.setPannable(false);

        setupSmoothScrolling();

        this.setContent(container);
        this.setFitToWidth(true);
        this.setFitToHeight(true);
        this.setHbarPolicy(ScrollBarPolicy.NEVER);
        this.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

        showPrivateNotes();
        showClubNotes();
    }

    private void setupSmoothScrolling() {
        final double SPEED = 0.005;
        container.setOnScroll(scrollEvent -> {
            double deltaY = scrollEvent.getDeltaY() * SPEED;
            this.setVvalue(this.getVvalue() - deltaY);
        });
    }

    public void showPrivateNotes() {
        var privateNotes = dataRepository.getNotes().stream()
                .filter(Note::isPrivate)
                .limit(3)
                .collect(Collectors.toList());

        VBox section = createSection(
                "Private Notes",
                "Your personal notes and reflections",
                createNoteCards(privateNotes)
        );
        container.getChildren().add(section);
    }

    public void showClubNotes() {
        var clubNotes = dataRepository.getNotes().stream()
                .filter(note -> !note.isPrivate())
                .limit(3)
                .collect(Collectors.toList());

        VBox section = createSection(
                "Club Notes",
                "Shared notes and discussion points",
                createNoteCards(clubNotes)
        );
        container.getChildren().add(section);
    }

    private VBox createSection(String title, String subtitle, FlowPane notesPane) {
        VBox section = new VBox(15);
        section.getStyleClass().add("notes-section");

        // Header
        VBox header = new VBox(5);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("section-title");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("section-subtitle");

        header.getChildren().addAll(titleLabel, subtitleLabel);

        // Notes container
        notesPane.getStyleClass().add("notes-container");

        section.getChildren().addAll(header, notesPane);
        return section;
    }

    protected FlowPane createNoteCards(java.util.List<Note> notes) {
        FlowPane flowPane = new FlowPane();
        flowPane.getStyleClass().add("notes-container");

        if (notes.isEmpty()) {
            Label emptyLabel = new Label("No notes yet");
            emptyLabel.getStyleClass().add("empty-notes-label");
            flowPane.getChildren().add(emptyLabel);
            return flowPane;
        }

        for (Note note : notes) {
            VBox noteCard = createNoteCard(note);
            flowPane.getChildren().add(noteCard);
        }

        return flowPane;
    }

    private VBox createNoteCard(Note note) {
        VBox card = new VBox();
        card.getStyleClass().add("note-card");
        card.setPadding(new Insets(16));
        card.setSpacing(10);

        // Note content
        Label contentLabel = new Label(note.getContent());
        contentLabel.getStyleClass().add("note-content");
        contentLabel.setWrapText(true);
        contentLabel.setMaxWidth(280);

        // Book reference
        String bookTitle = note.getBookTitle() != null ? note.getBookTitle() : "Unknown Book";
        Label bookLabel = new Label("📚 " + bookTitle);
        bookLabel.getStyleClass().add("note-book-reference");

        // Date
        String dateStr = note.getCreatedAt().format(DATE_FORMATTER);
        Label dateLabel = new Label(dateStr);
        dateLabel.getStyleClass().add("note-date");

        // Privacy indicator
        if (note.isPrivate()) {
            Label privateIndicator = new Label("🔒 Private");
            privateIndicator.getStyleClass().add("note-private-indicator");
            card.getChildren().addAll(contentLabel, bookLabel, dateLabel, privateIndicator);
        } else {
            Label sharedIndicator = new Label("👥 Shared");
            sharedIndicator.getStyleClass().add("note-shared-indicator");
            card.getChildren().addAll(contentLabel, bookLabel, dateLabel, sharedIndicator);
        }

        return card;
    }
}