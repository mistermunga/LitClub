package com.litclub.ui.main.shared.view.service;

import com.litclub.client.api.ApiErrorHandler;
import com.litclub.construct.Book;
import com.litclub.persistence.repository.LibraryRepository;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.function.Consumer;
import java.util.stream.Collectors;

public class RecommendationsService {

    private final LibraryRepository libraryRepository = LibraryRepository.getInstance();

    public void loadRecommendations(
            Long userID,
            Runnable onSuccess,
            Consumer<String> onError
    ) {
        libraryRepository.fetchRecommendations(userID)
                .thenRun(() -> Platform.runLater(() -> {
                    System.out.println("Loaded Recommendations");
                    onSuccess.run();
                })).exceptionally(
                        throwable -> {
                            Platform.runLater(() -> {
                                String errorMessage = ApiErrorHandler.parseError(throwable);
                                System.err.println("Failed to load Recommendations " + errorMessage);
                                onError.accept("Failed to load Recommendations " + errorMessage);
                            });
                            return null;
                        }
                );
    }

    public ObservableList<Book> getRecommendedBooks() {
        return fetchBooks();
    }

    private ObservableList<Book> fetchBooks() {
        ObservableList<Book> recommendations = libraryRepository.getRecommendations();
        ObservableList<Book> allReadBooks = FXCollections.observableArrayList();
        allReadBooks.addAll(libraryRepository.getCurrentlyReading());
        allReadBooks.addAll(libraryRepository.getFinishedReading());

        return recommendations.stream()
                .filter(book -> !allReadBooks.contains(book))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

}
