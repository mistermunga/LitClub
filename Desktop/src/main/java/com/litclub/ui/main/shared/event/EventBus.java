package com.litclub.ui.main.shared.event;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generic event bus for UI-level events across the application.
 * Supports both personal and club contexts with different event types.
 *
 * <p>Thread-safe and designed for decoupled communication between views.</p>
 *
 * <p><strong>Usage:</strong></p>
 * <pre>
 * // Subscribe to events
 * EventBus.getInstance().on(EventType.DISCUSSION_PROMPTS_UPDATED, this::refresh);
 *
 * // Publish events
 * EventBus.getInstance().emit(EventType.DISCUSSION_PROMPTS_UPDATED);
 *
 * // Cleanup when done
 * EventBus.getInstance().clearListeners(EventType.DISCUSSION_PROMPTS_UPDATED);
 * </pre>
 */
public class EventBus {

    private static EventBus instance;

    // Map of event types to their listeners
    private final Map<EventType, List<Runnable>> listeners = new ConcurrentHashMap<>();

    private EventBus() {}

    public static synchronized EventBus getInstance() {
        if (instance == null) {
            instance = new EventBus();
        }
        return instance;
    }

    /**
     * Subscribe to an event type.
     *
     * @param eventType the event to listen for
     * @param listener the callback to invoke when event is emitted
     */
    public void on(EventType eventType, Runnable listener) {
        listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(listener);
    }

    /**
     * Emit an event, notifying all subscribers.
     *
     * @param eventType the event to emit
     */
    public void emit(EventType eventType) {
        List<Runnable> eventListeners = listeners.get(eventType);
        if (eventListeners != null) {
            // Create a copy to avoid concurrent modification issues
            new ArrayList<>(eventListeners).forEach(Runnable::run);
        }
    }

    /**
     * Remove all listeners for a specific event type.
     * Useful when leaving a context (e.g., exiting a club).
     *
     * @param eventType the event type to clear
     */
    public void clearListeners(EventType eventType) {
        listeners.remove(eventType);
    }

    /**
     * Remove all listeners across all event types.
     * Useful for logout or major context switches.
     */
    public void clearAllListeners() {
        listeners.clear();
    }

    /**
     * Unsubscribe a specific listener from an event type.
     *
     * @param eventType the event type
     * @param listener the listener to remove
     */
    public void off(EventType eventType, Runnable listener) {
        List<Runnable> eventListeners = listeners.get(eventType);
        if (eventListeners != null) {
            eventListeners.remove(listener);
        }
    }

    public void off(EventType eventType) {
        // Remove all listeners for the given event type
        listeners.remove(eventType);
    }

    public void off(Collection<EventType> eventTypes) {
        // Remove listeners for all specified event types
        for (EventType eventType : eventTypes) {
            listeners.remove(eventType);
        }
    }

    public static EnumSet<EventType> clubEvents() {
        return EnumSet.of(
                EventType.DISCUSSION_PROMPTS_UPDATED,
                EventType.CLUB_NOTES_UPDATED,
                EventType.CLUB_MEETINGS_UPDATED,
                EventType.CLUB_MEMBERS_UPDATED,
                EventType.CLUB_BOOK_UPDATED,
                EventType.PROMPT_NOTE_UPDATED,
                EventType.INDEPENDENT_NOTE_REPLIES_ADDED,
                EventType.PROMPT_NOTE_REPLY_ADDED
        );
    }

    public static EnumSet<EventType> personalEvents() {
        return EnumSet.of(
                EventType.PERSONAL_NOTES_UPDATED,
                EventType.PERSONAL_LIBRARY_UPDATED,
                EventType.PERSONAL_REVIEWS_UPDATED,
                EventType.BOOKS_UPDATED,
                EventType.USER_PROFILE_UPDATED
        );
    }

    /**
     * Event types for the application.
     */
    public enum EventType {
        // Club events
        DISCUSSION_PROMPTS_UPDATED,
        CLUB_NOTES_UPDATED,
        CLUB_MEETINGS_UPDATED,
        CLUB_MEMBERS_UPDATED,

        // Personal events
        PERSONAL_NOTES_UPDATED,
        PERSONAL_LIBRARY_UPDATED,
        PERSONAL_REVIEWS_UPDATED,

        // Shared events
        BOOKS_UPDATED,
        CLUB_BOOK_UPDATED,
        USER_PROFILE_UPDATED,
        PROMPT_NOTE_UPDATED,

        INDEPENDENT_NOTE_REPLIES_ADDED,
        PROMPT_NOTE_REPLY_ADDED
    }
}