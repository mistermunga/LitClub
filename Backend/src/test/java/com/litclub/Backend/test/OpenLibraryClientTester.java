package com.litclub.Backend.test;

import com.litclub.Backend.construct.library.book.clientDTO.BookMetadataDTO;
import com.litclub.Backend.service.OpenLibraryClient;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Standalone test utility for OpenLibraryClient.
 * NOT managed by Spring - runs independently with its own main method.
 *
 * Usage: Run this class directly to interactively test Open Library API calls.
 */
public class OpenLibraryClientTester {

    private static final OpenLibraryClient client = new OpenLibraryClient();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("    Open Library Client Interactive Tester");
        System.out.println("=================================================\n");

        boolean running = true;
        while (running) {
            displayMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> testIsbnSearch();
                case "2" -> testTitleAuthorSearch();
                case "3" -> testMultipleResults();
                case "4" -> testEdgeCases();
                case "5" -> running = false;
                default -> System.out.println("❌ Invalid choice. Try again.\n");
            }
        }

        System.out.println("\n👋 Goodbye!");
        scanner.close();
    }

    private static void displayMenu() {
        System.out.println("Choose an option:");
        System.out.println("  1. Search by ISBN");
        System.out.println("  2. Search by Title/Author");
        System.out.println("  3. Search Multiple Results");
        System.out.println("  4. Run Edge Case Tests");
        System.out.println("  5. Exit");
        System.out.print("\nYour choice: ");
    }

    private static void testIsbnSearch() {
        System.out.println("\n--- ISBN Search ---");
        System.out.print("Enter ISBN (10 or 13 digits): ");
        String isbn = scanner.nextLine().trim();

        if (isbn.isEmpty()) {
            System.out.println("❌ ISBN cannot be empty.\n");
            return;
        }

        System.out.println("\n🔍 Searching for ISBN: " + isbn);
        System.out.println("⏳ Please wait...\n");

        try {
            long startTime = System.currentTimeMillis();
            Optional<BookMetadataDTO> result = client.fetchByIsbn(isbn);
            long elapsed = System.currentTimeMillis() - startTime;

            if (result.isPresent()) {
                System.out.println("✅ Book found! (took " + elapsed + "ms)\n");
                printBookDetails(result.get());
            } else {
                System.out.println("❌ No book found for ISBN: " + isbn);
                System.out.println("   This could mean:");
                System.out.println("   - Invalid ISBN");
                System.out.println("   - Book not in Open Library database");
                System.out.println("   - Network/API error\n");
            }
        } catch (Exception e) {
            System.err.println("❌ Error occurred: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println();
    }

    private static void testTitleAuthorSearch() {
        System.out.println("\n--- Title/Author Search ---");
        System.out.print("Enter book title: ");
        String title = scanner.nextLine().trim();

        System.out.print("Enter author name (optional, press Enter to skip): ");
        String author = scanner.nextLine().trim();
        author = author.isEmpty() ? null : author;

        if (title.isEmpty()) {
            System.out.println("❌ Title cannot be empty.\n");
            return;
        }

        System.out.println("\n🔍 Searching for: \"" + title + "\"" +
                (author != null ? " by " + author : ""));
        System.out.println("⏳ Please wait...\n");

        try {
            long startTime = System.currentTimeMillis();
            Optional<BookMetadataDTO> result = client.fetchByTitleAndAuthor(title, author);
            long elapsed = System.currentTimeMillis() - startTime;

            if (result.isPresent()) {
                System.out.println("✅ Book found! (took " + elapsed + "ms)\n");
                printBookDetails(result.get());
            } else {
                System.out.println("❌ No book found for title: \"" + title + "\"");
                System.out.println("   Try:");
                System.out.println("   - Different spelling");
                System.out.println("   - More specific/less specific title");
                System.out.println("   - Adding or removing the author name\n");
            }
        } catch (Exception e) {
            System.err.println("❌ Error occurred: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println();
    }

    private static void testMultipleResults() {
        System.out.println("\n--- Multiple Results Search ---");
        System.out.print("Enter search term: ");
        String title = scanner.nextLine().trim();

        System.out.print("Enter author (optional): ");
        String author = scanner.nextLine().trim();
        author = author.isEmpty() ? null : author;

        System.out.print("How many results? (1-20): ");
        String limitStr = scanner.nextLine().trim();
        int limit = 5; // default
        try {
            limit = Integer.parseInt(limitStr);
            limit = Math.max(1, Math.min(20, limit)); // Clamp between 1-20
        } catch (NumberFormatException e) {
            System.out.println("⚠️  Invalid number, using default: 5");
        }

        if (title.isEmpty()) {
            System.out.println("❌ Search term cannot be empty.\n");
            return;
        }

        System.out.println("\n🔍 Searching for up to " + limit + " results...");
        System.out.println("⏳ Please wait...\n");

        try {
            long startTime = System.currentTimeMillis();
            List<BookMetadataDTO> results = client.searchBooks(title, author, limit);
            long elapsed = System.currentTimeMillis() - startTime;

            if (results.isEmpty()) {
                System.out.println("❌ No results found.\n");
            } else {
                System.out.println("✅ Found " + results.size() + " result(s) (took " + elapsed + "ms)\n");
                System.out.println("=".repeat(80));
                for (int i = 0; i < results.size(); i++) {
                    System.out.println("\n📚 Result #" + (i + 1));
                    System.out.println("-".repeat(80));
                    printBookDetails(results.get(i));
                }
                System.out.println("=".repeat(80));
            }
        } catch (Exception e) {
            System.err.println("❌ Error occurred: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println();
    }

    private static void testEdgeCases() {
        System.out.println("\n--- Running Edge Case Tests ---\n");

        EdgeCaseTest[] tests = {
                new EdgeCaseTest("Null ISBN", () -> client.fetchByIsbn(null)),
                new EdgeCaseTest("Empty ISBN", () -> client.fetchByIsbn("")),
                new EdgeCaseTest("Invalid ISBN (letters)", () -> client.fetchByIsbn("ABCDEFGHIJ")),
                new EdgeCaseTest("ISBN with hyphens", () -> client.fetchByIsbn("978-0-13-468599-1")),
                new EdgeCaseTest("Null Title", () -> client.fetchByTitleAndAuthor(null, "Author")),
                new EdgeCaseTest("Empty Title", () -> client.fetchByTitleAndAuthor("", "Author")),
                new EdgeCaseTest("Title only (no author)", () -> client.fetchByTitleAndAuthor("1984", null)),
                new EdgeCaseTest("Non-existent book", () -> client.fetchByIsbn("9999999999")),
                new EdgeCaseTest("Special characters in title", () ->
                        client.fetchByTitleAndAuthor("C++ Programming: 101", null))
        };

        int passed = 0;
        int failed = 0;

        for (EdgeCaseTest test : tests) {
            System.out.print("Testing: " + test.name + "... ");
            try {
                Optional<BookMetadataDTO> result = test.operation.execute();
                System.out.println("✅ PASS (returned: " +
                        (result.isPresent() ? "book" : "empty") + ")");
                passed++;
            } catch (Exception e) {
                System.out.println("❌ FAIL - Exception: " + e.getClass().getSimpleName());
                failed++;
            }
        }

        System.out.println("\n" + "=".repeat(50));
        System.out.println("Edge Case Test Summary:");
        System.out.println("  ✅ Passed: " + passed);
        System.out.println("  ❌ Failed: " + failed);
        System.out.println("=".repeat(50) + "\n");
    }

    private static void printBookDetails(BookMetadataDTO book) {
        System.out.println("  Title:       " + (book.getTitle() != null ? book.getTitle() : "N/A"));

        if (book.getAuthors() != null && !book.getAuthors().isEmpty()) {
            System.out.println("  Author(s):   " + String.join(", ", book.getAuthors()));
        } else {
            System.out.println("  Author(s):   N/A");
        }

        System.out.println("  ISBN:        " + (book.getIsbn() != null ? book.getIsbn() : "N/A"));
        System.out.println("  Publisher:   " + (book.getPublisher() != null ? book.getPublisher() : "N/A"));
        System.out.println("  Pub. Date:   " + (book.getPublishDate() != null ? book.getPublishDate() : "N/A"));
        System.out.println("  Edition:     " + (book.getEdition() != null ? book.getEdition() : "N/A"));
        System.out.println("  Cover URL:   " + (book.getCoverUrl() != null ? book.getCoverUrl() : "N/A"));
        System.out.println("  Valid DTO:   " + (book.isValid() ? "✅ Yes" : "❌ No"));
    }

    @FunctionalInterface
    private interface TestOperation {
        Optional<BookMetadataDTO> execute() throws Exception;
    }

    private record EdgeCaseTest(String name, TestOperation operation) {}
}