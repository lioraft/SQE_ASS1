package ac.il.bgu.qa;
import ac.il.bgu.qa.errors.*;
import ac.il.bgu.qa.services.DatabaseService;
import ac.il.bgu.qa.services.NotificationService;
import ac.il.bgu.qa.services.ReviewService;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;

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
        // set up mocks and library object with the mock dependencies
        MockitoAnnotations.initMocks(this);
        library = new Library(databaseService, reviewService);
    }

    @BeforeEach
    void setUpObjects() {
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

    /***  tests for addBook method ***/
    @Test
    void GivenBookIsNull_WhenAddBook_ThenInvalidBookException() {
        // test that an exception is thrown when trying to add a null book and the message of the exception is correct
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.addBook(null), "Invalid book.");
    }

    /*** this function also tests the private function of isValidISBN ***/
    @Test
    void GivenInvalidISBN_WhenAddBook_ThenInvalidISBNException() {
        // case of null
        Mockito.when(book.getISBN()).thenReturn(null);
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.addBook(book), "Invalid ISBN.");
        // case of empty string
        Mockito.when(book.getISBN()).thenReturn("");
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.addBook(book), "Invalid ISBN.");
        // case of length != 13
        Mockito.when(book.getISBN()).thenReturn("123");
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.addBook(book), "Invalid ISBN.");
        // case of not all chars are digits
        Mockito.when(book.getISBN()).thenReturn("123A123123123");
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.addBook(book), "Invalid ISBN.");
        // invalid suffix because last digit should be 4 according to the formula
        Mockito.when(book.getISBN()).thenReturn("9780132350881");
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

    /*** this function also tests private function isAuthorValid ***/
    @Test
    void GivenInvalidAuthor_WhenAddBook_ThenInvalidAuthorException() {
        // case of null
        Mockito.when(book.getAuthor()).thenReturn(null);
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.addBook(book), "Invalid author.");
        // case of empty string
        Mockito.when(book.getAuthor()).thenReturn("");
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.addBook(book), "Invalid author.");
        // case of name starts with digit
        Mockito.when(book.getAuthor()).thenReturn("1author");
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.addBook(book), "Invalid author.");
        // case of name ends with special char
        Mockito.when(book.getAuthor()).thenReturn("author!");
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.addBook(book), "Invalid author.");
        // case of name contains special char
        Mockito.when(book.getAuthor()).thenReturn("auth!or");
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.addBook(book), "Invalid author.");
        // case of double - in name
        Mockito.when(book.getAuthor()).thenReturn("auth--or");
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.addBook(book), "Invalid author.");
        // case of double '' in name
        Mockito.when(book.getAuthor()).thenReturn("auth''or");
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

    @Test
    void GivenValidBook_WhenAddBook_ThenBookIsAdded() {
        // when getting the book from the database, return null
        Mockito.when(databaseService.getBookByISBN(book.getISBN())).thenReturn(null);
        // test that a book is added successfully
        library.addBook(book);
        // verify that the adding book in db was called
        Mockito.verify(databaseService).addBook(book.getISBN(), book);
    }

    /*** tests for registerUser method ***/
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
        // case of 9 digits
        Mockito.when(user.getId()).thenReturn("123456789");
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.registerUser(user), "Invalid user Id.");
        // case of 12 letters and numbers
        Mockito.when(user.getId()).thenReturn("1A2B3C456789");
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.registerUser(user), "Invalid user Id.");
    }

    @Test
    void GivenUserNameIsNull_WhenRegisterUser_ThenInvalidUserNameException() {
        // test that an exception is thrown when trying to register user with a null name
        Mockito.when(user.getName()).thenReturn(null);
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.registerUser(user), "Invalid user name.");
    }
    @Test
    void GivenUserNameIsEmpty_WhenRegisterUser_ThenInvalidUserNameException() {
        // test that an exception is thrown when trying to register user with an empty name
        Mockito.when(user.getName()).thenReturn("");
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.registerUser(user), "Invalid user name.");
    }

    @Test
    void GivenNotificationServiceIsNull_WhenRegisterUser_ThenInvalidNotificationServiceException() {
        // case of null notification service
        Mockito.when(user.getNotificationService()).thenReturn(null);
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.registerUser(user), "Invalid notification service.");
    }

    @Test
    void GivenUserAlreadyExists_WhenRegisterUser_ThenUserExistsException() {
        // test that an exception is thrown when trying to register a user that already exists
        Mockito.when(databaseService.getUserById(user.getId())).thenReturn(user);
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.registerUser(user), "User already exists.");
    }

    @Test
    void GivenValidUser_WhenRegisterUser_ThenUserIsRegistered() {
        // when getting the user from the database, return null
        Mockito.when(databaseService.getUserById(user.getId())).thenReturn(null);
        // test that a user is registered successfully
        library.registerUser(user);
        // verify that the registering user in db was called
        Mockito.verify(databaseService).registerUser(user.getId(), user);
    }

    /*** tests for borrowBook method ***/
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
        // case user id is null
        Mockito.when(user.getId()).thenReturn(null);
        // when getting the book from the database, return a valid book
        Mockito.when(databaseService.getBookByISBN(book.getISBN())).thenReturn(book);
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.borrowBook(book.getISBN(), user.getId()), "Invalid user Id.");
        // case user id is not 12 digits
        Mockito.when(user.getId()).thenReturn("123456789");
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

    /*** tests for returnBook method ***/
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

    /*** tests for notifyUserWithBookReviews method ***/
    @Test
    void GivenInvalidISBN_WhenNotifyUserWithBookReviews_ThenInvalidISBNException() {
        // case of invalid ISBN - null
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.notifyUserWithBookReviews(null, user.getId()), "Invalid ISBN.");
    }

    @Test
    void GivenInvalidUserID_WhenNotifyUserWithBookReviews_ThenInvalidUserIDException() {
        // cass of null user id
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.notifyUserWithBookReviews(book.getISBN(), null), "Invalid user Id.");
        // case of user id is not 12 digits
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.notifyUserWithBookReviews(book.getISBN(), "123456789"), "Invalid user Id.");
    }

    @Test
    void GivenBookNotExist_WhenNotifyUserWithBookReviews_ThenBookNotFoundException() {
        // when getting the book from the database, return null
        Mockito.when(databaseService.getBookByISBN(book.getISBN())).thenReturn(null);
        // check book is not found exception is thrown
        Assertions.assertThrows(BookNotFoundException.class, () -> library.notifyUserWithBookReviews(book.getISBN(), user.getId()), "Book not found!");
    }

    @Test
    void GivenUserNotExist_WhenNotifyUserWithBookReviews_ThenUserNotFoundException() {
        // when getting the book from the database, return a valid book
        Mockito.when(databaseService.getBookByISBN(book.getISBN())).thenReturn(book);
        // when getting the user from the database, return null
        Mockito.when(databaseService.getUserById(user.getId())).thenReturn(null);
        // check user is not found exception is thrown
        Assertions.assertThrows(UserNotRegisteredException.class, () -> library.notifyUserWithBookReviews(book.getISBN(), user.getId()), "User not found!");
    }

    @Test
    void GivenReviewsNull_WhenNotifyUserWithBookReviews_ThenNoReviewsException() {
        // when getting the book from the database, return a valid book
        Mockito.when(databaseService.getBookByISBN(book.getISBN())).thenReturn(book);
        // when getting the user from the database, return a valid user
        Mockito.when(databaseService.getUserById(user.getId())).thenReturn(user);
        // when getting the reviews from the review service, return null
        Mockito.when(reviewService.getReviewsForBook(book.getISBN())).thenReturn(null);
        // check no reviews exception is thrown
        Assertions.assertThrows(NoReviewsFoundException.class, () -> library.notifyUserWithBookReviews(book.getISBN(), user.getId()), "No reviews found!");
    }

    @Test
    void GivenNoReviews_WhenNotifyUserWithBookReviews_ThenNoReviewsException() {
        // when getting the book from the database, return a valid book
        Mockito.when(databaseService.getBookByISBN(book.getISBN())).thenReturn(book);
        // when getting the user from the database, return a valid user
        Mockito.when(databaseService.getUserById(user.getId())).thenReturn(user);
        // create empty list
        String[] reviews = new String[0];
        // when getting the reviews from the review service, return empty list
        Mockito.when(reviewService.getReviewsForBook(book.getISBN())).thenReturn(Arrays.asList(reviews));
        // check no reviews exception is thrown
        Assertions.assertThrows(NoReviewsFoundException.class, () -> library.notifyUserWithBookReviews(book.getISBN(), user.getId()), "No reviews found!");
    }

    @Test
    void GivenReviewException_WhenNotifyUserWithBookReviews_ThenThrowReviewException() {
        // when getting the book from the database, return a valid book
        Mockito.when(databaseService.getBookByISBN(book.getISBN())).thenReturn(book);
        // when getting the user from the database, return a valid user
        Mockito.when(databaseService.getUserById(user.getId())).thenReturn(user);
        // when getting the reviews from the review service, throw exception
        Mockito.when(reviewService.getReviewsForBook(book.getISBN())).thenThrow(ReviewException.class);
        // check review exception is thrown
        Assertions.assertThrows(ReviewServiceUnavailableException.class, () -> library.notifyUserWithBookReviews(book.getISBN(), user.getId()), "Review service exception!");
    }

    // to fix: testing output to console not working
    @Test
    void GivenNotificationExceptionThrownForAllRetry_WhenNotifyUserWithBookReviews_ThenThrowNotificationException() {
        // create stream to hold the output
        ByteArrayOutputStream outMessage = new ByteArrayOutputStream();
        // set the output to the stream
        System.setOut(new PrintStream(outMessage));
        // when getting the book from the database, return a valid book
        Mockito.when(databaseService.getBookByISBN(book.getISBN())).thenReturn(book);
        // when getting the user from the database, return a valid user
        Mockito.when(databaseService.getUserById(user.getId())).thenReturn(user);
        // when getting the reviews from the review service, return a valid list
        String[] reviews = new String[1];
        reviews[0] = "review";
        Mockito.when(reviewService.getReviewsForBook(book.getISBN())).thenReturn(Arrays.asList(reviews));
        // when sending notification, throw exception
        doThrow(new NotificationException("Simulated notification failure")).when(user).sendNotification(anyString());
        // check notification exception is thrown
        Assertions.assertThrows(NotificationException.class, () -> library.notifyUserWithBookReviews(book.getISBN(), user.getId()), "Notification service exception!");
        // Verify that the text was printed to the console
        assertTrue(new PrintStream(outMessage).toString().contains("Notification failed! Retrying attempt 1/5\n" +
                "Notification failed! Retrying attempt 2/5\n" +
                "Notification failed! Retrying attempt 3/5\n" +
                "Notification failed! Retrying attempt 4/5\n" +
                "Notification failed! Retrying attempt 5/5\n"));
    }

    @Test
    void GivenInvalidISBN_WhenGetBookByISBN_ThenInvalidISBNException() {
        // Case invalid ISBN
        Mockito.when(book.getISBN()).thenReturn(null);
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.getBookByISBN(book.getISBN(), user.getId()), "Invalid ISBN.");
    }

    @Test
    void GivenInvalidId_WhenGetBookByISBN_ThenInvalidISBNException() {
        // Case 1: null Id
        Mockito.when(user.getId()).thenReturn(null);
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.getBookByISBN(book.getISBN(), user.getId()), "Invalid user Id.");
        // Case 2: Less than 12 digits is an invalid ID format
        Mockito.when(user.getId()).thenReturn("123");
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.getBookByISBN(book.getISBN(), user.getId()), "Invalid user Id.");
    }

    @Test
    void GivenBookByISBNNotExist_WhenGetBookByISBN_ThenBookNotFoundException() {
        // when getting the book from the database, return null
        Mockito.when(databaseService.getBookByISBN(book.getISBN())).thenReturn(null);
        // check book is not found exception is thrown
        Assertions.assertThrows(BookNotFoundException.class, () -> library.getBookByISBN(book.getISBN(), user.getId()), "Book not found!");
    }

    @Test
    void GivenBookByISBNBAlreadyBorrowed_WhenGetBookByISBN_ThenBookAlreadyBorrowedException() {
        // when getting the book from the database, return a valid book
        Mockito.when(databaseService.getBookByISBN(book.getISBN())).thenReturn(book);
        // when check if book is borrowed, return true
        Mockito.when(book.isBorrowed()).thenReturn(true);
        Assertions.assertThrows(BookAlreadyBorrowedException.class, () -> library.getBookByISBN(book.getISBN(), user.getId()), "Book was already borrowed!");
    }

    // also needs to check that notification failed is printed to screen
    @Test
    void GivenValidBookAndUser_WhenGetBookByISBN_ThenBookIsBorrowed() {
        // when getting the book from the database, return a valid book
        Mockito.when(databaseService.getBookByISBN(book.getISBN())).thenReturn(book);
        // when check if book is borrowed, return false
        Mockito.when(book.isBorrowed()).thenReturn(false);
        // when getting the user from the database, return a valid user
        Mockito.when(databaseService.getUserById(user.getId())).thenReturn(user);
        // test that a book is borrowed successfully and that the book was returned by the function
        Book returnedBook = library.getBookByISBN(book.getISBN(), user.getId());
        Assertions.assertEquals(returnedBook, book);
    }





}
