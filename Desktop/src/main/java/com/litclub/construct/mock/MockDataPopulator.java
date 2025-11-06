package com.litclub.construct.mock;

import com.litclub.persistence.DataRepository;
import com.litclub.session.AppSession;
import com.litclub.construct.*;
import com.litclub.construct.record.user.UserRecord;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/*
design to be called once per club, immediately after
the CrossRoads page
 */
@Deprecated
public class MockDataPopulator {

    private MockEntityGenerator mockEntityGenerator = new MockEntityGenerator();
    private ClubRecord club = AppSession.getInstance().getClubRecord();
    private DataRepository dataRepository = DataRepository.getInstance();

    public Set<UserRecord> createUsersForClub(int userCount) {
        Set<UserRecord> users = new HashSet<>();
        while (users.size() < userCount) {
            users.add(mockEntityGenerator.mockUserRecord());
        }

        return users;
    }

    public Set<Book> createFakeBooks(int bookCount) {
        Set<Book> books = new HashSet<>();
        while (books.size() < bookCount) {
            Book book = mockEntityGenerator.mockBook();
            if (book.getBookID() == 0) {
                continue;
            }
            books.add(book);
            dataRepository.addBook(book);
        }
        return books;
    }

    public Set<MeetingRecord> createFakePhysicalMeetings(int meetingCount) {
        Set<MeetingRecord> meetings = new HashSet<>();
        while (meetings.size() < meetingCount) {
            MeetingRecord meetingRecord = mockEntityGenerator.mockMeeting(false);
            meetings.add(meetingRecord);
            dataRepository.addMeeting(meetingRecord);
        }
        return meetings;
    }

    public Set<MeetingRecord> createFakeOnlineMeetings(int meetingCount) {
        Set<MeetingRecord> meetings = new HashSet<>();
        while (meetings.size() < meetingCount) {
            MeetingRecord meetingRecord = mockEntityGenerator.mockMeeting(true);
            meetings.add(meetingRecord);
            dataRepository.addMeeting(meetingRecord);
        }
        return meetings;
    }

    public Set<MeetingRecord> createFakeHeterogenousMeetings(int meetingCount) {
        Set<MeetingRecord> meetings = new HashSet<>();
        while (meetings.size() < meetingCount) {
            MeetingRecord meetingRecord = mockEntityGenerator.mockMeeting(Math.random() < 0.5);
            meetings.add(meetingRecord);
            dataRepository.addMeeting(meetingRecord);
        }
        return meetings;
    }

    // always run after making books
    public Set<Note> createFakePrivateNotes(int noteCount) {
        Set<Note> notes = new HashSet<>();
        if (dataRepository.getBooks().isEmpty()) {
            throw new IllegalStateException("No books found for notes");
        }

        while (notes.size() < noteCount) {
            Note note = mockEntityGenerator.mockNote(true);
            int randomIndex = ThreadLocalRandom.current().nextInt(dataRepository.getBooks().size());
            int bookID = dataRepository.getBooks().get(randomIndex).getBookID();
            note.setBookID(bookID);
            notes.add(note);
            dataRepository.addNote(note);
        }

        return notes;
    }

    public  Set<Note> createFakePublicNotes(int noteCount) {
        Set<Note> notes = new HashSet<>();
        if (dataRepository.getBooks().isEmpty()) {
            throw new IllegalStateException("No books found for notes");
        }

        while (notes.size() < noteCount) {
            Note note = mockEntityGenerator.mockNote(false);
            int randomIndex = ThreadLocalRandom.current().nextInt(dataRepository.getBooks().size());
            int bookID = dataRepository.getBooks().get(randomIndex).getBookID();
            note.setBookID(bookID);
            notes.add(note);
            dataRepository.addNote(note);
        }

        return notes;
    }

    public Set<Note> createFakeHeterogenousNotes(int noteCount) {
        Set<Note> notes = new HashSet<>();
        if (dataRepository.getBooks().isEmpty()) {
            throw new IllegalStateException("No books found for notes");
        }

        while (notes.size() < noteCount) {
            Note note = mockEntityGenerator.mockNote(Math.random() < 0.5 );
            int randomIndex = ThreadLocalRandom.current().nextInt(dataRepository.getBooks().size());
            int bookID = dataRepository.getBooks().get(randomIndex).getBookID();
            note.setBookID(bookID);
            notes.add(note);
            dataRepository.addNote(note);
        }

        return notes;
    }

    public Set<Review> createFakeReviews(int reviewCount) {
        Set<Review> reviews = new HashSet<>();
        if (dataRepository.getBooks().isEmpty()) {
            throw new IllegalStateException("No books found for reviews");
        }

        while (reviews.size() < reviewCount) {
            Review review = mockEntityGenerator.mockReview();
            int randomIndex = ThreadLocalRandom.current().nextInt(dataRepository.getBooks().size());
            int bookID = dataRepository.getBooks().get(randomIndex).getBookID();
            review.setBookID(bookID);
            reviews.add(review);
            dataRepository.addReview(review);
        }

        return reviews;
    }

    public Set<DiscussionPromptRecord> createFakeDiscussionPrompts(int promptCount) {
        Set<DiscussionPromptRecord> discussionPrompts = new HashSet<>();

        while (discussionPrompts.size() < promptCount) {
            DiscussionPromptRecord discussionPromptRecord = mockEntityGenerator.mockPrompt();
            discussionPrompts.add(discussionPromptRecord);
            dataRepository.addDiscussionPrompts(discussionPromptRecord);
        }
        return discussionPrompts;
    }

    /**
     * Master method to populate a club with mock data.
     * Should be called once when HomeView is initialized.
     * Uses default counts for a typical small reading group.
     */
    public void populateClub() {
        populateClub(8, 12, 5, 2, 3, 2);
    }

    /**
     * Master method to populate a club with mock data.
     * Orchestrates all entity creation in proper order (books first, then dependent entities).
     *
     * @param bookCount number of books to generate
     * @param noteCount number of notes (mix of private and public)
     * @param reviewCount number of reviews
     * @param physicalMeetingCount number of in-person meetings
     * @param onlineMeetingCount number of virtual meetings
     * @param discussionPromptCount number of discussion prompts
     */
    public void populateClub(int bookCount, int noteCount, int reviewCount,
                             int physicalMeetingCount, int onlineMeetingCount,
                             int discussionPromptCount) {

        // Check if data already exists (avoid re-populating)
        if (!dataRepository.getBooks().isEmpty()) {
            System.out.println("Data already populated for club: " + club.name());
            return;
        }

        try {
            System.out.println("Populating mock data for club: " + club.name());

            // 1. Books first (all other entities depend on this)
            createFakeBooks(bookCount);
            System.out.println("✓ Created " + bookCount + " books");

            // 2. Notes (depends on books)
            int privateNoteCount = (int) Math.ceil(noteCount * 0.4);
            int publicNoteCount = noteCount - privateNoteCount;
            createFakePrivateNotes(privateNoteCount);
            createFakePublicNotes(publicNoteCount);
            System.out.println("✓ Created " + noteCount + " notes (" + privateNoteCount + " private, " + publicNoteCount + " public)");

            // 3. Reviews (depends on books)
            createFakeReviews(reviewCount);
            System.out.println("✓ Created " + reviewCount + " reviews");

            // 4. Meetings (independent, but grouped for clarity)
            createFakePhysicalMeetings(physicalMeetingCount);
            createFakeOnlineMeetings(onlineMeetingCount);
            System.out.println("✓ Created " + physicalMeetingCount + " physical meetings");
            System.out.println("✓ Created " + onlineMeetingCount + " online meetings");

            // 5. Prompts
            createFakeDiscussionPrompts(discussionPromptCount);
            System.out.println("✓ Created "  + discussionPromptCount + " discussion prompts");

            System.out.println("Mock data population complete!");

        } catch (Exception e) {
            System.err.println("Error populating mock data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
