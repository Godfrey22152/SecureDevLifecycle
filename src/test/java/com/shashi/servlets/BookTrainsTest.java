package com.shashi.servlets;

import com.shashi.beans.HistoryBean;
import com.shashi.beans.TrainBean;
import com.shashi.beans.TrainException;
import com.shashi.constant.ResponseCode;
import com.shashi.constant.UserRole;
import com.shashi.service.BookingService;
import com.shashi.service.TrainService;
import com.shashi.service.impl.BookingServiceImpl;
import com.shashi.service.impl.TrainServiceImpl;
import com.shashi.utility.TrainUtil;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Epic("Train Booking Module")
@Feature("BookTrains Servlet")
class BookTrainsTest {

    private BookTrains servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private ServletContext context;
    private StringWriter stringWriter;
    private PrintWriter printWriter;
    private TrainService trainService;
    private BookingService bookingService;

    @BeforeEach
    void setUp() throws Exception {
        try (
            MockedConstruction<TrainServiceImpl> trainMock = mockConstruction(TrainServiceImpl.class);
            MockedConstruction<BookingServiceImpl> bookingMock = mockConstruction(BookingServiceImpl.class)
        ) {
            servlet = new BookTrains();

            Field trainField = BookTrains.class.getDeclaredField("trainService");
            trainField.setAccessible(true);
            trainService = (TrainService) trainField.get(servlet);

            Field bookingField = BookTrains.class.getDeclaredField("bookingService");
            bookingField.setAccessible(true);
            bookingService = (BookingService) bookingField.get(servlet);
        }

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        context = mock(ServletContext.class);
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);

        when(response.getWriter()).thenReturn(printWriter);
        when(request.getServletContext()).thenReturn(context);
        when(request.getRequestDispatcher(anyString())).thenReturn(mock(RequestDispatcher.class));
    }

    private void setupContextParams(int seats) {
        when(context.getAttribute("seats")).thenReturn(seats);
        when(context.getAttribute("trainnumber")).thenReturn("12345");
        when(context.getAttribute("journeydate")).thenReturn(LocalDate.now().toString());
        when(context.getAttribute("class")).thenReturn("AC");
    }

    @Test
    @Story("Successful booking flow")
    @DisplayName("Should book successfully and return TX ID")
    @Description("This test verifies that a booking succeeds when all conditions are met.")
    @Severity(SeverityLevel.CRITICAL)
    void testDoPost_SuccessfulBooking() throws Exception {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            setupContextParams(2);
            TrainBean mockTrain = new TrainBean();
            mockTrain.setSeats(5);
            mockTrain.setFare(100.0);
            mockTrain.setFrom_stn("StationA");
            mockTrain.setTo_stn("StationB");
            mockTrain.setTr_name("Express");

            HistoryBean mockHistory = new HistoryBean();
            mockHistory.setTransId("TX123");

            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.CUSTOMER)).thenAnswer(inv -> null);
            utilMock.when(() -> TrainUtil.getCurrentUserEmail(request)).thenReturn("user@test.com");
            when(trainService.getTrainById(anyString())).thenReturn(mockTrain);
            when(trainService.updateTrain(any(TrainBean.class))).thenReturn(ResponseCode.SUCCESS.toString());
            when(bookingService.createHistory(any(HistoryBean.class))).thenReturn(mockHistory);

            servlet.doPost(request, response);

            String output = stringWriter.toString();
            assertAll(
                () -> assertTrue(output.contains("Booked Successfully")),
                () -> assertTrue(output.contains("TX123")),
                () -> verify(context).removeAttribute("seat"),
                () -> verify(context).removeAttribute("trainNo"),
                () -> verify(context).removeAttribute("journeyDate"),
                () -> verify(context).removeAttribute("class")
            );
        }
    }

    @Test
    @Story("Booking failure due to insufficient seats")
    @DisplayName("Should fail when requested seats exceed available")
    @Description("Booking fails when user tries to book more seats than available.")
    @Severity(SeverityLevel.NORMAL)
    void testDoPost_InsufficientSeats() throws Exception {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            setupContextParams(10);
            TrainBean mockTrain = new TrainBean();
            mockTrain.setSeats(5);

            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.CUSTOMER)).thenAnswer(inv -> null);
            when(trainService.getTrainById(anyString())).thenReturn(mockTrain);

            servlet.doPost(request, response);

            assertTrue(stringWriter.toString().contains("Only 5 Seats"));
        }
    }

    @Test
    @Story("Booking update failure")
    @DisplayName("Should return failure message if train update fails")
    @Description("Test case to verify handling of update failure in booking.")
    @Severity(SeverityLevel.CRITICAL)
    void testDoPost_UpdateFailure() throws Exception {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            setupContextParams(2);
            TrainBean mockTrain = new TrainBean();
            mockTrain.setSeats(5);

            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.CUSTOMER)).thenAnswer(inv -> null);
            when(trainService.getTrainById(anyString())).thenReturn(mockTrain);
            when(trainService.updateTrain(any(TrainBean.class))).thenReturn("FAILURE");

            servlet.doPost(request, response);

            assertTrue(stringWriter.toString().contains("Transaction Declined"));
        }
    }

    @Test
    @Story("Invalid train selection")
    @DisplayName("Should return invalid train number message")
    @Description("Handles scenario where train ID does not exist.")
    @Severity(SeverityLevel.MINOR)
    void testDoPost_InvalidTrain() throws Exception {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            setupContextParams(2);
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.CUSTOMER)).thenAnswer(inv -> null);
            when(trainService.getTrainById(anyString())).thenReturn(null);

            servlet.doPost(request, response);

            assertTrue(stringWriter.toString().contains("Invalid Train Number"));
        }
    }

    @Test
    @Story("User authorization check")
    @DisplayName("Should throw error if user is not authorized")
    @Description("Validates authorization logic for booking.")
    @Severity(SeverityLevel.CRITICAL)
    void testDoPost_AuthorizationFailure() {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.CUSTOMER))
                   .thenThrow(new RuntimeException("Unauthorized"));

            assertThrows(RuntimeException.class, () -> servlet.doPost(request, response));
        }
    }

    @Test
    @Story("Journey date parse validation")
    @DisplayName("Should throw exception for invalid journey date")
    @Description("Throws exception when parsing an invalid date string from context.")
    @Severity(SeverityLevel.NORMAL)
    void testDoPost_ParseException() {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            when(context.getAttribute("journeydate")).thenReturn("invalid-date");
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.CUSTOMER))
                   .thenAnswer(inv -> null);

            assertThrows(TrainException.class, () -> servlet.doPost(request, response));
        }
    }

    @Test
    @Story("Service layer exception")
    @DisplayName("Should throw TrainException from service layer")
    @Description("Ensure servlet throws exception when TrainService fails.")
    @Severity(SeverityLevel.BLOCKER)
    void testDoPost_ServiceException() throws Exception {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            setupContextParams(2);
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.CUSTOMER)).thenAnswer(inv -> null);
            when(trainService.getTrainById(anyString())).thenThrow(new TrainException("DB Error"));

            assertThrows(TrainException.class, () -> servlet.doPost(request, response));
        }
    }
}
