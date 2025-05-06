package com.shashi.service.impl;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.shashi.beans.HistoryBean;
import com.shashi.beans.TrainException;
import com.shashi.constant.ResponseCode;
import com.shashi.utility.DBUtil;
import io.qameta.allure.*;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;

@Epic("Booking Management")
@Feature("Booking Service Implementation")
@Story("MongoDB Booking Operations")
class BookingServiceImplTest {

    private BookingServiceImpl bookingService;
    private MongoCollection<Document> mockCollection;

    @SuppressWarnings("unchecked")
    @BeforeEach
    @Step("Initialize test environment and mocks")
    void setUp() {
        mockCollection = mock(MongoCollection.class, withSettings().defaultAnswer(RETURNS_DEEP_STUBS));
        MongoDatabase mockDatabase = mock(MongoDatabase.class);
        MongoClient mockClient = mock(MongoClient.class);

        when(mockClient.getDatabase(DBUtil.getDatabaseName())).thenReturn(mockDatabase);
        when(mockDatabase.getCollection("history")).thenReturn(mockCollection);

        try (MockedStatic<DBUtil> dbUtilMockedStatic = mockStatic(DBUtil.class)) {
            dbUtilMockedStatic.when(DBUtil::getMongoClient).thenReturn(mockClient);
            bookingService = new BookingServiceImpl();
        }
    }

    @Test
    @DisplayName("Retrieve customer bookings successfully")
    @Story("Booking Retrieval")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verify successful retrieval of all bookings for a valid customer ID")
    void testGetAllBookingsByCustomerId_Success() throws TrainException {
        String customerEmailId = "test@example.com";
        List<Document> mockDocuments = new ArrayList<>();
        mockDocuments.add(new Document("transid", "123")
                .append("mailid", customerEmailId)
                .append("tr_no", "TR001")
                .append("date", "2023-10-01")
                .append("from_stn", "StationA")
                .append("to_stn", "StationB")
                .append("seats", 2)
                .append("amount", 500.0));

        FindIterable<Document> mockFindIterable = mock(FindIterable.class);
        MongoCursor<Document> mockCursor = mock(MongoCursor.class);
        when(mockCursor.hasNext()).thenReturn(true, false);
        when(mockCursor.next()).thenReturn(mockDocuments.get(0));
        when(mockFindIterable.iterator()).thenReturn(mockCursor);
        when(mockCollection.find(eq("mailid", customerEmailId))).thenReturn(mockFindIterable);

        List<HistoryBean> bookings = bookingService.getAllBookingsByCustomerId(customerEmailId);

        assertAll(
            () -> assertEquals(1, bookings.size(), "Should return exactly one booking"),
            () -> assertEquals("123", bookings.get(0).getTransId(), "Transaction ID should match"),
            () -> verify(mockCollection).find(eq("mailid", customerEmailId))
        );
    }

    @Test
    @DisplayName("Handle no bookings found for customer")
    @Story("Booking Retrieval")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify proper exception handling when no bookings exist for customer")
    void testGetAllBookingsByCustomerId_NoContent() {
        String customerEmailId = "test@example.com";
        FindIterable<Document> mockFindIterable = mock(FindIterable.class);
        MongoCursor<Document> mockCursor = mock(MongoCursor.class);
        when(mockCursor.hasNext()).thenReturn(false);
        when(mockFindIterable.iterator()).thenReturn(mockCursor);
        when(mockCollection.find(eq("mailid", customerEmailId))).thenReturn(mockFindIterable);

        TrainException exception = assertThrows(TrainException.class, 
            () -> bookingService.getAllBookingsByCustomerId(customerEmailId));

        assertAll(
            () -> assertEquals(ResponseCode.NO_CONTENT.getMessage(), exception.getMessage(),
                "Should return NO_CONTENT message"),
            () -> verify(mockCollection).find(eq("mailid", customerEmailId))
        );
    }

    @Test
    @DisplayName("Create booking history successfully")
    @Story("Booking Creation")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify successful creation of booking history record")
    void testCreateHistory_Success() throws TrainException {
        HistoryBean historyBean = new HistoryBean();
        historyBean.setMailId("test@example.com");
        historyBean.setTr_no("TR001");
        historyBean.setDate("2023-10-01");
        historyBean.setFrom_stn("StationA");
        historyBean.setTo_stn("StationB");
        historyBean.setSeats(2);
        historyBean.setAmount(500.0);

        HistoryBean result = bookingService.createHistory(historyBean);

        assertAll(
            () -> assertNotNull(result.getTransId(), "Transaction ID should be generated"),
            () -> verify(mockCollection, times(1)).insertOne(any(Document.class))
        );
    }

    @Test
    @DisplayName("Handle booking creation failure")
    @Story("Booking Creation")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify proper exception handling when booking creation fails")
    @Issue("BOOK-102")
    @TmsLink("TMS-305")
    void testCreateHistory_Exception() {
        HistoryBean historyBean = new HistoryBean();
        when(mockCollection.insertOne(any(Document.class))).thenThrow(new RuntimeException("Insert failed"));

        TrainException exception = assertThrows(TrainException.class, 
            () -> bookingService.createHistory(historyBean));

        assertAll(
            () -> assertEquals("Insert failed", exception.getMessage(),
                "Should propagate database error message"),
            () -> verify(mockCollection).insertOne(any(Document.class))
        );
    }
}
