package ac.il.bgu.qa;
import ac.il.bgu.qa.errors.*;
import ac.il.bgu.qa.services.DatabaseService;
import ac.il.bgu.qa.services.NotificationService;
import ac.il.bgu.qa.services.ReviewService;
import org.junit.jupiter.api.*;
import org.mockito.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    @BeforeEach
    void setUpObjects() {
        // set up mocks and library object with the mock dependencies
        MockitoAnnotations.initMocks(this);
        library = new Library(databaseService, reviewService);

        // in order to avoid duplicate code, we will initialize the mock calls for the book and user objects here
        // when we need to change the mock calls for a specific test, we will do it in the test itself
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
        // verify that the adding book in db was not called
        Mockito.verify(databaseService, Mockito.never()).addBook(book.getISBN(), book);
        // verify that the get book by ISBN was called
        Mockito.verify(databaseService).getBookByISBN(book.getISBN());

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
        // verify that the registering user in db was not called
        Mockito.verify(databaseService, Mockito.never()).registerUser(user.getId(), user);
        // verify that the get user by id was called
        Mockito.verify(databaseService).getUserById(user.getId());
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
        // verify that the get book by ISBN was called
        Mockito.verify(databaseService).getBookByISBN(book.getISBN());

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
        // verify that the get user by id was called
        Mockito.verify(databaseService).getUserById(user.getId());
    }

    @Test
    void GivenBookIsBorrowed_WhenBorrowBook_ThenBookIsBorrowedException() {
        // test that an exception is thrown when trying to borrow a book that is already borrowed and the message of the exception is correct
        Mockito.when(book.isBorrowed()).thenReturn(true);
        Mockito.when(databaseService.getBookByISBN(book.getISBN())).thenReturn(book);
        Mockito.when(databaseService.getUserById(user.getId())).thenReturn(user);
        Assertions.assertThrows(BookAlreadyBorrowedException.class, () -> library.borrowBook(book.getISBN(), user.getId()), "Book is already borrowed!");
        // verify that the book was not borrowed
        Mockito.verify(book, Mockito.never()).borrow();
        // verify that the borrowing book in db was not called
        Mockito.verify(databaseService, Mockito.never()).borrowBook(book.getISBN(), user.getId());
        // verify that the get book by ISBN was called
        Mockito.verify(databaseService).getBookByISBN(book.getISBN());
        // verify that the get user by id was called
        Mockito.verify(databaseService).getUserById(user.getId());
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
        // verify that the book was not returned
        Mockito.verify(book, Mockito.never()).returnBook();
        // verify that the returning book in db was not called
        Mockito.verify(databaseService, Mockito.never()).returnBook(book.getISBN());
        // verify that the get book by ISBN was called
        Mockito.verify(databaseService).getBookByISBN(book.getISBN());
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
        // verify that the get book by ISBN was called
        Mockito.verify(databaseService).getBookByISBN(book.getISBN());
    }

    @Test
    void GivenUserNotExist_WhenNotifyUserWithBookReviews_ThenUserNotFoundException() {
        // when getting the book from the database, return a valid book
        Mockito.when(databaseService.getBookByISBN(book.getISBN())).thenReturn(book);
        // when getting the user from the database, return null
        Mockito.when(databaseService.getUserById(user.getId())).thenReturn(null);
        // check user is not found exception is thrown
        Assertions.assertThrows(UserNotRegisteredException.class, () -> library.notifyUserWithBookReviews(book.getISBN(), user.getId()), "User not found!");
        // verify that the get book by ISBN was called
        Mockito.verify(databaseService).getBookByISBN(book.getISBN());
        // verify that the get user by id was called
        Mockito.verify(databaseService).getUserById(user.getId());
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
        // verify that the get book by ISBN was called
        Mockito.verify(databaseService).getBookByISBN(book.getISBN());
        // verify that the get user by id was called
        Mockito.verify(databaseService).getUserById(user.getId());
        // verify that the get reviews for book was called
        Mockito.verify(reviewService).getReviewsForBook(book.getISBN());
    }

    @Test
    void GivenNoReviews_WhenNotifyUserWithBookReviews_ThenNoReviewsException() {
        // when getting the book from the database, return a valid book
        Mockito.when(databaseService.getBookByISBN(book.getISBN())).thenReturn(book);
        // when getting the user from the database, return a valid user
        Mockito.when(databaseService.getUserById(user.getId())).thenReturn(user);
        // Create a spy list
        List<String> spyList = Mockito.spy(new ArrayList<>());
        // when getting the reviews from the review service, return empty list
        Mockito.when(reviewService.getReviewsForBook(book.getISBN())).thenReturn(spyList);
        // check no reviews exception is thrown
        Assertions.assertThrows(NoReviewsFoundException.class, () -> library.notifyUserWithBookReviews(book.getISBN(), user.getId()), "No reviews found!");
        // verify that the get book by ISBN was called
        Mockito.verify(databaseService).getBookByISBN(book.getISBN());
        // verify that the get user by id was called
        Mockito.verify(databaseService).getUserById(user.getId());
        // verify that the get reviews for book was called
        Mockito.verify(reviewService).getReviewsForBook(book.getISBN());
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
        Assertions.assertThrows(ReviewServiceUnavailableException.class, () -> library.notifyUserWithBookReviews(book.getISBN(), user.getId()), "Review service unavailable!");
        // verify that the get book by ISBN was called
        Mockito.verify(databaseService).getBookByISBN(book.getISBN());
        // verify that the get user by id was called
        Mockito.verify(databaseService).getUserById(user.getId());
        // verify that the get reviews for book was called
        Mockito.verify(reviewService).getReviewsForBook(book.getISBN());
    }

    @Test
    void GivenNotificationExceptionThrownForAllRetry_WhenNotifyUserWithBookReviews_ThenThrowNotificationException() {
        // when getting the book from the database, return a valid book
        Mockito.when(databaseService.getBookByISBN(book.getISBN())).thenReturn(book);
        // when getting the user from the database, return a valid user
        Mockito.when(databaseService.getUserById(user.getId())).thenReturn(user);
        // create string list
        List<String> reviews = new ArrayList<>(Arrays.asList("review1"));
        Mockito.when(reviewService.getReviewsForBook(book.getISBN())).thenReturn(reviews);
        // when sending notification, throw exception
        doThrow(new NotificationException("Simulated notification failure")).when(user).sendNotification(anyString());
        // save original err stream
        PrintStream originalErrStream = System.err;
        // create new err stream
        ByteArrayOutputStream newErrStream = new ByteArrayOutputStream();
        // set new err stream
        System.setErr(new PrintStream(newErrStream));
        // make sure right output is printed to console
        String expectedPrint = "Notification failed! Retrying attempt 1/5\r\n" +
                "Notification failed! Retrying attempt 2/5\r\n" +
                "Notification failed! Retrying attempt 3/5\r\n" +
                "Notification failed! Retrying attempt 4/5\r\n" +
                "Notification failed! Retrying attempt 5/5\r\n";
        // check notification exception is thrown
        Assertions.assertThrows(NotificationException.class, () -> library.notifyUserWithBookReviews(book.getISBN(), user.getId()), "Notification failed!");
        // assert that the right output was printed to console
        Assertions.assertEquals(expectedPrint, newErrStream.toString());
        // set original err stream back
        System.setErr(originalErrStream);
    }

    @Test
    void GivenValidBookAndUser_WhenNotifyUserWithBookReviews_ThenNotificationIsSent() {
        // when getting the book from the database, return a valid book
        Mockito.when(databaseService.getBookByISBN(book.getISBN())).thenReturn(book);
        // when getting the user from the database, return a valid user
        Mockito.when(databaseService.getUserById(user.getId())).thenReturn(user);
        // create string list
        List<String> reviews = new ArrayList<>(Arrays.asList("review1"));
        Mockito.when(reviewService.getReviewsForBook(book.getISBN())).thenReturn(reviews);
        // test that a notification is sent successfully
        library.notifyUserWithBookReviews(book.getISBN(), user.getId());
        // verify that the send notification was called
        Mockito.verify(user).sendNotification(anyString());
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
        // verify that the get book by ISBN was called
        Mockito.verify(databaseService).getBookByISBN(book.getISBN());
    }

    @Test
    void GivenBookByISBNBAlreadyBorrowed_WhenGetBookByISBN_ThenBookAlreadyBorrowedException() {
        // when getting the book from the database, return a valid book
        Mockito.when(databaseService.getBookByISBN(book.getISBN())).thenReturn(book);
        // when check if book is borrowed, return true
        Mockito.when(book.isBorrowed()).thenReturn(true);
        Assertions.assertThrows(BookAlreadyBorrowedException.class, () -> library.getBookByISBN(book.getISBN(), user.getId()), "Book was already borrowed!");
        // verify that the get book by ISBN was called
        Mockito.verify(databaseService).getBookByISBN(book.getISBN());
    }

    @Test
    void GivenNoNotifyingUser_WhenGetBookByISBN_ThenExceptionAndBookReturned() {
        // when getting the book from the database, return a valid book
        Mockito.when(databaseService.getBookByISBN(book.getISBN())).thenReturn(book);
        // when check if book is borrowed, return false
        Mockito.when(book.isBorrowed()).thenReturn(false);
        // when getting the user from the database, return a valid user
        Mockito.when(databaseService.getUserById(user.getId())).thenReturn(user);
        // when notifying user, throw exception
        doThrow(new NotificationException("Simulated notification failure")).when(user).sendNotification(anyString());
        // save original out stream
        PrintStream originalOutStream = System.out;
        // create new out stream
        ByteArrayOutputStream newOutStream = new ByteArrayOutputStream();
        // set new out stream
        System.setOut(new PrintStream(newOutStream));
        // make sure right output is printed to console
        String expectedPrint = "Notification failed!\r\n";
        // create a spy for the library object
        Library partialMockLibrary = Mockito.spy(library);
        // verify that the book was returned by function
        Book returnedBook = partialMockLibrary.getBookByISBN(book.getISBN(), user.getId());
        Assertions.assertEquals(returnedBook, book);
        // assert that the right output was printed to console
        Assertions.assertEquals(expectedPrint, newOutStream.toString());
        // set original out stream back
        System.setOut(originalOutStream);
    }

    @Test
    void GivenValidBookAndUser_WhenGetBookByISBN_ThenBookIsReturned() {
        // when getting the book from the database, return a valid book
        Mockito.when(databaseService.getBookByISBN(book.getISBN())).thenReturn(book);
        // when check if book is borrowed, return false
        Mockito.when(book.isBorrowed()).thenReturn(false);
        // when getting the user from the database, return a valid user
        Mockito.when(databaseService.getUserById(user.getId())).thenReturn(user);
        // create a spy for the library object
        Library partialMockLibrary = Mockito.spy(library);
        // when notify user, don't throw exception
        Mockito.doNothing().when(partialMockLibrary).notifyUserWithBookReviews(anyString(), anyString());
        // test that a book was returned by the function
        Book returnedBook = partialMockLibrary.getBookByISBN(book.getISBN(), user.getId());
        Assertions.assertEquals(returnedBook, book);
    }





}
