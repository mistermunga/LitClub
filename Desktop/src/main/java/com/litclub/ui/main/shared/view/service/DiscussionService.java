package com.litclub.ui.main.shared.view.service;

import com.litclub.client.api.ApiErrorHandler;
import com.litclub.construct.DiscussionPrompt;
import com.litclub.construct.Note;
import com.litclub.persistence.repository.ClubRepository;
import com.litclub.session.AppSession;
import javafx.application.Platform;
import javafx.collections.ObservableList;

import java.util.function.Consumer;

public class DiscussionService {

    private final ClubRepository clubRepository;
    private ObservableList<Note> promptNotes;

    public DiscussionService() {
        clubRepository = ClubRepository.getInstance();
    }

    public ObservableList<DiscussionPrompt> getDiscussionPrompts(){
        return clubRepository.getDiscussions();
    }

    public ObservableList<Note> getPromptNotes() {
        return promptNotes;
    }

    public void setPromptNotes(ObservableList<Note> promptNotes) {
        this.promptNotes = promptNotes;
    }

    public void loadPrompts(
            Long clubID,
            Runnable onSuccess,
            Consumer<String> onError) {
        clubRepository.fetchDiscussions(clubID)
                .thenRun(() -> Platform.runLater(() -> {
                    System.out.println("Loaded Discussion Prompts");
                    onSuccess.run();
                }))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        String errorMessage = ApiErrorHandler.parseError(throwable);
                        System.err.println("Failed to load Discussion Prompts " + errorMessage);
                        onError.accept("Failed to load Discussion Prompts " + errorMessage);
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

    public void loadPromptNotes(
            Long promptID,
            Runnable onSuccess,
            Consumer<String> onError
    ) {
        clubRepository.fetchPromptNotes(
                AppSession.getInstance().getCurrentClub().getClubID(),
                promptID
        ).thenRun(() -> Platform.runLater(() -> {
            System.out.println("Loaded Prompt Notes");
            setPromptNotes(clubRepository.getClubNotes());
            onSuccess.run();
        })).exceptionally(throwable -> {
            Platform.runLater(() -> {
                String errorMessage = ApiErrorHandler.parseError(throwable);
                System.err.println("Failed to load Prompt Notes" + errorMessage);
                onError.accept("Failed to load Prompt Notes" + errorMessage);
            });
            return null;
        });
    }

}
