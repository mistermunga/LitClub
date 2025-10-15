package com.litclub.session.construct.mock;

import com.litclub.session.AppSession;
import com.litclub.session.construct.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class MockEntityGenerator {

    public Book mockBook(){
        Book mockBook = new Book();

        int bookID = (int) (Math.random() * 1001);
        String title = "Book #" + bookID;
        String author = "Author";
        String isbn = "123456789123";
        String coverUrl = "https://archive.org/services/img/howtowritethesis0000ecou/full/pct:200/0/default.jpg";

        mockBook.setTitle(title);
        mockBook.setAuthor(author);
        mockBook.setIsbn(isbn);
        mockBook.setCoverURL(coverUrl);
        return mockBook;
    }

    public UserRecord mockUserRecord(){
        int userID = (int) (Math.random() * 1005);
        List<String> namePool = List.of("John", "Marie", "Graham", "Dylan", "Quentin", "Fisher");
        String firstname = namePool.get((int) (Math.random() * namePool.size()));
        String lastname = namePool.get((int) (Math.random() * namePool.size()));
        String username = firstname + lastname;
        String email = firstname + "@example.com";

        return new UserRecord(
                userID, firstname, lastname, username, email
        );
    }

    // TODO implement book id with referential integrity
    public Note mockNote(boolean isPrivate){
        int noteID = (int) (Math.random() * 1001);
        // int BookID = repo.getBook
        int clubID = isPrivate ? null : AppSession.getInstance().getClubRecord().clubID();
        int userID = AppSession.getInstance().getUserRecord().userID();
        String content = "Lorem ipsum dolor scribit";
        boolean Private = isPrivate;
        LocalDateTime date = LocalDateTime.now();

        Note note = new Note();
        note.setClubID(clubID);
        // note.setbookid
        note.setUserID(userID);
        note.setContent(content);
        note.setPrivate(Private);
        note.setCreatedAt(date); // TODO use database

        return note;
    }

    public MeetingRecord mockMeeting (boolean online) {
        String meetingName = "Sample Meeting";
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusHours(2L);
        ClubRecord club = AppSession.getInstance().getClubRecord();
        String location = null, link = null;
        if (online){
            link = club.name() + ".litclub.com";
        } else  {
            location = "Physical Room " + club.clubID();
        }

        return new MeetingRecord(
                meetingName,
                start,
                end,
                club,
                location,
                Optional.ofNullable(link)
        );
    }

    // Add this method to your MockEntityGenerator class

    public Review mockReview() {
        int reviewID = (int) (Math.random() * 1001);
        // TODO: Get actual book ID from repository when implementing referential integrity
        int bookID = (int) (Math.random() * 100); // Placeholder
        int userID = AppSession.getInstance().getUserRecord().userID();

        // Random rating between 1-10 (allows half-star increments when divided by 2)
        int rating = (int) (Math.random() * 10) + 1;

        // Sample review content based on rating
        String content = switch (rating) {
            case 10, 9 -> "Absolutely loved this book! A masterpiece that I'll recommend to everyone.";
            case 8, 7 -> "Really enjoyed reading this. Great characters and compelling story.";
            case 6, 5 -> "Decent read. Had its moments but nothing particularly memorable.";
            case 4, 3 -> "Struggled to get through this one. Not quite what I was expecting.";
            case 2, 1 -> "Unfortunately couldn't connect with this book at all.";
            default -> "A thought-provoking read that made me think differently.";
        };

        Review review = new Review(reviewID, bookID, userID, rating, content);

        return review;
    }

    // Overloaded version with specific book and rating
    public Review mockReview(int bookID, int rating) {
        int reviewID = (int) (Math.random() * 1001);
        int userID = AppSession.getInstance().getUserRecord().userID();

        // Clamp rating to 1-10
        rating = Math.max(1, Math.min(10, rating));

        String content = switch (rating) {
            case 10, 9 -> "Absolutely loved this book! A masterpiece that I'll recommend to everyone.";
            case 8, 7 -> "Really enjoyed reading this. Great characters and compelling story.";
            case 6, 5 -> "Decent read. Had its moments but nothing particularly memorable.";
            case 4, 3 -> "Struggled to get through this one. Not quite what I was expecting.";
            case 2, 1 -> "Unfortunately couldn't connect with this book at all.";
            default -> "A thought-provoking read that made me think differently.";
        };

        Review review = new Review(reviewID, bookID, userID, rating, content);
        // Don't set createdAt - will come from database

        return review;
    }
}
