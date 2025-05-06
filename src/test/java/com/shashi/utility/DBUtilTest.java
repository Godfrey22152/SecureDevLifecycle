package com.shashi.utility;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.shashi.beans.TrainException;

import io.qameta.allure.*;

import org.junit.jupiter.api.*;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Epic("Database Utility Tests")
@Feature("MongoDB Connection Management")
@DisplayName("Unit Tests for DBUtil Class")
class DBUtilTest {

    private static MongoClient mockMongoClient;
    private static MongoDatabase mockDatabase;

    @BeforeAll
    static void setUp() throws Exception {
        mockMongoClient = mock(MongoClient.class);
        mockDatabase = mock(MongoDatabase.class);

        when(mockMongoClient.getDatabase(anyString())).thenReturn(mockDatabase);

        Field clientField = DBUtil.class.getDeclaredField("mongoClient");
        clientField.setAccessible(true);
        clientField.set(null, mockMongoClient);

        Field databaseField = DBUtil.class.getDeclaredField("database");
        databaseField.setAccessible(true);
        databaseField.set(null, mockDatabase);
    }

    @Test
    @Story("Retrieve Mongo Client")
    @DisplayName("Should return mocked MongoClient")
    @Description("Verifies that getMongoClient returns the mocked client")
    @Severity(SeverityLevel.NORMAL)
    void testGetMongoClient() {
        MongoClient client = DBUtil.getMongoClient();
        assertNotNull(client);
        assertEquals(mockMongoClient, client);
    }

    @Test
    @Story("Retrieve DB Name")
    @DisplayName("Should return DB name from environment")
    @Description("Ensures the getDatabaseName method returns DB name from system environment variable")
    @Severity(SeverityLevel.MINOR)
    void testGetDatabaseName() {
        String dbName = System.getenv("DB_NAME");
        String result = DBUtil.getDatabaseName();
        assertEquals(dbName, result);
    }

    @Test
    @Story("Retrieve Mongo Database")
    @DisplayName("Should return mocked MongoDatabase")
    @Description("Verifies getDatabase returns the injected mock MongoDatabase instance")
    @Severity(SeverityLevel.CRITICAL)
    void testGetDatabase_Success() throws TrainException {
        MongoDatabase db = DBUtil.getDatabase();
        assertNotNull(db);
        assertEquals(mockDatabase, db);
    }

    @Test
    @Story("Database Retrieval Failure")
    @DisplayName("Should throw TrainException when database is null")
    @Description("Simulates null MongoDatabase to confirm TrainException is thrown")
    @Severity(SeverityLevel.BLOCKER)
    void testGetDatabase_Failure() throws Exception {
        Field databaseField = DBUtil.class.getDeclaredField("database");
        databaseField.setAccessible(true);
        Object originalDb = databaseField.get(null);
        databaseField.set(null, null);

        TrainException exception = assertThrows(TrainException.class, DBUtil::getDatabase);
        assertEquals("Unable to Connect to DB, Please Check your db credentials in application.properties", exception.getMessage());

        databaseField.set(null, originalDb); // restore
    }

    @Test
    @Story("Verify DB Connectivity")
    @DisplayName("Should return true when database is connected")
    @Description("Checks if isDatabaseConnected correctly detects active connection")
    @Severity(SeverityLevel.NORMAL)
    void testIsDatabaseConnected_Success() {
        MongoIterable<String> mockIterable = mock(MongoIterable.class);
        when(mockIterable.first()).thenReturn("trains");
        when(mockDatabase.listCollectionNames()).thenReturn(mockIterable);

        assertTrue(DBUtil.isDatabaseConnected());
    }

    @Test
    @Story("Handle DB Connectivity Failure")
    @DisplayName("Should return false if listCollectionNames throws exception")
    @Description("Ensures isDatabaseConnected returns false if MongoDB throws an exception")
    @Severity(SeverityLevel.NORMAL)
    void testIsDatabaseConnected_Exception() {
        when(mockDatabase.listCollectionNames()).thenThrow(new RuntimeException("Connection error"));
        assertFalse(DBUtil.isDatabaseConnected());
    }
}
