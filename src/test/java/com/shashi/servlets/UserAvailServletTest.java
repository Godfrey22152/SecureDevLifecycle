package com.shashi.servlets;

import com.shashi.beans.TrainBean;
import com.shashi.beans.TrainException;
import com.shashi.constant.UserRole;
import com.shashi.service.TrainService;
import com.shashi.service.impl.TrainServiceImpl;
import com.shashi.utility.TrainUtil;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Epic("Train Availability")
@Feature("User Train Search")
class UserAvailServletTest {

    private UserAvailServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private RequestDispatcher dispatcher;
    private StringWriter stringWriter;
    private PrintWriter printWriter;
    private TrainService trainService;

    @BeforeEach
    void setUp() throws Exception {
        try (MockedConstruction<TrainServiceImpl> mocked = mockConstruction(TrainServiceImpl.class)) {
            servlet = new UserAvailServlet();
            trainService = mocked.constructed().get(0);
        }

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        dispatcher = mock(RequestDispatcher.class);
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);

        when(response.getWriter()).thenReturn(printWriter);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    @Test
    @Story("Train Availability Check")
    @DisplayName("Display Available Train Details")
    @Description("Test verifies correct display of train information when valid train number is provided")
    @Severity(SeverityLevel.CRITICAL)
    void testDoPost_TrainFound() throws Exception {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.CUSTOMER))
                   .thenAnswer(inv -> null);
            utilMock.when(() -> TrainUtil.getCurrentUserName(request)).thenReturn("John Doe");
            
            TrainBean mockTrain = createTestTrain();
            when(trainService.getTrainById("123")).thenReturn(mockTrain);
            when(request.getParameter("trainno")).thenReturn("123");

            servlet.doPost(request, response);

            verify(response).setContentType("text/html");
            String output = stringWriter.toString();
            assertAll(
                () -> assertTrue(output.contains("Express")),
                () -> assertTrue(output.contains("StationA")),
                () -> assertTrue(output.contains("100")),
                () -> assertTrue(output.contains("&#8358;250.0")),
                () -> assertTrue(output.contains("John Doe"))
            );
        }
    }

    @Test
    @Story("Train Availability Check")
    @DisplayName("Handle Missing Train Gracefully")
    @Description("Test ensures proper handling of invalid train numbers and displays appropriate message")
    @Severity(SeverityLevel.NORMAL)
    void testDoPost_TrainNotFound() throws Exception {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.CUSTOMER))
                   .thenAnswer(inv -> null);
            when(request.getParameter("trainno")).thenReturn("999");
            when(trainService.getTrainById("999")).thenReturn(null);

            servlet.doPost(request, response);

            verify(request).getRequestDispatcher("Availability.html");
            assertTrue(stringWriter.toString().contains("Not Available"));
        }
    }

    @Test
    @Story("Authorization Handling")
    @DisplayName("Block Unauthorized Access")
    @Description("Test verifies proper authorization checks for train availability access")
    @Severity(SeverityLevel.BLOCKER)
    void testDoPost_AuthorizationFailure() {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.CUSTOMER))
                   .thenThrow(new RuntimeException("Unauthorized"));

            assertThrows(RuntimeException.class, () -> servlet.doPost(request, response));
        }
    }

    @Test
    @Story("Error Handling")
    @DisplayName("Handle Database Errors")
    @Description("Test ensures proper exception propagation when service layer throws errors")
    @Severity(SeverityLevel.CRITICAL)
    void testDoPost_ServiceException() throws Exception {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.CUSTOMER))
                   .thenAnswer(inv -> null);
            when(request.getParameter("trainno")).thenReturn("error");
            when(trainService.getTrainById("error")).thenThrow(new RuntimeException("DB Error"));

            assertThrows(TrainException.class, () -> servlet.doPost(request, response));
        }
    }

    private TrainBean createTestTrain() {
        TrainBean train = new TrainBean();
        train.setTr_no(123L);
        train.setTr_name("Express");
        train.setFrom_stn("StationA");
        train.setTo_stn("StationB");
        train.setSeats(100);
        train.setFare(250.0);
        return train;
    }
}
