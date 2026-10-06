import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

/**
 * Library Management System
 *
 * A single-file Java console application designed as a substantial
 * Git collaboration/testing project.
 *
 * Features:
 *  - Book management
 *  - Member management
 *  - Borrow and return books
 *  - Due-date calculation
 *  - Fine calculation
 *  - Search and filtering
 *  - Reports
 *  - Input validation
 *  - Sample data
 */
public class LibraryManagementSystem {

    private static final Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final double DAILY_FINE = 5.0;
    private static final int LOAN_DAYS = 14;

    private static final List<Book> books = new ArrayList<>();
    private static final List<Member> members = new ArrayList<>();
    private static final List<Loan> loans = new ArrayList<>();

    public static void main(String[] args) {
        seedData();

        printBanner();

        boolean running = true;

        while (running) {
            printMainMenu();
            int choice = readInt("Enter your choice: ");

            switch (choice) {
                case 1 -> addBook();
                case 2 -> listBooks();
                case 3 -> searchBooks();
                case 4 -> addMember();
                case 5 -> listMembers();
                case 6 -> issueBook();
                case 7 -> returnBook();
                case 8 -> listActiveLoans();
                case 9 -> showOverdueLoans();
                case 10 -> showReports();
                case 11 -> removeBook();
                case 12 -> removeMember();
                case 0 -> {
                    running = false;
                    System.out.println("\nThank you for using the Library Management System.");
                }
                default -> System.out.println("\nInvalid option. Please try again.");
            }
        }

        scanner.close();
    }

    // ============================================================
    // UI
    // ============================================================

    private static void printBanner() {
        System.out.println();
        System.out.println("==============================================================");
        System.out.println("              LIBRARY MANAGEMENT SYSTEM                       ");
        System.out.println("==============================================================");
        System.out.println(" Java Console Project | Git Testing Application");
        System.out.println("==============================================================");
    }

    private static void printMainMenu() {
        System.out.println("\n------------------------ MAIN MENU ----------------------------");
        System.out.println("1.  Add Book");
        System.out.println("2.  List Books");
        System.out.println("3.  Search Books");
        System.out.println("4.  Add Member");
        System.out.println("5.  List Members");
        System.out.println("6.  Issue Book");
        System.out.println("7.  Return Book");
        System.out.println("8.  Active Loans");
        System.out.println("9.  Overdue Loans");
        System.out.println("10. Reports");
        System.out.println("11. Remove Book");
        System.out.println("12. Remove Member");
        System.out.println("0.  Exit");
        System.out.println("--------------------------------------------------------------");
    }

    // ============================================================
    // BOOK MANAGEMENT
    // ============================================================

    private static void addBook() {
        System.out.println("\n======================== ADD BOOK =============================");

        String title = readNonEmpty("Book title: ");
        String author = readNonEmpty("Author: ");
        String isbn = readNonEmpty("ISBN: ");
        String category = readNonEmpty("Category: ");
        int year = readIntRange("Publication year: ", 1000, 2100);

        if (findBookByIsbn(isbn) != null) {
            System.out.println("A book with this ISBN already exists.");
            return;
        }

        Book book = new Book(
                generateId("BK"),
                title,
                author,
                isbn,
                category,
                year
        );

        books.add(book);
        System.out.println("Book added successfully.");
        System.out.println("Book ID: " + book.id);
    }

    private static void listBooks() {
        System.out.println("\n======================== BOOK LIST ============================");

        if (books.isEmpty()) {
            System.out.println("No books found.");
            return;
        }

        System.out.printf(
                "%-10s %-28s %-22s %-16s %-8s %-12s%n",
                "ID", "TITLE", "AUTHOR", "CATEGORY", "YEAR", "STATUS"
        );
        System.out.println("-".repeat(105));

        books.stream()
                .sorted(Comparator.comparing(book -> book.title.toLowerCase()))
                .forEach(book -> System.out.printf(
                        "%-10s %-28s %-22s %-16s %-8d %-12s%n",
                        truncate(book.id, 10),
                        truncate(book.title, 28),
                        truncate(book.author, 22),
                        truncate(book.category, 16),
                        book.year,
                        book.available ? "AVAILABLE" : "ISSUED"
                ));
    }

    private static void searchBooks() {
        System.out.println("\n======================== SEARCH BOOKS ========================");
        String keyword = readNonEmpty("Search by title, author, ISBN or category: ")
                .toLowerCase();

        List<Book> results = books.stream()
                .filter(book ->
                        book.title.toLowerCase().contains(keyword)
                                || book.author.toLowerCase().contains(keyword)
                                || book.isbn.toLowerCase().contains(keyword)
                                || book.category.toLowerCase().contains(keyword))
                .toList();

        if (results.isEmpty()) {
            System.out.println("No matching books found.");
            return;
        }

        System.out.println("\nFound " + results.size() + " matching book(s):");
        for (Book book : results) {
            printBookDetails(book);
        }
    }

    private static void removeBook() {
        System.out.println("\n======================== REMOVE BOOK =========================");

        String isbn = readNonEmpty("Enter ISBN of book to remove: ");
        Book book = findBookByIsbn(isbn);

        if (book == null) {
            System.out.println("Book not found.");
            return;
        }

        boolean activeLoanExists = loans.stream()
                .anyMatch(loan -> loan.bookId.equals(book.id) && loan.active);

        if (activeLoanExists) {
            System.out.println("Cannot remove a book that is currently issued.");
            return;
        }

        books.remove(book);
        System.out.println("Book removed successfully.");
    }

    private static void printBookDetails(Book book) {
        System.out.println("\n----------------------------------------");
        System.out.println("ID       : " + book.id);
        System.out.println("Title    : " + book.title);
        System.out.println("Author   : " + book.author);
        System.out.println("ISBN     : " + book.isbn);
        System.out.println("Category : " + book.category);
        System.out.println("Year     : " + book.year);
        System.out.println("Status   : " + (book.available ? "Available" : "Issued"));
        System.out.println("----------------------------------------");
    }

    // ============================================================
    // MEMBER MANAGEMENT
    // ============================================================

    private static void addMember() {
        System.out.println("\n======================== ADD MEMBER ===========================");

        String name = readNonEmpty("Member name: ");
        String email = readNonEmpty("Email: ");
        String phone = readNonEmpty("Phone: ");

        if (findMemberByEmail(email) != null) {
            System.out.println("A member with this email already exists.");
            return;
        }

        Member member = new Member(
                generateId("MB"),
                name,
                email,
                phone,
                LocalDate.now()
        );

        members.add(member);

        System.out.println("Member added successfully.");
        System.out.println("Member ID: " + member.id);
    }

    private static void listMembers() {
        System.out.println("\n======================== MEMBER LIST ==========================");

        if (members.isEmpty()) {
            System.out.println("No members found.");
            return;
        }

        System.out.printf(
                "%-10s %-25s %-30s %-16s %-12s%n",
                "ID", "NAME", "EMAIL", "PHONE", "JOINED"
        );
        System.out.println("-".repeat(100));

        members.stream()
                .sorted(Comparator.comparing(member -> member.name.toLowerCase()))
                .forEach(member -> System.out.printf(
                        "%-10s %-25s %-30s %-16s %-12s%n",
                        member.id,
                        truncate(member.name, 25),
                        truncate(member.email, 30),
                        truncate(member.phone, 16),
                        member.joinDate.format(DATE_FORMAT)
                ));
    }

    private static void removeMember() {
        System.out.println("\n======================== REMOVE MEMBER =======================");

        String email = readNonEmpty("Enter member email: ");
        Member member = findMemberByEmail(email);

        if (member == null) {
            System.out.println("Member not found.");
            return;
        }

        boolean activeLoanExists = loans.stream()
                .anyMatch(loan -> loan.memberId.equals(member.id) && loan.active);

        if (activeLoanExists) {
            System.out.println("Cannot remove a member with active loans.");
            return;
        }

        members.remove(member);
        System.out.println("Member removed successfully.");
    }

    // ============================================================
    // LOAN MANAGEMENT
    // ============================================================

    private static void issueBook() {
        System.out.println("\n======================== ISSUE BOOK ===========================");

        String isbn = readNonEmpty("Book ISBN: ");
        Book book = findBookByIsbn(isbn);

        if (book == null) {
            System.out.println("Book not found.");
            return;
        }

        if (!book.available) {
            System.out.println("This book is already issued.");
            return;
        }

        String email = readNonEmpty("Member email: ");
        Member member = findMemberByEmail(email);

        if (member == null) {
            System.out.println("Member not found.");
            return;
        }

        long activeLoans = loans.stream()
                .filter(loan -> loan.memberId.equals(member.id) && loan.active)
                .count();

        if (activeLoans >= 5) {
            System.out.println("This member has reached the maximum of 5 active loans.");
            return;
        }

        LocalDate issueDate = LocalDate.now();
        LocalDate dueDate = issueDate.plusDays(LOAN_DAYS);

        Loan loan = new Loan(
                generateId("LN"),
                book.id,
                member.id,
                issueDate,
                dueDate
        );

        loans.add(loan);
        book.available = false;

        System.out.println("\nBook issued successfully.");
        System.out.println("Loan ID : " + loan.id);
        System.out.println("Due date: " + dueDate.format(DATE_FORMAT));
    }

    private static void returnBook() {
        System.out.println("\n======================== RETURN BOOK ==========================");

        String isbn = readNonEmpty("Book ISBN: ");
        Book book = findBookByIsbn(isbn);

        if (book == null) {
            System.out.println("Book not found.");
            return;
        }

        Loan loan = loans.stream()
                .filter(item -> item.bookId.equals(book.id) && item.active)
                .findFirst()
                .orElse(null);

        if (loan == null) {
            System.out.println("This book is not currently issued.");
            return;
        }

        LocalDate returnDate = LocalDate.now();
        double fine = calculateFine(loan.dueDate, returnDate);

        loan.active = false;
        loan.returnDate = returnDate;
        loan.fine = fine;
        book.available = true;

        System.out.println("\nBook returned successfully.");
        System.out.println("Return date: " + returnDate.format(DATE_FORMAT));
        System.out.printf("Fine       : ₹%.2f%n", fine);
    }

    private static void listActiveLoans() {
        System.out.println("\n======================== ACTIVE LOANS =========================");

        List<Loan> active = loans.stream()
                .filter(loan -> loan.active)
                .sorted(Comparator.comparing(loan -> loan.dueDate))
                .toList();

        if (active.isEmpty()) {
            System.out.println("No active loans.");
            return;
        }

        for (Loan loan : active) {
            Book book = findBookById(loan.bookId);
            Member member = findMemberById(loan.memberId);

            System.out.println("----------------------------------------");
            System.out.println("Loan ID : " + loan.id);
            System.out.println("Book    : " + (book == null ? "Unknown" : book.title));
            System.out.println("Member  : " + (member == null ? "Unknown" : member.name));
            System.out.println("Issued  : " + loan.issueDate.format(DATE_FORMAT));
            System.out.println("Due     : " + loan.dueDate.format(DATE_FORMAT));
            System.out.println("Status  : " +
                    (isOverdue(loan.dueDate) ? "OVERDUE" : "ACTIVE"));
        }
    }

    private static void showOverdueLoans() {
        System.out.println("\n======================== OVERDUE LOANS ========================");

        List<Loan> overdue = loans.stream()
                .filter(loan -> loan.active && isOverdue(loan.dueDate))
                .sorted(Comparator.comparing(loan -> loan.dueDate))
                .toList();

        if (overdue.isEmpty()) {
            System.out.println("No overdue loans.");
            return;
        }

        for (Loan loan : overdue) {
            Book book = findBookById(loan.bookId);
            Member member = findMemberById(loan.memberId);

            long days = java.time.temporal.ChronoUnit.DAYS.between(
                    loan.dueDate,
                    LocalDate.now()
            );

            double fine = days * DAILY_FINE;

            System.out.println("----------------------------------------");
            System.out.println("Loan ID : " + loan.id);
            System.out.println("Book    : " + (book == null ? "Unknown" : book.title));
            System.out.println("Member  : " + (member == null ? "Unknown" : member.name));
            System.out.println("Due     : " + loan.dueDate.format(DATE_FORMAT));
            System.out.println("Days late: " + days);
            System.out.printf("Current fine: ₹%.2f%n", fine);
        }
    }

    // ============================================================
    // REPORTS
    // ============================================================

    private static void showReports() {
        boolean back = false;

        while (!back) {
            System.out.println("\n======================== REPORTS ==============================");
            System.out.println("1. Library summary");
            System.out.println("2. Most borrowed books");
            System.out.println("3. Member loan history");
            System.out.println("4. Category statistics");
            System.out.println("0. Back");

            int choice = readInt("Report choice: ");

            switch (choice) {
                case 1 -> librarySummary();
                case 2 -> mostBorrowedBooks();
                case 3 -> memberLoanHistory();
                case 4 -> categoryStatistics();
                case 0 -> back = true;
                default -> System.out.println("Invalid report option.");
            }
        }
    }

    private static void librarySummary() {
        long available = books.stream()
                .filter(book -> book.available)
                .count();

        long issued = books.size() - available;

        long activeLoans = loans.stream()
                .filter(loan -> loan.active)
                .count();

        long completedLoans = loans.stream()
                .filter(loan -> !loan.active)
                .count();

        double collectedFine = loans.stream()
                .mapToDouble(loan -> loan.fine)
                .sum();

        System.out.println("\n---------------- LIBRARY SUMMARY ----------------");
        System.out.println("Total books       : " + books.size());
        System.out.println("Available books   : " + available);
        System.out.println("Issued books      : " + issued);
        System.out.println("Total members     : " + members.size());
        System.out.println("Active loans      : " + activeLoans);
        System.out.println("Completed loans   : " + completedLoans);
        System.out.printf("Recorded fines    : ₹%.2f%n", collectedFine);
    }

    private static void mostBorrowedBooks() {
        System.out.println("\n---------------- MOST BORROWED BOOKS ------------");

        books.stream()
                .map(book -> new BookBorrowCount(
                        book,
                        (int) loans.stream()
                                .filter(loan -> loan.bookId.equals(book.id))
                                .count()))
                .sorted(Comparator.comparingInt(
                        (BookBorrowCount item) -> item.count
                ).reversed())
                .limit(10)
                .forEach(item -> System.out.printf(
                        "%-35s %d loan(s)%n",
                        truncate(item.book.title, 35),
                        item.count
                ));
    }

    private static void memberLoanHistory() {
        System.out.println("\n---------------- MEMBER HISTORY ------------------");

        String email = readNonEmpty("Member email: ");
        Member member = findMemberByEmail(email);

        if (member == null) {
            System.out.println("Member not found.");
            return;
        }

        List<Loan> history = loans.stream()
                .filter(loan -> loan.memberId.equals(member.id))
                .sorted(Comparator.comparing(
                        (Loan loan) -> loan.issueDate
                ).reversed())
                .toList();

        System.out.println("Member: " + member.name);

        if (history.isEmpty()) {
            System.out.println("No loan history.");
            return;
        }

        for (Loan loan : history) {
            Book book = findBookById(loan.bookId);

            System.out.println("----------------------------------------");
            System.out.println("Book   : " + (book == null ? "Unknown" : book.title));
            System.out.println("Issued : " + loan.issueDate.format(DATE_FORMAT));
            System.out.println("Due    : " + loan.dueDate.format(DATE_FORMAT));
            System.out.println("Status : " + (loan.active ? "Active" : "Returned"));

            if (loan.returnDate != null) {
                System.out.println(
                        "Returned: " + loan.returnDate.format(DATE_FORMAT)
                );
                System.out.printf("Fine   : ₹%.2f%n", loan.fine);
            }
        }
    }

    private static void categoryStatistics() {
        System.out.println("\n---------------- CATEGORY STATISTICS -------------");

        books.stream()
                .map(book -> book.category)
                .distinct()
                .sorted()
                .forEach(category -> {
                    long total = books.stream()
                            .filter(book -> book.category.equalsIgnoreCase(category))
                            .count();

                    long available = books.stream()
                            .filter(book ->
                                    book.category.equalsIgnoreCase(category)
                                            && book.available)
                            .count();

                    System.out.printf(
                            "%-20s Total: %-4d Available: %-4d%n",
                            category,
                            total,
                            available
                    );
                });
    }

    // ============================================================
    // SEARCH / UTILITY
    // ============================================================

    private static Book findBookByIsbn(String isbn) {
        return books.stream()
                .filter(book -> book.isbn.equalsIgnoreCase(isbn))
                .findFirst()
                .orElse(null);
    }

    private static Book findBookById(String id) {
        return books.stream()
                .filter(book -> book.id.equals(id))
                .findFirst()
                .orElse(null);
    }

    private static Member findMemberByEmail(String email) {
        return members.stream()
                .filter(member -> member.email.equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);
    }

    private static Member findMemberById(String id) {
        return members.stream()
                .filter(member -> member.id.equals(id))
                .findFirst()
                .orElse(null);
    }

    private static boolean isOverdue(LocalDate dueDate) {
        return LocalDate.now().isAfter(dueDate);
    }

    private static double calculateFine(LocalDate dueDate, LocalDate returnDate) {
        if (!returnDate.isAfter(dueDate)) {
            return 0;
        }

        long daysLate = java.time.temporal.ChronoUnit.DAYS.between(
                dueDate,
                returnDate
        );

        return daysLate * DAILY_FINE;
    }

    private static String generateId(String prefix) {
        return prefix + "-" +
                UUID.randomUUID().toString()
                        .substring(0, 8)
                        .toUpperCase();
    }

    private static String truncate(String value, int length) {
        if (value == null) {
            return "";
        }

        if (value.length() <= length) {
            return value;
        }

        return value.substring(0, Math.max(0, length - 3)) + "...";
    }

    private static String readNonEmpty(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();

            if (!value.isEmpty()) {
                return value;
            }

            System.out.println("Input cannot be empty.");
        }
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException exception) {
                System.out.println("Please enter a valid integer.");
            }
        }
    }

    private static int readIntRange(String prompt, int min, int max) {
        while (true) {
            int value = readInt(prompt);

            if (value >= min && value <= max) {
                return value;
            }

            System.out.println(
                    "Please enter a value between " + min + " and " + max + "."
            );
        }
    }

    @SuppressWarnings("unused")
    private static LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            try {
                return LocalDate.parse(input, DATE_FORMAT);
            } catch (DateTimeParseException exception) {
                System.out.println(
                        "Invalid date. Use YYYY-MM-DD format."
                );
            }
        }
    }

    // ============================================================
    // SAMPLE DATA
    // ============================================================

    private static void seedData() {
        books.add(new Book(
                "BK-1001",
                "Clean Code",
                "Robert C. Martin",
                "9780132350884",
                "Programming",
                2008
        ));

        books.add(new Book(
                "BK-1002",
                "Effective Java",
                "Joshua Bloch",
                "9780134685991",
                "Programming",
                2018
        ));

        books.add(new Book(
                "BK-1003",
                "The Pragmatic Programmer",
                "David Thomas",
                "9780135957059",
                "Programming",
                2019
        ));

        books.add(new Book(
                "BK-1004",
                "Design Patterns",
                "Erich Gamma",
                "9780201633610",
                "Software Engineering",
                1994
        ));

        books.add(new Book(
                "BK-1005",
                "Introduction to Algorithms",
                "Thomas H. Cormen",
                "9780262046305",
                "Algorithms",
                2022
        ));

        books.add(new Book(
                "BK-1006",
                "Database System Concepts",
                "Abraham Silberschatz",
                "9780078022159",
                "Database",
                2019
        ));

        books.add(new Book(
                "BK-1007",
                "Computer Networks",
                "Andrew S. Tanenbaum",
                "9780132126953",
                "Networking",
                2010
        ));

        books.add(new Book(
                "BK-1008",
                "Operating System Concepts",
                "Abraham Silberschatz",
                "9781119456339",
                "Operating Systems",
                2018
        ));

        members.add(new Member(
                "MB-1001",
                "Aarav Sharma",
                "aarav@example.com",
                "9876543210",
                LocalDate.now().minusMonths(8)
        ));

        members.add(new Member(
                "MB-1002",
                "Priya Patil",
                "priya@example.com",
                "9876501234",
                LocalDate.now().minusMonths(5)
        ));

        members.add(new Member(
                "MB-1003",
                "Rohan Joshi",
                "rohan@example.com",
                "9823012345",
                LocalDate.now().minusMonths(2)
        ));
    }

    // ============================================================
    // DATA MODELS
    // ============================================================

    static class Book {
        String id;
        String title;
        String author;
        String isbn;
        String category;
        int year;
        boolean available;

        Book(
                String id,
                String title,
                String author,
                String isbn,
                String category,
                int year
        ) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.isbn = isbn;
            this.category = category;
            this.year = year;
            this.available = true;
        }
    }

    static class Member {
        String id;
        String name;
        String email;
        String phone;
        LocalDate joinDate;

        Member(
                String id,
                String name,
                String email,
                String phone,
                LocalDate joinDate
        ) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.joinDate = joinDate;
        }
    }

    static class Loan {
        String id;
        String bookId;
        String memberId;
        LocalDate issueDate;
        LocalDate dueDate;
        LocalDate returnDate;
        boolean active;
        double fine;

        Loan(
                String id,
                String bookId,
                String memberId,
                LocalDate issueDate,
                LocalDate dueDate
        ) {
            this.id = id;
            this.bookId = bookId;
            this.memberId = memberId;
            this.issueDate = issueDate;
            this.dueDate = dueDate;
            this.active = true;
            this.fine = 0.0;
        }
    }

    static class BookBorrowCount {
        Book book;
        int count;

        BookBorrowCount(Book book, int count) {
            this.book = book;
            this.count = count;
        }
    }
}
