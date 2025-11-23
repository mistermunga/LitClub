package com.litclub.ui.main.shared.view.service;

import com.litclub.client.api.ApiErrorHandler;
import com.litclub.construct.Book;
import com.litclub.construct.interfaces.club.ActiveFlag;
import com.litclub.persistence.repository.ClubRepository;
import javafx.application.Platform;
import javafx.collections.ObservableList;

import java.util.function.Consumer;

/**
 * Service layer for club book operations.
 */
public class ClubBookService {

    private final ClubRepository clubRepository;

    public ClubBookService() {
        this.clubRepository = ClubRepository.getInstance();
    }

    public void loadClubBooks(Long clubID, Runnable onSuccess, Consumer<String> onError) {
        clubRepository.fetchClubBooks(clubID)
                .thenRun(() -> Platform.runLater(onSuccess))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        String errorMessage = ApiErrorHandler.parseError(throwable);
                        onError.accept(errorMessage);
                    });
                    return null;
                });
    }

    public void addBook(Long clubID, Long bookID,
                        Consumer<Book> onSuccess, Consumer<String> onError) {
        clubRepository.addClubBook(clubID, bookID)
                .thenAccept(book -> Platform.runLater(() -> onSuccess.accept(book)))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        String errorMessage = ApiErrorHandler.parseError(throwable);
                        onError.accept(errorMessage);
                    });
                    return null;
                });
    }

    public void updateBookStatus(Long clubID, Long bookID, boolean valid,
                                 Runnable onSuccess, Consumer<String> onError) {
        clubRepository.updateClubBook(clubID, bookID, new ActiveFlag(valid))
                .thenRun(() -> Platform.runLater(onSuccess))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        String errorMessage = ApiErrorHandler.parseError(throwable);
                        onError.accept(errorMessage);
                    });
                    return null;
                });
    }

    public void removeBook(Long clubID, Long bookID,
                           Runnable onSuccess, Consumer<String> onError) {
        clubRepository.deleteClubBook(clubID, bookID)
                .thenRun(() -> Platform.runLater(onSuccess))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        String errorMessage = ApiErrorHandler.parseError(throwable);
                        onError.accept(errorMessage);
                    });
                    return null;
                });
    }

    public ObservableList<Book> getClubBooks() {
        return clubRepository.getClubBooks();
    }
}