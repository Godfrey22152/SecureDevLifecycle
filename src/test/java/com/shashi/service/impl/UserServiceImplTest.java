package com.shashi.service.impl;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.shashi.beans.TrainException;
import com.shashi.beans.UserBean;
import com.shashi.constant.ResponseCode;
import com.shashi.constant.UserRole;
import com.shashi.utility.DBUtil;

import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Epic("User Management")
@Feature("User Service Implementation")
@Story("MongoDB User Operations")
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;
    private MongoCollection<Document> mongoCollection;
    private UpdateResult updateResult;
    private DeleteResult deleteResult;
    private UserServiceImpl userService;

    private MockedStatic<DBUtil> dbUtilMockedStatic;

    @BeforeEach
    @Step("Initialize test environment and mocks")
    void setUp() {
        mongoClient = mock(MongoClient.class);
        mongoDatabase = mock(MongoDatabase.class);
        mongoCollection = mock(MongoCollection.class);
        updateResult = mock(UpdateResult.class);
        deleteResult = mock(DeleteResult.class);

        dbUtilMockedStatic = mockStatic(DBUtil.class);
        dbUtilMockedStatic.when(DBUtil::getMongoClient).thenReturn(mongoClient);
        dbUtilMockedStatic.when(DBUtil::getDatabaseName).thenReturn("testdb");

        when(mongoClient.getDatabase(anyString())).thenReturn(mongoDatabase);
        when(mongoDatabase.getCollection(anyString())).thenReturn(mongoCollection);

        userService = new UserServiceImpl(UserRole.CUSTOMER);
    }

    @AfterEach
    void tearDown() {
        dbUtilMockedStatic.close(); // 👈 important! Close after each test
    }

    @Test
    @DisplayName("Get existing user by email")
    @Story("User Retrieval")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verify successful retrieval of user by valid email")
    void testGetUserByEmailId_Found() throws Exception {
        // Create a sample document
        Document document = createSampleDocument();

        // Mock FindIterable
        FindIterable<Document> mockFindIterable = mock(FindIterable.class);
        when(mockFindIterable.first()).thenReturn(document);

        // Mock mongoCollection.find() to return the mocked FindIterable
        when(mongoCollection.find(any(Bson.class))).thenReturn(mockFindIterable);

        // Call the method under test
        UserBean user = userService.getUserByEmailId("test@example.com");

        // Assertions
        assertNotNull(user);
        assertEquals("John", user.getFName());
    }

    @Test
    @DisplayName("Handle non-existent user email lookup")
    @Story("User Retrieval")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify exception handling for invalid email lookup")
    void testGetUserByEmailId_NotFound() {
        // Mock FindIterable
        FindIterable<Document> mockFindIterable = mock(FindIterable.class);
        when(mockFindIterable.first()).thenReturn(null);  // Important: first() returns null when no item is found

        // Mock mongoCollection.find()
        when(mongoCollection.find(any(Bson.class))).thenReturn(mockFindIterable);

        // Now test the exception
        assertThrows(TrainException.class, () -> userService.getUserByEmailId("notfound@example.com"));
    }

    @Test
    @DisplayName("Retrieve all users successfully")
    @Story("User Retrieval")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify retrieval of all registered users")
    void testGetAllUsers_Found() throws Exception {
        // Create sample documents
        Document doc1 = createSampleDocument();
        Document doc2 = createSampleDocument();

        // Mock MongoCursor
        MongoCursor<Document> mockCursor = mock(MongoCursor.class);
        when(mockCursor.hasNext()).thenReturn(true, true, false);  // First true (doc1), second true (doc2), then false
        when(mockCursor.next()).thenReturn(doc1, doc2);            // next() returns doc1 then doc2

        // Mock FindIterable
        FindIterable<Document> mockFindIterable = mock(FindIterable.class);
        when(mockFindIterable.iterator()).thenReturn(mockCursor);

        // Mock mongoCollection.find()
        when(mongoCollection.find()).thenReturn(mockFindIterable);

        // Now call the method under test
        List<UserBean> users = userService.getAllUsers();

        // Assertions
        assertEquals(2, users.size());
    }

    @Test
    @DisplayName("Handle empty user list retrieval")
    @Story("User Retrieval")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify proper exception handling when no users exist in the database")
    void testGetAllUsers_NotFound() {
        // Mock an empty MongoCursor
        MongoCursor<Document> mockCursor = mock(MongoCursor.class);
        when(mockCursor.hasNext()).thenReturn(false); // No documents

        // Mock FindIterable
        FindIterable<Document> mockFindIterable = mock(FindIterable.class);
        when(mockFindIterable.iterator()).thenReturn(mockCursor);

        // Mock mongoCollection.find()
        when(mongoCollection.find()).thenReturn(mockFindIterable);

        // Call and assert
        assertThrows(TrainException.class, () -> userService.getAllUsers());
    }

    @Test
    @DisplayName("Update user information successfully")
    @Story("CRUD Operations")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify successful update of user details")
    void testUpdateUser_Success() {
        when(mongoCollection.updateOne(any(Bson.class), any(Bson.class))).thenReturn(updateResult);
        when(updateResult.getModifiedCount()).thenReturn(1L);

        String result = userService.updateUser(createSampleUser());

        assertEquals(ResponseCode.SUCCESS.toString(), result);
    }

    @Test
    @DisplayName("Handle update of non-existent user")
    @Story("CRUD Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify failure handling for updating non-existing user")
    void testUpdateUser_Failure() {
        when(mongoCollection.updateOne(any(Bson.class), any(Bson.class))).thenReturn(updateResult);
        when(updateResult.getModifiedCount()).thenReturn(0L);

        String result = userService.updateUser(createSampleUser());

        assertEquals(ResponseCode.FAILURE.toString(), result);
    }

    @Test
    @DisplayName("Delete user successfully")
    @Story("CRUD Operations")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify successful deletion of user account")
    void testDeleteUser_Success() {
        when(mongoCollection.deleteOne(any(Bson.class))).thenReturn(deleteResult);
        when(deleteResult.getDeletedCount()).thenReturn(1L);

        String result = userService.deleteUser(createSampleUser());

        assertEquals(ResponseCode.SUCCESS.toString(), result);
    }

    @Test
    @DisplayName("Handle deletion of non-existent user")
    @Story("CRUD Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify failure handling for deleting non-existing user")
    void testDeleteUser_Failure() {
        when(mongoCollection.deleteOne(any(Bson.class))).thenReturn(deleteResult);
        when(deleteResult.getDeletedCount()).thenReturn(0L);

        String result = userService.deleteUser(createSampleUser());

        assertEquals(ResponseCode.FAILURE.toString(), result);
    }

    @Test
    @DisplayName("Register new user successfully")
    @Story("User Registration")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verify successful user registration workflow")
    void testRegisterUser_Success() {
        // Mock FindIterable to simulate no user already existing (important for registration success)
        FindIterable<Document> mockFindIterable = mock(FindIterable.class);
        when(mockFindIterable.first()).thenReturn(null); // Means user not found

        // Mock mongoCollection.find() behavior
        when(mongoCollection.find(any(Bson.class))).thenReturn(mockFindIterable);

        // Call the method
        String result = userService.registerUser(createSampleUser());

        // Assert
        assertEquals(ResponseCode.SUCCESS.toString(), result);
    }

    @Test
    @DisplayName("Prevent duplicate user registration")
    @Story("User Registration")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify system prevents registration when user email already exists")
    @Issue("REG-102")  
    @TmsLink("TMS-205") 
    void testRegisterUser_AlreadyExists() {
        // Mock FindIterable to simulate a user already existing
        FindIterable<Document> mockFindIterable = mock(FindIterable.class);
        when(mockFindIterable.first()).thenReturn(createSampleDocument()); // Means user found

        // Mock mongoCollection.find() behavior
        when(mongoCollection.find(any(Bson.class))).thenReturn(mockFindIterable);

        // Call the method
        String result = userService.registerUser(createSampleUser());

        // Assert
        assertTrue(result.contains("FAILURE"));
    }

    @Test
    @DisplayName("Handle registration database errors")
    @Story("User Registration")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify error handling during user registration")
    void testRegisterUser_Exception() {
        when(mongoCollection.find(any(Bson.class))).thenThrow(new RuntimeException("DB failure"));

        String result = userService.registerUser(createSampleUser());

        assertTrue(result.contains("FAILURE"));
    }

    @Test
    @DisplayName("Authenticate user successfully")
    @Story("Authentication")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verify successful user login with valid credentials")
    void testLoginUser_Success() throws Exception {
        // Mock FindIterable to simulate user found
        FindIterable<Document> mockFindIterable = mock(FindIterable.class);
        when(mockFindIterable.first()).thenReturn(createSampleDocument()); // Only first() is needed

        // Mock mongoCollection.find() behavior
        when(mongoCollection.find(any(Bson.class))).thenReturn(mockFindIterable);

        // Call the method
        UserBean user = userService.loginUser("test@example.com", "password");

        // Assert
        assertNotNull(user);
        assertEquals("John", user.getFName());
    }

    @Test
    @DisplayName("Handle invalid login credentials")
    @Story("Authentication")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify authentication failure with invalid credentials")
    void testLoginUser_Failure() {
        // Mock FindIterable to simulate no document found (null)
        FindIterable<Document> findIterable = mock(FindIterable.class);
        when(findIterable.first()).thenReturn(null);  // No user found, so return null

        // Mock mongoCollection.find() behavior
        when(mongoCollection.find(any(Bson.class))).thenReturn(findIterable);

        // Assert TrainException is thrown when no user found
        assertThrows(TrainException.class, () -> userService.loginUser("wrong@example.com", "wrongpass"));
    }

    // ===================== HELPERS =====================

    private UserBean createSampleUser() {
        UserBean user = new UserBean();
        user.setMailId("test@example.com");
        user.setPWord("password");
        user.setFName("John");
        user.setLName("Doe");
        user.setAddr("123 Street");
        user.setPhNo(1234567890L);
        return user;
    }

    private Document createSampleDocument() {
        return new Document("mailid", "test@example.com")
                .append("pword", "password")
                .append("fname", "John")
                .append("lname", "Doe")
                .append("addr", "123 Street")
                .append("phno", 1234567890L);
    }

    private FindIterable<Document> mockFindIterable(Document... documents) {
        FindIterable<Document> findIterable = mock(FindIterable.class);
        MongoCursor<Document> cursor = mock(MongoCursor.class);

        List<Document> docList = documents == null ? Collections.emptyList() : Arrays.asList(documents);

        when(cursor.hasNext()).thenAnswer(invocation -> !docList.isEmpty());
        when(cursor.next()).thenAnswer(invocation -> {
            if (!docList.isEmpty()) {
                return docList.remove(0);
            }
            throw new NoSuchElementException();
        });

        when(findIterable.iterator()).thenReturn(cursor);
        when(findIterable.first()).thenReturn(documents.length > 0 ? documents[0] : null);

        return findIterable;
    }
}
