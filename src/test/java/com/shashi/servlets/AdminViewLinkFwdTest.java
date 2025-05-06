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
import static org.mockito.Mockito.*;

@Epic("Admin Panel")
@Feature("Train Viewing Feature")
@Story("Admin views train details via hyperlink forward")
class AdminViewLinkFwdTest {

    private HttpServletRequest request;
    private HttpServletResponse response;
    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws Exception {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);
    }

    @Test
    @DisplayName("Authorization failure throws RuntimeException")
    @Description("Verifies that unauthorized access by non-admin users results in RuntimeException.")
    @Severity(SeverityLevel.CRITICAL)
    void testDoGet_AuthorizationFailure() {
        try (
            MockedConstruction<TrainServiceImpl> mockedService = mockConstruction(TrainServiceImpl.class);
            MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)
        ) {
            AdminViewLinkFwd servlet = new AdminViewLinkFwd();

            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.ADMIN))
                   .thenThrow(new RuntimeException("Unauthorized"));

            assertThrows(RuntimeException.class, () -> servlet.doGet(request, response));
        }
    }

    @Test
    @DisplayName("Service throws TrainException on train fetch failure")
    @Description("Ensures TrainException is thrown when service layer fails to retrieve train details.")
    @Severity(SeverityLevel.CRITICAL)
    void testDoGet_ServiceException() throws TrainException {
        try (
            MockedConstruction<TrainServiceImpl> mockedService = mockConstruction(TrainServiceImpl.class);
            MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)
        ) {
            AdminViewLinkFwd servlet = new AdminViewLinkFwd();
            TrainService trainService = mockedService.constructed().get(0);

            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.ADMIN))
                   .thenAnswer(inv -> null);
            when(request.getParameter("trainNo")).thenReturn("error");
            when(trainService.getTrainById("error"))
                .thenThrow(new TrainException("DB Error"));

            assertThrows(TrainException.class, () -> servlet.doGet(request, response));
        }
    }

    @Test
    @DisplayName("Train details fetched and included in response")
    @Description("Verifies train data is correctly fetched, forwarded, and rendered in the output HTML.")
    @Severity(SeverityLevel.NORMAL)
    void testDoGet_TrainFound() throws Exception {
        try (
            MockedConstruction<TrainServiceImpl> mockedService = mockConstruction(TrainServiceImpl.class);
            MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)
        ) {
            HttpServletRequest request = mock(HttpServletRequest.class);
            HttpServletResponse response = mock(HttpServletResponse.class);
            RequestDispatcher dispatcher = mock(RequestDispatcher.class);
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);

            when(response.getWriter()).thenReturn(printWriter);
            when(request.getRequestDispatcher("AdminHome.html")).thenReturn(dispatcher);

            AdminViewLinkFwd servlet = new AdminViewLinkFwd();
            TrainService trainService = mockedService.constructed().get(0);

            TrainBean mockTrain = new TrainBean();
            mockTrain.setTr_no(12345L);
            mockTrain.setTr_name("Express");
            mockTrain.setFrom_stn("StationA");
            mockTrain.setTo_stn("StationB");
            mockTrain.setSeats(100);
            mockTrain.setFare(250.0);

            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.ADMIN))
                .thenAnswer(inv -> null);
            when(request.getParameter("trainNo")).thenReturn("12345");
            when(trainService.getTrainById("12345")).thenReturn(mockTrain);

            servlet.doGet(request, response);

            verify(request).getRequestDispatcher("AdminHome.html");
            verify(dispatcher).include(request, response);

            String output = stringWriter.toString();
            assertAll(
                () -> assertTrue(output.contains("Express")),
                () -> assertTrue(output.contains("StationA")),
                () -> assertTrue(output.contains("250.0"))
            );
        }
    }

    @Test
    @DisplayName("Train not found displays error message")
    @Description("Ensures that the fallback path is executed when train is not found (null response).")
    @Severity(SeverityLevel.NORMAL)
    void testDoGet_TrainNotFound() throws Exception {
        try (
            MockedConstruction<TrainServiceImpl> mockedService = mockConstruction(TrainServiceImpl.class);
            MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)
        ) {
            HttpServletRequest request = mock(HttpServletRequest.class);
            HttpServletResponse response = mock(HttpServletResponse.class);
            RequestDispatcher dispatcher = mock(RequestDispatcher.class);
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);

            when(response.getWriter()).thenReturn(printWriter);
            when(request.getRequestDispatcher("AdminSearchTrains.html")).thenReturn(dispatcher);
            when(request.getParameter("trainNo")).thenReturn("99999");
            when(request.getParameter("trainnumber")).thenReturn("99999"); // Important for error message

            AdminViewLinkFwd servlet = new AdminViewLinkFwd();
            TrainService trainService = mockedService.constructed().get(0);

            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.ADMIN))
                .thenAnswer(inv -> null);
            when(trainService.getTrainById("99999")).thenReturn(null); // Train not found

            servlet.doGet(request, response);

            verify(request).getRequestDispatcher("AdminSearchTrains.html");
            verify(dispatcher).include(request, response);

            String output = stringWriter.toString();
            assertTrue(output.contains("Train No.99999 is Not Available"));
        }
    }
}
