package com.litclub.session.construct.mock;

import com.litclub.session.AppSession;
import com.litclub.session.construct.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MockEntityGenerator {

    private static int bookIdCounter = 1000;
    private static int noteIdCounter = 2000;
    private static int reviewIdCounter = 3000;
    private static int userIdCounter = 4000;

    private static final List<String> FIRST_NAMES = List.of(
            "John", "Marie", "Graham", "Dylan", "Quentin", "Fisher",
            "Alex", "Sam", "Jordan", "Casey", "Morgan", "Riley"
    );

    private static final List<String> LAST_NAMES = List.of(
            "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia",
            "Miller", "Davis", "Rodriguez", "Martinez", "Hernandez", "Lopez"
    );

    private static final List<String> BOOK_TITLES = List.of(
            "The Midnight Library", "Project Hail Mary", "Atomic Habits",
            "The Seven Husbands of Evelyn Hugo", "Lessons in Chemistry",
            "Klara and the Sun", "The Thursday Murder Club", "Remarkably Bright",
            "Tomorrow, and Tomorrow, and Tomorrow", "The Four Winds"
    );

    private static final List<String> AUTHORS = List.of(
            "Matt Haig", "Andy Weir", "James Clear", "Taylor Jenkins Reid",
            "Bonnie Garmus", "Kazuo Ishiguro", "Richard Osman", "Katherine Heiny",
            "Gabrielle Zevin", "Kristin Hannah"
    );

    private static final List<String> NOTE_CONTENTS = List.of(
            "Loved this passage - really resonated with me",
            "This character development was incredible",
            "Didn't expect that plot twist!",
            "The writing style is so engaging",
            "This scene made me think about my own life",
            "Brilliant dialogue between the main characters",
            "The pacing was perfect in this section",
            "This theme appears throughout the novel",
            "Reminds me of another book I read",
            "Beautiful metaphor here"
    );

    public Book mockBook() {
        Book mockBook = new Book();

        int bookID = ++bookIdCounter;
        String title = BOOK_TITLES.get((int) (Math.random() * BOOK_TITLES.size()));
        String author = AUTHORS.get((int) (Math.random() * AUTHORS.size()));
        String isbn = generateISBN();
        String coverUrl = "https://archive.org/services/img/howtowritethesis0000ecou/full/pct:200/0/default.jpg";

        mockBook.setBookID(bookID);
        mockBook.setTitle(title);
        mockBook.setAuthor(author);
        mockBook.setIsbn(isbn);
        mockBook.setCoverURL(coverUrl);

        return mockBook;
    }

    public UserRecord mockUserRecord() {
        int userID = ++userIdCounter;
        String firstname = FIRST_NAMES.get((int) (Math.random() * FIRST_NAMES.size()));
        String lastname = LAST_NAMES.get((int) (Math.random() * LAST_NAMES.size()));
        String username = (firstname + lastname).toLowerCase();
        String email = username + "@litclub.com";

        return new UserRecord(userID, firstname, lastname, username, email);
    }

    public Note mockNote(boolean isPrivate) {
        int noteID = ++noteIdCounter;
        Integer clubID = isPrivate ? null : AppSession.getInstance().getClubRecord().clubID();
        int userID = AppSession.getInstance().getUserRecord().userID();
        String content = NOTE_CONTENTS.get((int) (Math.random() * NOTE_CONTENTS.size()));
        LocalDateTime date = LocalDateTime.now().minusDays((long) (Math.random() * 30));

        Note note = new Note();
        note.setNoteID(noteID);
        note.setClubID(clubID);
        note.setUserID(userID);
        note.setContent(content);
        note.setPrivate(isPrivate);
        note.setCreatedAt(date);

        return note;
    }

    public MeetingRecord mockMeeting(boolean online) {
        String[] meetingNames = {
                "Monthly Book Discussion",
                "Author Spotlight Session",
                "Reading Challenge Kickoff",
                "Genre Deep Dive",
                "New Member Welcome",
                "Holiday Reading Celebration"
        };

        String meetingName = meetingNames[(int) (Math.random() * meetingNames.length)];
        LocalDateTime start = LocalDateTime.now().plusDays((long) (Math.random() * 14)).plusHours((int) (Math.random() * 12) + 1);
        LocalDateTime end = start.plusHours(2);
        ClubRecord club = AppSession.getInstance().getClubRecord();
        String location = null;
        Optional<String> link = Optional.empty();

        if (online) {
            link = Optional.of("https://meet.litclub.com/" + club.name().toLowerCase().replace(" ", "-"));
        } else {
            String[] locations = {
                    "Central Library - Room A",
                    "Coffee House Downtown",
                    "Community Center",
                    "Park Pavilion",
                    "Bookstore - Upstairs"
            };
            location = locations[(int) (Math.random() * locations.length)];
        }

        return new MeetingRecord(meetingName, start, end, club, location, link);
    }

    public Review mockReview() {
        int reviewID = ++reviewIdCounter;
        int userID = AppSession.getInstance().getUserRecord().userID();
        int rating = (int) (Math.random() * 10) + 1;

        String content = getReviewContent(rating);

        Review review = new Review(reviewID, 0, userID, rating, content);
        return review;
    }

    public Review mockReview(int bookID, int rating) {
        int reviewID = ++reviewIdCounter;
        int userID = AppSession.getInstance().getUserRecord().userID();

        rating = Math.max(1, Math.min(10, rating));
        String content = getReviewContent(rating);

        Review review = new Review(reviewID, bookID, userID, rating, content);
        return review;
    }

    // ==================== HELPER METHODS ====================

    private String getReviewContent(int rating) {
        return switch (rating) {
            case 10, 9 -> "Absolutely loved this book! A masterpiece that I'll recommend to everyone.";
            case 8, 7 -> "Really enjoyed reading this. Great characters and compelling story.";
            case 6, 5 -> "Decent read. Had its moments but nothing particularly memorable.";
            case 4, 3 -> "Struggled to get through this one. Not quite what I was expecting.";
            case 2, 1 -> "Unfortunately couldn't connect with this book at all.";
            default -> "A thought-provoking read that made me think differently.";
        };
    }

    private String generateISBN() {
        // Generate a fake but valid-looking ISBN-13
        StringBuilder isbn = new StringBuilder("978");
        for (int i = 0; i < 10; i++) {
            isbn.append((int) (Math.random() * 10));
        }
        return isbn.toString();
    }
}