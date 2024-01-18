package ac.il.bgu.qa;

import ac.il.bgu.qa.services.DatabaseService;
import ac.il.bgu.qa.services.ReviewService;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.lang.reflect.Method;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestLibrary {
    // Creating mock objects for the dependencies of the library class
    @Mock
    private DatabaseService databaseService;
    @Mock
    private ReviewService reviewService;
    @Mock
    Library libraryMock;
    @Mock
    Book book;
    @Mock
    User user;
    private Library library;

    @BeforeAll
    void setUp() {
        MockitoAnnotations.initMocks(this);
        library = new Library(databaseService, reviewService);

    }

    @BeforeEach
    void setUpBook() {
        // initialize book mock calls for valid book
        Mockito.when(book.getISBN()).thenReturn("978-0-13-149505-0");
        Mockito.when(book.getTitle()).thenReturn("Mocked title");
        Mockito.when(book.getAuthor()).thenReturn("Mocked author");
        Mockito.when(book.isBorrowed()).thenReturn(false);
    }

    @Test
    void GivenBookIsNull_WhenAddBook_ThenInvalidBookException() {
        // test that an exception is thrown when trying to add a null book and the message of the exception is correct
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.addBook(null), "Invalid book.");
    }

    @Test
    void GivenInvalidISBN_WhenAddBook_ThenInvalidISBNException() {
        // when isbn is called, return an invalid isbn
        Mockito.when(book.getISBN()).thenReturn(null);
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.addBook(book), "Invalid ISBN.");
        Mockito.when(book.getISBN()).thenReturn("");
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.addBook(book), "Invalid ISBN.");
        Mockito.when(book.getISBN()).thenReturn("123");
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.addBook(book), "Invalid ISBN.");
        Mockito.when(book.getISBN()).thenReturn("123-123");
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.addBook(book), "Invalid ISBN.");
        Mockito.when(book.getISBN()).thenReturn("invalidISBN");
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.addBook(book), "Invalid ISBN.");
    }

    @Test
    void GivenInvalidTitle_WhenAddBook_ThenInvalidTitleException() {
        // test that an exception is thrown when trying to add a book with an invalid title and the message of the exception is correct
        Mockito.when(book.getTitle()).thenReturn(null);
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.addBook(book), "Invalid title.");
        Mockito.when(book.getTitle()).thenReturn("");
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.addBook(book), "Invalid title.");
    }

    @Test
    void GivenInvalidAuthor_WhenAddBook_ThenInvalidAuthorException() {
        // test that an exception is thrown when trying to add a book with an invalid author and the message of the exception is correct
        Mockito.when(book.getAuthor()).thenReturn(null);
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.addBook(book), "Invalid author.");
        Mockito.when(book.getAuthor()).thenReturn("");
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.addBook(book), "Invalid author.");
    }

    @Test
    void GivenBookIsBorrowed_WhenAddBook_ThenBookIsBorrowedException() {
        // test that an exception is thrown when trying to add a book that is already borrowed and the message of the exception is correct
        Mockito.when(book.isBorrowed()).thenReturn(true);
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.addBook(book), "Book with invalid borrowed state.");
    }


    @Test
    void GivenBookAlreadyExists_WhenAddBook_ThenBookExistsException() {
        Mockito.when(databaseService.getBookByISBN(book.getISBN())).thenReturn(book);
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.addBook(book), "Book already exists.");
    }

    // tests for registerUser method
    @Test
    void GivenUserIsNull_WhenBorrowBook_ThenInvalidUserException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.borrowBook(null, user.getName()), "Invalid user.") ;
    }

    void GivenIdIsNull_WhenBorrowBook_ThenInvalidISBNException() { // test that an exception is thrown when trying to borrow a book with a null id
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.borrowBook(null, user.getId()), "Invalid user Id.");
    }

    void GivenIdIsNot12Digits_WhenBorrowBook_ThenInvalidISBNException() { // test that an exception is thrown when trying to borrow a book with an id that is not 12 digits
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.borrowBook("123456789", user.getId()), "Invalid user Id.");
    }

    void GivenNotificationServiceIsNull_WhenBorrowBook_ThenInvalidNotificationServiceException() { // test that an exception is thrown when trying to borrow a book with a null notification service
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.borrowBook(null, user.getNotificationService().toString()), "Invalid notification service.");
    }

    void GivenUserAlreadyExists_WhenBorrowBook_ThenUserExistsException() { // test that an exception is thrown when trying to borrow a book with a user that already exists
        Mockito.when(databaseService.getUserById(user.getId())).thenReturn(user);
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.borrowBook(user.getId(), user.getName()), "User already exists.");
    }
}