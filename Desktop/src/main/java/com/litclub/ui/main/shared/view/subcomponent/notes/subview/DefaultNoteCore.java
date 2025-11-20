package com.litclub.ui.main.shared.view.subcomponent.notes.subview;

import com.litclub.construct.Note;
import com.litclub.session.AppSession;
import com.litclub.theme.ThemeManager;
import com.litclub.ui.main.shared.view.service.NoteService;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Default note core view showing all notes in a grid layout.
 * Context-aware: shows either personal notes or club notes based on context.
 */
public class DefaultNoteCore extends ScrollPane {

    private final NoteService noteService;
    private final boolean isPersonal;
    private final Consumer<Note> onNoteClick;
    private final VBox container;

    private FlowPane notesPane;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM, yyyy");

    // Filter and sort state
    private Predicate<Note> currentFilter = note -> true; // Show all by default
    private Comparator<Note> currentSort = Comparator.comparing(
            (Note n) -> n.getNoteID() != null ? n.getNoteID() : 0L
    ).reversed(); // Recently added by default

    // Loading state
    private StackPane loadingContainer;
    private ProgressIndicator loadingIndicator;
    private Label loadingLabel;

    /**
     * Creates a default note core view.
     *
     * @param isPersonal true for personal notes, false for club notes
     * @param onNoteClick callback when a note is clicked (for viewing details/replies)
     */
    public DefaultNoteCore(boolean isPersonal, Consumer<Note> onNoteClick) {
        this.noteService = new NoteService();
        this.isPersonal = isPersonal;
        this.onNoteClick = onNoteClick;

        ThemeManager.getInstance().registerComponent(this);
        this.getStyleClass().addAll("notes-core", "scroll-pane");

        container = new VBox(30);
        container.setPadding(new Insets(20));
        container.setFillWidth(true);
        container.getStyleClass().add("container");

        this.setVvalue(0);
        this.setPannable(false);
        setupSmoothScrolling();

        this.setContent(container);
        this.setFitToWidth(true);
        this.setFitToHeight(true);
        this.setHbarPolicy(ScrollBarPolicy.NEVER);
        this.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

        // Show loading state initially
        showLoading();

        // Load data
        loadNotes();
    }

    private void setupSmoothScrolling() {
        final double SPEED = 0.005;
        container.setOnScroll(scrollEvent -> {
            double deltaY = scrollEvent.getDeltaY() * SPEED;
            this.setVvalue(this.getVvalue() - deltaY);
        });
    }

    // ==================== DATA LOADING ====================

    public void loadNotes() {
        if (isPersonal) {
            Long userID = AppSession.getInstance().getUserRecord().userID();
            noteService.loadPersonalNotes(
                    userID,
                    this::onNotesLoaded,
                    this::showError
            );
        } else {
            Long clubID = AppSession.getInstance().getCurrentClub().getClubID();
            noteService.loadClubNotes(
                    clubID,
                    this::onNotesLoaded,
                    this::showError
            );
        }
    }

    private void onNotesLoaded() {
        // Hide loading indicator
        container.getChildren().clear();

        // Build notes view
        buildNotesView();

        // Setup data listeners
        setupDataListeners();

        System.out.println("Notes loaded successfully!");
    }

    private void buildNotesView() {
        // Create notes pane
        notesPane = new FlowPane();
        notesPane.getStyleClass().add("notes-container");

        // Create section with header
        VBox section = createSection(
                isPersonal ? "Personal Notes" : "Club Notes",
                isPersonal ? "Your private notes and reflections" : "Shared notes and discussion points",
                notesPane
        );

        container.getChildren().add(section);

        // Populate notes
        refreshNotes();
    }

    // ==================== DATA LISTENERS ====================

    private void setupDataListeners() {
        if (isPersonal) {
            noteService.getPersonalNotes().addListener(
                    (ListChangeListener.Change<? extends Note> c) -> {
                        System.out.println("Personal notes list changed!");
                        if (notesPane != null) {
                            refreshNotes();
                        }
                    }
            );
        } else {
            noteService.getClubNotes().addListener(
                    (ListChangeListener.Change<? extends Note> c) -> {
                        System.out.println("Club notes list changed!");
                        if (notesPane != null) {
                            refreshNotes();
                        }
                    }
            );
        }
    }

    // ==================== NOTE REFRESH ====================

    private void refreshNotes() {
        var notes = (isPersonal
                ? noteService.getPersonalNotes()
                : noteService.getClubNotes())
                .stream()
                .filter(currentFilter)
                .sorted(currentSort)
                .collect(Collectors.toList());

        populateNotes(notes);
    }

    private void populateNotes(java.util.List<Note> notes) {
        notesPane.getChildren().clear();

        if (notes.isEmpty()) {
            // Show empty state
            Label emptyLabel = new Label(isPersonal
                    ? "No personal notes yet. Start by adding a note!"
                    : "No club notes yet. Be the first to share!");
            emptyLabel.getStyleClass().add("empty-notes-label");
            notesPane.getChildren().add(emptyLabel);
            return;
        }

        // Create note cards
        for (Note note : notes) {
            VBox noteCard = createNoteCard(note);
            notesPane.getChildren().add(noteCard);
        }
    }

    // ==================== UI BUILDERS ====================

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
        section.getChildren().addAll(header, notesPane);
        return section;
    }

    private VBox createNoteCard(Note note) {
        VBox card = new VBox();
        card.getStyleClass().add("note-card");
        card.setPadding(new Insets(16));
        card.setSpacing(10);
        card.setCursor(javafx.scene.Cursor.HAND);

        // Note content (truncate if too long)
        String content = note.getContent();
        if (content != null && content.length() > 150) {
            content = content.substring(0, 147) + "...";
        }
        Label contentLabel = new Label(content);
        contentLabel.getStyleClass().add("note-content");
        contentLabel.setWrapText(true);
        contentLabel.setMaxWidth(280);

        // Book reference
        String bookTitle = note.getBook() != null && note.getBook().getTitle() != null
                ? note.getBook().getTitle()
                : "Unknown Book";
        Label bookLabel = new Label("📚 " + bookTitle);
        bookLabel.getStyleClass().add("note-book-reference");
        bookLabel.setWrapText(true);
        bookLabel.setMaxWidth(280);

        // Date
        String dateStr = note.getCreatedAt() != null
                ? note.getCreatedAt().format(DATE_FORMATTER)
                : "Unknown date";
        Label dateLabel = new Label(dateStr);
        dateLabel.getStyleClass().add("note-date");

        // Privacy/sharing indicator
        if (isPersonal) {
            Label privateIndicator = new Label("🔒 Private");
            privateIndicator.getStyleClass().add("note-private-indicator");
            card.getChildren().addAll(contentLabel, bookLabel, dateLabel, privateIndicator);
        } else {
            // For club notes, show author
            String author = note.getUser() != null && note.getUser().getUsername() != null
                    ? note.getUser().getUsername()
                    : "Unknown author";
            Label authorLabel = new Label("✍️ " + author);
            authorLabel.getStyleClass().add("note-shared-indicator");
            card.getChildren().addAll(contentLabel, bookLabel, dateLabel, authorLabel);
        }

        // Click handler - open note details/replies view
        card.setOnMouseClicked(e -> onNoteClick.accept(note));

        return card;
    }

    // ==================== LOADING & ERROR STATES ====================

    private void showLoading() {
        container.getChildren().clear();

        loadingContainer = new StackPane();
        loadingContainer.setAlignment(Pos.CENTER);
        loadingContainer.setPadding(new Insets(100));

        VBox loadingBox = new VBox(20);
        loadingBox.setAlignment(Pos.CENTER);

        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(50, 50);

        loadingLabel = new Label("Loading notes...");
        loadingLabel.getStyleClass().add("section-subtitle");

        loadingBox.getChildren().addAll(loadingIndicator, loadingLabel);
        loadingContainer.getChildren().add(loadingBox);

        container.getChildren().add(loadingContainer);
    }

    private void showError(String errorMessage) {
        container.getChildren().clear();

        VBox errorBox = new VBox(20);
        errorBox.setAlignment(Pos.CENTER);
        errorBox.setPadding(new Insets(100));

        Label errorIcon = new Label("⚠️");
        errorIcon.setStyle("-fx-font-size: 48px;");

        Label errorLabel = new Label("Failed to load notes");
        errorLabel.getStyleClass().add("section-title");

        Label errorDetails = new Label(errorMessage);
        errorDetails.getStyleClass().addAll("text-muted", "error-label");
        errorDetails.setWrapText(true);
        errorDetails.setMaxWidth(400);
        errorDetails.setAlignment(Pos.CENTER);

        errorBox.getChildren().addAll(errorIcon, errorLabel, errorDetails);

        container.getChildren().add(errorBox);
    }

    // ==================== PUBLIC API (for NoteControlBar) ====================

    /**
     * Apply filter predicate to notes.
     */
    public void applyFilter(Predicate<Note> filterPredicate) {
        this.currentFilter = filterPredicate;
        refreshNotes();
    }

    /**
     * Apply sort comparator to notes.
     */
    public void applySort(Comparator<Note> sortComparator) {
        this.currentSort = sortComparator;
        refreshNotes();
    }

    /**
     * Refresh notes display (useful after adding/removing notes).
     */
    public void refresh() {
        refreshNotes();
    }

    /**
     * Get the note service instance.
     */
    public NoteService getNoteService() {
        return noteService;
    }
}