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

@Epic("Admin Train Management")
@Feature("Search Train")
class AdminSearchTrainTest {

    private AdminSearchTrain servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private RequestDispatcher dispatcher;
    private StringWriter stringWriter;
    private PrintWriter printWriter;
    private TrainService trainService;

    @BeforeEach
    void setUp() throws Exception {
        try (MockedConstruction<TrainServiceImpl> mocked = mockConstruction(TrainServiceImpl.class)) {
            servlet = new AdminSearchTrain();
            trainService = mocked.constructed().get(0);
        }

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        dispatcher = mock(RequestDispatcher.class);
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);

        when(response.getWriter()).thenReturn(printWriter);
        when(request.getRequestDispatcher("AdminSearchTrain.html")).thenReturn(dispatcher);
    }

    @Test
    @Story("Successful search for existing train")
    @DisplayName("Train Found - Displays Train Info")
    @Description("Ensures train information is shown when a valid train number is entered.")
    @Severity(SeverityLevel.CRITICAL)
    void testTrainFound() throws Exception {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            when(request.getParameter("trainnumber")).thenReturn("123");
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.ADMIN)).thenAnswer(inv -> null);

            TrainBean mockTrain = new TrainBean();
            mockTrain.setTr_name("Express");
            when(trainService.getTrainById("123")).thenReturn(mockTrain);

            servlet.doPost(request, response);

            verify(dispatcher).include(request, response);
            printWriter.flush();
            assertTrue(stringWriter.toString().contains("Express"));
        }
    }

    @Test
    @Story("Search with non-existent train number")
    @DisplayName("Train Not Found - Show 'Not Available'")
    @Description("Validates that 'Not Available' is shown when train number is not in database.")
    @Severity(SeverityLevel.NORMAL)
    void testTrainNotFound() throws Exception {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            when(request.getParameter("trainnumber")).thenReturn("999");
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.ADMIN)).thenAnswer(inv -> null);
            when(trainService.getTrainById("999")).thenReturn(null);

            servlet.doPost(request, response);

            verify(dispatcher).include(request, response);
            printWriter.flush();
            assertTrue(stringWriter.toString().contains("Not Available"));
        }
    }

    @Test
    @Story("Exception handling during search")
    @DisplayName("Exception Thrown - Convert to TrainException")
    @Description("Ensures exceptions from service are wrapped and thrown as TrainException.")
    @Severity(SeverityLevel.CRITICAL)
    void testThrowsException() throws Exception {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            when(request.getParameter("trainnumber")).thenReturn("err");
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.ADMIN)).thenAnswer(inv -> null);
            when(trainService.getTrainById("err")).thenThrow(new RuntimeException("DB failed"));

            assertThrows(TrainException.class, () -> servlet.doPost(request, response));
        }
    }
}
