package ac.il.bgu.qa;

import ac.il.bgu.qa.errors.BookAlreadyBorrowedException;
import ac.il.bgu.qa.errors.BookNotBorrowedException;
import ac.il.bgu.qa.errors.BookNotFoundException;
import ac.il.bgu.qa.errors.UserNotRegisteredException;
import ac.il.bgu.qa.services.DatabaseService;
import ac.il.bgu.qa.services.NotificationService;
import ac.il.bgu.qa.services.ReviewService;
import org.junit.jupiter.api.*;
import org.mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestLibrary {
    // Creating mock objects for the dependencies of the library class
    @Mock
    private DatabaseService databaseService;
    @Mock
    private ReviewService reviewService;
    @Mock
    Book book;
    @Mock
    User user;
    @Mock
    NotificationService notificationService;
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

        // initialize user mock calls for valid user
        Mockito.when(user.getName()).thenReturn("Mocked name");
        Mockito.when(user.getId()).thenReturn("123456789123");
        Mockito.when(user.getNotificationService()).thenReturn(notificationService);
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
    void GivenUserIsNull_WhenRegisterUser_ThenInvalidUserException() {
        // test that an exception is thrown when trying to register null user
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.registerUser(null), "Invalid user.") ;
    }

    @Test
    void GivenIdIsNull_WhenRegisterUser_ThenInvalidIdException() {
        // test that an exception is thrown when trying to register null id user
        Mockito.when(user.getId()).thenReturn(null);
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.registerUser(user), "Invalid user Id.");
    }

    @Test
    void GivenIdIsNot12Digits_WhenRegisterUser_ThenInvalidIdxception() {
        // test that an exception is thrown when user with id less than 12 digits tries to register
        Mockito.when(user.getId()).thenReturn("123456789");
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.registerUser(user), "Invalid user Id.");
    }

    @Test
    void GivenNotificationServiceIsNull_WhenRegisterUser_ThenInvalidNotificationServiceException() {
        // test that an exception is thrown when trying to register user with a null notification service
        Mockito.when(user.getNotificationService()).thenReturn(null);
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.registerUser(user), "Invalid notification service.");
    }

    @Test
    void GivenUserAlreadyExists_WhenRegisterUser_ThenUserExistsException() {
        // test that an exception is thrown when trying to borrow a book with a user that already exists
        Mockito.when(databaseService.getUserById(user.getId())).thenReturn(user);
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.registerUser(user), "User already exists.");
    }

    @Test
    void GiveValidUser_WhenRegisterUser_ThenUserIsRegistered() {
        // test that a user is registered successfully
        library.registerUser(user);
        // verify that the registering user in db was called
        Mockito.verify(databaseService).registerUser(user.getId(), user);
    }


    @Test
    void GivenInvalidISBN_WhenBorrowBook_ThenInvalidISBNException() {
        // test that an exception is thrown when trying to borrow a book with an invalid ISBN and the message of the exception is correct
        Mockito.when(book.getISBN()).thenReturn(null);
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.borrowBook(book.getISBN(), user.getId()), "Invalid ISBN.");
    }

    @Test
    void GivenInvalidBook_WhenBorrowBook_ThenBookNotFoundException() {
        // test that an exception is thrown when trying to borrow a book that does not exist and the message of the exception is correct
        Mockito.when(databaseService.getBookByISBN(book.getISBN())).thenReturn(null);
        Assertions.assertThrows(BookNotFoundException.class, () -> library.borrowBook(book.getISBN(), user.getId()), "Book not found!");
    }

    @Test
    void GivenInvalidUserID_WhenBorrowBook_ThenInvalidUserIDException() {
        // test that an exception is thrown when trying to borrow a book with an invalid user ID and the message of the exception is correct
        Mockito.when(user.getId()).thenReturn(null);
        // when getting the book from the database, return a valid book
        Mockito.when(databaseService.getBookByISBN(book.getISBN())).thenReturn(book);
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.borrowBook(book.getISBN(), user.getId()), "Invalid user Id.");
    }

    @Test
    void GivenInvalidUser_WhenBorrowBook_ThenUserNotFoundException() {
        // test that an exception is thrown when trying to borrow a book to a user that does not exist and the message of the exception is correct
        Mockito.when(databaseService.getBookByISBN(book.getISBN())).thenReturn(book);
        Mockito.when(databaseService.getUserById(user.getId())).thenReturn(null);
        Assertions.assertThrows(UserNotRegisteredException.class, () -> library.borrowBook(book.getISBN(), user.getId()), "User not found!");
    }

    @Test
    void GivenBookIsBorrowed_WhenBorrowBook_ThenBookIsBorrowedException() {
        // test that an exception is thrown when trying to borrow a book that is already borrowed and the message of the exception is correct
        Mockito.when(book.isBorrowed()).thenReturn(true);
        Mockito.when(databaseService.getBookByISBN(book.getISBN())).thenReturn(book);
        Mockito.when(databaseService.getUserById(user.getId())).thenReturn(user);
        Assertions.assertThrows(BookAlreadyBorrowedException.class, () -> library.borrowBook(book.getISBN(), user.getId()), "Book is already borrowed!");
    }

    @Test
    void GivenValidBookAndUser_WhenBorrowBook_ThenBookIsBorrowed() {
        // test that a book is borrowed successfully
        Mockito.when(databaseService.getBookByISBN(book.getISBN())).thenReturn(book);
        Mockito.when(databaseService.getUserById(user.getId())).thenReturn(user);
        library.borrowBook(book.getISBN(), user.getId());
        // verify that the book was borrowed
        Mockito.verify(book).borrow();
        // verify that the borrowing book in db was called
        Mockito.verify(databaseService).borrowBook(book.getISBN(), user.getId());
    }

    @Test
    void GivenInvalidISBN_WhenReturnBook_ThenInvalidISBNException() {
        // test that an exception is thrown when trying to return a book with an invalid ISBN and the message of the exception is correct
        Mockito.when(book.getISBN()).thenReturn(null);
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.returnBook(book.getISBN()), "Invalid ISBN.");
    }

    @Test
    void GivenNullBook_WhenReturnBook_ThenBookNotFoundException() {
        // test that an exception is thrown when trying to return a book that does not exist and the message of the exception is correct
        Mockito.when(databaseService.getBookByISBN(book.getISBN())).thenReturn(null);
        Assertions.assertThrows(BookNotFoundException.class, () -> library.returnBook(book.getISBN()), "Book not found!");
    }

    @Test
    void GivenUnborrowedBook_WhenReturnBook_ThenBookNotBorrowedException() {
        // test that an exception is thrown when trying to return a book that is not borrowed and the message of the exception is correct
        Mockito.when(book.isBorrowed()).thenReturn(false);
        Mockito.when(databaseService.getBookByISBN(book.getISBN())).thenReturn(book);
        Assertions.assertThrows(BookNotBorrowedException.class, () -> library.returnBook(book.getISBN()), "Book wasn't borrowed!");
    }

    @Test
    void GivenBorrowedBook_WhenReturnBook_ThenBookIsReturned() {
        // test that a book is returned successfully
        Mockito.when(book.isBorrowed()).thenReturn(true);
        Mockito.when(databaseService.getBookByISBN(book.getISBN())).thenReturn(book);
        library.returnBook(book.getISBN());
        // verify that the book was returned
        Mockito.verify(book).returnBook();
        // verify that the returning book in db was called
        Mockito.verify(databaseService).returnBook(book.getISBN());
    }

    @Test
    void GivenInvalidISBN_WhenNotifyUserWithBookReviews_ThenInvalidISBNException() {
        // test that an exception is thrown when trying to notify a user with a review with an invalid ISBN and the message of the exception is correct
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.notifyUserWithBookReviews(null, user.getId()), "Invalid ISBN.");
    }

    @Test
    void GivenInvalidUserID_WhenNotifyUserWithBookReviews_ThenInvalidUserIDException() {
        // sending null user id to notifyUserWithBookReviews should throw an exception with the correct message of invalid user id
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.notifyUserWithBookReviews(book.getISBN(), null), "Invalid user Id.");
    }

    @Test
    void GivenBookNotExist_WhenNotifyUserWithBookReviews_ThenBookNotFoundException() {
        // when getting the book from the database, return null
        Mockito.when(databaseService.getBookByISBN(book.getISBN())).thenReturn(null);
        // check book is not found exception is thrown
        Assertions.assertThrows(BookNotFoundException.class, () -> library.notifyUserWithBookReviews(book.getISBN(), user.getId()), "Book not found!");
    }





}
