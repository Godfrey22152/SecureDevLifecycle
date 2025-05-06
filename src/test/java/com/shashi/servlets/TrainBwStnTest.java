package com.shashi.servlets;

import com.shashi.beans.TrainBean;
import com.shashi.beans.TrainException;
import com.shashi.constant.UserRole;
import com.shashi.service.TrainService;
import com.shashi.service.impl.TrainServiceImpl;
import com.shashi.utility.TrainUtil;
import io.qameta.allure.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@Epic("Train Movement Between Stations")
@Feature("Trains Between Stations")
@Story("Display Trains Between stations")
class TrainBwStnTest {

    private TrainBwStn servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private RequestDispatcher dispatcher;
    private StringWriter stringWriter;
    private PrintWriter printWriter;
    private TrainService trainService;

    @BeforeEach
    void setUp() throws Exception {
        // Mock service construction
        try (MockedConstruction<TrainServiceImpl> mocked = mockConstruction(TrainServiceImpl.class)) {
            servlet = new TrainBwStn();
            trainService = mocked.constructed().get(0);
        }

        // Setup standard mocks
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        dispatcher = mock(RequestDispatcher.class);
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);

        when(response.getWriter()).thenReturn(printWriter);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    @Test
    @Epic("Train Booking System")
    @Feature("Train Search")
    @Story("Search Trains Between Stations")
    @DisplayName("Test DoPost: Trains Found Between Stations")
    @Description("Test case for searching trains between two stations when trains are available")
    @Severity(SeverityLevel.NORMAL)
    void testDoPost_WithTrains() throws Exception {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            // Setup test data
            List<TrainBean> mockTrains = Arrays.asList(
                createTrain(123L, "Express", "StationA", "StationB", 100, 50.0),
                createTrain(456L, "FastTrack", "StationC", "StationD", 50, 75.0)
            );
            
            // Configure mocks
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.CUSTOMER))
                   .thenAnswer(inv -> null);
            when(request.getParameter("fromstation")).thenReturn("StationA");
            when(request.getParameter("tostation")).thenReturn("StationB");
            when(trainService.getTrainsBetweenStations(anyString(), anyString())).thenReturn(mockTrains);

            // Execute
            servlet.doPost(request, response);

            // Verify
            verify(response).setContentType("text/html");
            verify(request).getRequestDispatcher("UserHome.html");
            
            String output = stringWriter.toString();
            assertAll(
                () -> assertTrue(output.contains("Trains Between Station StationA and StationB")),
                () -> assertTrue(output.contains("Express")),
                () -> assertTrue(output.contains("FastTrack")),
                () -> assertTrue(output.contains("&#8358;50.0")),
                () -> assertTrue(output.contains("booktrainbyref?trainNo=123")),
                () -> assertFalse(output.contains(".*\\d{2}:\\d{2}.*"), "Time format validation not present")
            );
        }
    }

    @Test
    @Epic("Train Booking System")
    @Feature("Train Search")
    @Story("Search Trains Between Stations")
    @DisplayName("Test DoPost: No Trains Found Between Stations")
    @Description("Test case for searching trains between two stations when no trains are available")
    @Severity(SeverityLevel.NORMAL)
    void testDoPost_NoTrains() throws Exception {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            // Configure mocks
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.CUSTOMER))
                   .thenAnswer(inv -> null);
            when(request.getParameter("fromstation")).thenReturn("StationX");
            when(request.getParameter("tostation")).thenReturn("StationY");
            when(trainService.getTrainsBetweenStations(anyString(), anyString())).thenReturn(Collections.emptyList());

            // Execute
            servlet.doPost(request, response);

            // Verify
            verify(request).getRequestDispatcher("TrainBwStn.html");
            assertTrue(stringWriter.toString().contains("There are no trains Between StationX and StationY"));
        }
    }

    @Test
    @Epic("Train Booking System")
    @Feature("Authorization")
    @Story("User Authorization Failure")
    @DisplayName("Test DoPost: Authorization Failure")
    @Description("Test case for handling user authorization failure during train search")
    @Severity(SeverityLevel.CRITICAL)
    void testDoPost_AuthorizationFailure() {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.CUSTOMER))
                   .thenThrow(new RuntimeException("Unauthorized"));

            assertThrows(RuntimeException.class, () -> servlet.doPost(request, response));
        }
    }

    @Test
    @Epic("Train Booking System")
    @Feature("Train Search")
    @Story("Service Exception Handling")
    @DisplayName("Test DoPost: Train Service Exception")
    @Description("Test case for handling service exceptions during train search")
    @Severity(SeverityLevel.BLOCKER)
    void testDoPost_ServiceException() throws TrainException {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.CUSTOMER))
                .thenAnswer(inv -> null);
            when(request.getParameter("fromstation")).thenReturn("StationA");
            when(request.getParameter("tostation")).thenReturn("StationB");

            // Mock the service method to throw a TrainException
            when(trainService.getTrainsBetweenStations(anyString(), anyString()))
                .thenThrow(new TrainException("DB Error"));

            // Ensure that the TrainException is thrown when calling doPost
            assertThrows(TrainException.class, () -> servlet.doPost(request, response));
        }
    }

    private TrainBean createTrain(long number, String name, String from, String to, int seats, double fare) {
        TrainBean train = new TrainBean();
        train.setTr_no(number);
        train.setTr_name(name);
        train.setFrom_stn(from);
        train.setTo_stn(to);
        train.setSeats(seats);
        train.setFare(fare);
        return train;
    }
}
