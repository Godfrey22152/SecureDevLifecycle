package com.shashi.service.impl;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.shashi.beans.TrainBean;
import com.shashi.beans.TrainException;
import com.shashi.constant.ResponseCode;
import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Epic("Train Management System")
@Feature("Train Service Implementation")
@Story("MongoDB-backed Train Service Operations")
class TrainServiceImplTest {

    private TrainServiceImpl trainService;
    private MongoCollection<Document> collection;

    @BeforeEach
    @Step("Initialize test environment and mocks")
    void setUp() {
        collection = mock(MongoCollection.class);

        // Mock DBUtil to return our fake collection
        MockedStatic<com.shashi.utility.DBUtil> mockedDBUtil = mockStatic(com.shashi.utility.DBUtil.class);
        com.mongodb.client.MongoClient mongoClient = mock(com.mongodb.client.MongoClient.class);
        com.mongodb.client.MongoDatabase database = mock(com.mongodb.client.MongoDatabase.class);

        mockedDBUtil.when(com.shashi.utility.DBUtil::getMongoClient).thenReturn(mongoClient);
        mockedDBUtil.when(com.shashi.utility.DBUtil::getDatabaseName).thenReturn("test-db");

        when(mongoClient.getDatabase(anyString())).thenReturn(database);
        when(database.getCollection(anyString())).thenReturn(collection);

        trainService = new TrainServiceImpl();
        mockedDBUtil.close(); // Important: close after setup
    }

    @Test
    @DisplayName("Successfully add new train")
    @Story("CRUD Operations")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Test successful creation of new train record in database")
    void testAddTrain_Success() {
        TrainBean train = createSampleTrain();

        FindIterable<Document> mockFindIterable = mock(FindIterable.class);
        when(mockFindIterable.first()).thenReturn(null);
        when(collection.find(any(Document.class))).thenReturn(mockFindIterable);

        String result = trainService.addTrain(train);

        assertEquals(ResponseCode.SUCCESS.toString(), result);
        verify(collection).insertOne(any(Document.class));
    }

    @Test
    @DisplayName("Prevent duplicate train creation")
    @Story("CRUD Operations")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Test failure scenario when adding duplicate train record")
    void testAddTrain_Failure_AlreadyExists() {
        TrainBean train = createSampleTrain();

        FindIterable<Document> mockFindIterable = mock(FindIterable.class);
        when(collection.find(any(Document.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(new Document()); // Simulate that a document is found

        String result = trainService.addTrain(train);

        assertTrue(result.contains(ResponseCode.FAILURE.toString()));
    }

    @Test
    @DisplayName("Successfully delete train by ID")
    @Story("CRUD Operations")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Test successful deletion of existing train record")
    void testDeleteTrainById_Success() {
        DeleteResult deleteResult = mock(DeleteResult.class);
        when(deleteResult.getDeletedCount()).thenReturn(1L);
        when(collection.deleteOne(any(Document.class))).thenReturn(deleteResult);

        String result = trainService.deleteTrainById("123");

        assertEquals(ResponseCode.SUCCESS.toString(), result);
    }

    @Test
    @DisplayName("Handle non-existent train deletion")
    @Story("CRUD Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Test deletion attempt for non-existing train record")
    void testDeleteTrainById_Failure() {
        DeleteResult deleteResult = mock(DeleteResult.class);
        when(deleteResult.getDeletedCount()).thenReturn(0L);
        when(collection.deleteOne(any(Document.class))).thenReturn(deleteResult);

        String result = trainService.deleteTrainById("123");

        assertEquals(ResponseCode.FAILURE.toString(), result);
    }

    @Test
    @DisplayName("Successfully update train information")
    @Story("CRUD Operations")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Test successful update of existing train record")
    void testUpdateTrain_Success() {
        TrainBean train = createSampleTrain();
        UpdateResult updateResult = mock(UpdateResult.class);
        when(updateResult.getModifiedCount()).thenReturn(1L);
        when(collection.updateOne(any(Document.class), any(Document.class))).thenReturn(updateResult);

        String result = trainService.updateTrain(train);

        assertEquals(ResponseCode.SUCCESS.toString(), result);
    }

    @Test
    @DisplayName("Handle update of non-existent train")
    @Story("CRUD Operations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Test update attempt for non-existing train record")
    void testUpdateTrain_Failure() {
        TrainBean train = createSampleTrain();
        UpdateResult updateResult = mock(UpdateResult.class);
        when(updateResult.getModifiedCount()).thenReturn(0L);
        when(collection.updateOne(any(Document.class), any(Document.class))).thenReturn(updateResult);

        String result = trainService.updateTrain(train);

        assertEquals(ResponseCode.FAILURE.toString(), result);
    }

    @Test
    @DisplayName("Retrieve train by valid ID")
    @Story("Train Retrieval")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Test successful retrieval of train record by ID")
    void testGetTrainById_Success() throws TrainException {
        Document doc = createSampleTrainDocument();

        FindIterable<Document> mockFindIterable = mock(FindIterable.class);
        when(collection.find(any(Document.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(doc); // return your sample document

        TrainBean result = trainService.getTrainById("123");

        assertNotNull(result);
        assertEquals(123L, result.getTr_no());
    }

    @Test
    @DisplayName("Handle invalid train ID lookup")
    @Story("Train Retrieval")
    @Severity(SeverityLevel.NORMAL)
    @Description("Test train retrieval with non-existent ID")
    void testGetTrainById_NotFound() {
        FindIterable<Document> mockFindIterable = mock(FindIterable.class);
        when(mockFindIterable.first()).thenReturn(null); // No document found
        when(collection.find(any(Document.class))).thenReturn(mockFindIterable);

        assertThrows(TrainException.class, () -> trainService.getTrainById("123"));
    }

    @Test
    @DisplayName("Retrieve all trains successfully")
    @Story("Train Retrieval")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Test retrieval of all available train records")
    void testGetAllTrains_Success() throws TrainException {
        MongoCursor<Document> cursor = mock(MongoCursor.class);
        when(cursor.hasNext()).thenReturn(true, false);
        when(cursor.next()).thenReturn(createSampleTrainDocument());

        var findIterable = mockFindIterable(cursor);
        when(collection.find()).thenReturn(findIterable);

        List<TrainBean> trains = trainService.getAllTrains();

        assertFalse(trains.isEmpty());
    }

    @Test
    @DisplayName("Handle empty train list retrieval")
    @Story("Train Retrieval")
    @Severity(SeverityLevel.NORMAL)
    @Description("Test empty result handling when no trains exist")
    void testGetAllTrains_NoContent() {
        MongoCursor<Document> cursor = mock(MongoCursor.class);
        when(cursor.hasNext()).thenReturn(false);

        var findIterable = mockFindIterable(cursor);
        when(collection.find()).thenReturn(findIterable);

        assertThrows(TrainException.class, () -> trainService.getAllTrains());
    }

    @Test
    @DisplayName("Find trains between stations successfully")
    @Story("Route Planning")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Test successful retrieval of trains between two stations")
    @Issue("TRAIN-123")
    @TmsLink("TMS-456")
    void testGetTrainsBetweenStations_Success() throws TrainException {
        // Mock a sample train document to be returned from the database
        Document doc = createSampleTrainDocument();

        // Mock MongoCursor to simulate returning the train document
        MongoCursor<Document> cursor = mock(MongoCursor.class);
        when(cursor.hasNext()).thenReturn(true, false);  // First iteration returns true, then false
        when(cursor.next()).thenReturn(doc);  // Return the sample train document on next()

        // Mock the FindIterable to return the cursor when iterator() is called
        FindIterable<Document> mockFindIterable = mock(FindIterable.class);
        when(mockFindIterable.iterator()).thenReturn(cursor);  // Ensure iterator() returns the mocked cursor

        // Mock the collection to return the mocked FindIterable when find() is called
        when(collection.find(any(Bson.class))).thenReturn(mockFindIterable);

        // Call the service method
        List<TrainBean> trains = trainService.getTrainsBetweenStations("CityA", "CityB");

        // Verify that the result is not empty and contains one train
        assertFalse(trains.isEmpty());
        assertEquals(1, trains.size());  // Only one train should be found

        // Verify that find() was called on the collection with the correct filters
        verify(collection).find(any(Bson.class));
        (mockFindIterable).iterator();  // Ensure iterator() was called on FindIterable
        verify(cursor).next();  // Ensure next() was called
    }

    @Test
    @DisplayName("Handle no trains between stations")
    @Story("Route Planning")
    @Severity(SeverityLevel.NORMAL)
    @Description("Test empty result handling for station pairs with no trains")
    void testGetTrainsBetweenStations_NoContent() {
        MongoCursor<Document> cursor = mock(MongoCursor.class);
        when(cursor.hasNext()).thenReturn(false);

        var findIterable = mockFindIterable(cursor);
        when(collection.find(any(Bson.class))).thenReturn(findIterable); // specify Bson.class

        assertThrows(TrainException.class, () -> trainService.getTrainsBetweenStations("A", "B"));
    }

    @Test
    @DisplayName("Handle database errors during station search")
    @Story("Error Handling")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify proper exception handling when database operations fail during station search")
    @Issue("DB-456")
    @TmsLink("TMS-789")
    void testGetTrainsBetweenStations_ExceptionHandling() {
        // Mock the collection to throw an exception when find() is called
        when(collection.find(any(Bson.class))).thenThrow(new RuntimeException("Database error"));

        // Call the service method and verify that it throws a TrainException with the correct message
        assertThrows(TrainException.class, () -> trainService.getTrainsBetweenStations("A", "B"));
    }

    @Test
    @DisplayName("Handle database errors during search")
    @Story("Error Handling")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Test proper exception handling for database failures")
    void testGetTrainsBetweenStations_DatabaseError() {
        // Simulate database failure
        when(collection.find(any(Bson.class))).thenThrow(new RuntimeException("DB connection failed"));

        // Verify exception wrapping
        TrainException exception = assertThrows(TrainException.class,
            () -> trainService.getTrainsBetweenStations("StationA", "StationB"));
    
        assertTrue(exception.getMessage().contains("DB connection failed"));
    }

    // Helper Methods
    private TrainBean createSampleTrain() {
        TrainBean train = new TrainBean();
        train.setTr_no(123L);   // <-- pass as String
        train.setTr_name("Express");
        train.setFrom_stn("CityA");
        train.setTo_stn("CityB");
        train.setSeats(100);
        train.setFare(99.99);
        return train;
    }
    
    private Document createSampleTrainDocument() {
        return new Document("tr_no", 123L)
                .append("tr_name", "Express")
                .append("from_stn", "CityA")
                .append("to_stn", "CityB")
                .append("seats", 100)
                .append("fare", 99.99);
    }

    private com.mongodb.client.FindIterable<Document> mockFindIterable(MongoCursor<Document> cursor) {
        var iterable = mock(com.mongodb.client.FindIterable.class);
        when(iterable.iterator()).thenReturn(cursor);
        return iterable;
    }   
}
