package com.litclub.ui.main.shared.view.service;

import com.litclub.client.api.ApiErrorHandler;
import com.litclub.construct.DiscussionPrompt;
import com.litclub.persistence.repository.ClubRepository;
import javafx.application.Platform;
import javafx.collections.ObservableList;

import java.util.function.Consumer;

public class DiscussionService {

    private final ClubRepository clubRepository;

    public DiscussionService() {
        clubRepository = ClubRepository.getInstance();
    }

    public ObservableList<DiscussionPrompt> getDiscussionPrompts(){
        return clubRepository.getDiscussions();
    }

    public void loadPrompts(
            Long clubID,
            Runnable onSuccess,
            Consumer<String> onError) {
        clubRepository.fetchClubMeetings(clubID)
                .thenRun(() -> Platform.runLater(() -> {
                    System.out.println("Loaded Discussion Prompts");
                    onSuccess.run();
                }))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        String errorMessage = ApiErrorHandler.parseError(throwable);
                        System.err.println("Failed to load Discussion Prompts");
                        onError.accept("Failed to load Discussion Prompts");
                    });
                    return null;
                });
    }

    public void createPrompt(
            Long clubID,
            String prompt,
            Consumer<DiscussionPrompt> onSuccess,
            Consumer<String> onError
    ) {
        clubRepository.createDiscussion(clubID, prompt)
                .thenAccept(discussionPrompt -> {
                    Platform.runLater(() -> {
                        System.out.println("Created Discussion Prompt");
                        onSuccess.accept(discussionPrompt);
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        String errorMessage = ApiErrorHandler.parseError(throwable);
                        System.err.println("Failed to create Discussion Prompt" + errorMessage);
                        onError.accept("Failed to create Discussion Prompt" + errorMessage);
                    });
                    return null;
                });
    }

}
