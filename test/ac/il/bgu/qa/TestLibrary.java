package ac.il.bgu.qa;

import ac.il.bgu.qa.services.DatabaseService;
import ac.il.bgu.qa.services.ReviewService;
import org.junit.jupiter.api.*;
import org.mockito.*;
import ac.il.bgu.qa.Library;

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

    @Test
    void GivenBookIsNull_WhenAddBook_ThenInvalidBookException() {
        // test that an exception is thrown when trying to add a null book and the message of the exception is correct
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.addBook(null), "Invalid book.");
    }

    /*
    @Test
    void GivenInvalidISBN_WhenAddBook_ThenInvalidISBNException() {
        Mockito.when(libraryMock.isISBNValid(book.getISBN())).thenReturn(false);
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.addBook(book), "Invalid ISBN.");
    } */

    @Test
    void GivenInvalidTitle_WhenAddBook_ThenInvalidTitleException() {
        // test that an exception is thrown when trying to add a book with an invalid title and the message of the exception is correct
        Mockito.when(book.getTitle()).thenReturn(null);
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.addBook(book), "Invalid title.");
        Mockito.when(book.getTitle()).thenReturn("");
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.addBook(book), "Invalid title.");
    }

    /*
    @Test
    void GivenInvalidAuthor_WhenAddBook_ThenInvalidAuthorException() {
        // test that an exception is thrown when trying to add a book with an invalid author and the message of the exception is correct
        Mockito.when(book.getAuthor()).thenReturn(null);
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.addBook(book), "Invalid author.");
        Mockito.when(book.getAuthor()).thenReturn("");
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.addBook(book), "Invalid author.");
    } */

    @Test
    void GivenBookIsBorrowed_WhenAddBook_ThenBookIsBorrowedException() {
        // test that an exception is thrown when trying to add a book that is already borrowed and the message of the exception is correct
        Mockito.when(book.isBorrowed()).thenReturn(true);
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.addBook(book), "Book with invalid borrowed state.");
    }

    /*
    @Test
    void GivenBookAlreadyExists_WhenAddBook_ThenBookExistsException() {
        Mockito.when(book.getISBN()).thenReturn("Mocked name");
        Mockito.when(databaseService.getBookByISBN(book.getISBN())).thenReturn(book);
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.addBook(book), "Book already exists.");
    } */




}
