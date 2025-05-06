package com.shashi.servlets;

import com.shashi.beans.TrainBean;
import com.shashi.beans.TrainException;
import com.shashi.constant.ResponseCode;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Epic("Train Management System")
@Feature("Admin Train Operations")
@Story("Admin adds a new train")
class AdminAddTrainTest {

    private AdminAddTrain servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private RequestDispatcher dispatcher;
    private StringWriter stringWriter;
    private PrintWriter printWriter;
    private TrainService trainService;

    @BeforeEach
    void setUp() throws Exception {
        try (MockedConstruction<TrainServiceImpl> mocked = mockConstruction(TrainServiceImpl.class)) {
            servlet = new AdminAddTrain();
            trainService = mocked.constructed().get(0);
        }

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        dispatcher = mock(RequestDispatcher.class);
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);

        when(response.getWriter()).thenReturn(printWriter);
        when(request.getRequestDispatcher(any(String.class))).thenReturn(dispatcher);
    }

    @Test
    @DisplayName("Successful Train Addition")
    @Description("Valid train details submitted by admin result in successful train addition")
    @Severity(SeverityLevel.CRITICAL)
    void testDoPost_Success() throws Exception {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            when(request.getParameter("trainno")).thenReturn("12345");
            when(request.getParameter("trainname")).thenReturn("Express");
            when(request.getParameter("fromstation")).thenReturn("StationA");
            when(request.getParameter("tostation")).thenReturn("StationB");
            when(request.getParameter("available")).thenReturn("100");
            when(request.getParameter("fare")).thenReturn("250.0");

            when(trainService.addTrain(any(TrainBean.class))).thenReturn(ResponseCode.SUCCESS.toString());
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.ADMIN))
                    .thenAnswer(inv -> null);

            servlet.doPost(request, response);

            verify(response).setContentType("text/html");
            verify(dispatcher).include(request, response);
            assertTrue(stringWriter.toString().contains("Added Successfully"));
        }
    }

    @Test
    @DisplayName("Train Addition Failure")
    @Description("Admin submits valid train data but backend fails to add train")
    @Severity(SeverityLevel.NORMAL)
    void testDoPost_AddFailure() throws Exception {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            when(request.getParameter("trainno")).thenReturn("12345");
            when(request.getParameter("trainname")).thenReturn("Express");
            when(request.getParameter("fromstation")).thenReturn("StationA");
            when(request.getParameter("tostation")).thenReturn("StationB");
            when(request.getParameter("available")).thenReturn("100");
            when(request.getParameter("fare")).thenReturn("250.0");

            when(trainService.addTrain(any(TrainBean.class))).thenReturn("ERROR");
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.ADMIN))
                    .thenAnswer(inv -> null);

            servlet.doPost(request, response);

            assertTrue(stringWriter.toString().contains("Error in filling"));
        }
    }

    @Test
    @DisplayName("Invalid Number Format")
    @Description("Admin enters non-numeric train number, causing input parsing failure")
    @Severity(SeverityLevel.MINOR)
    void testDoPost_InvalidNumberFormat() {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            when(request.getParameter("trainno")).thenReturn("invalid");

            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.ADMIN))
                    .thenAnswer(inv -> null);

            assertThrows(TrainException.class, () -> servlet.doPost(request, response));
        }
    }

    @Test
    @DisplayName("Authorization Failure")
    @Description("Admin authorization validation fails, resulting in a RuntimeException")
    @Severity(SeverityLevel.CRITICAL)
    void testDoPost_AuthorizationFailure() {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.ADMIN))
                    .thenThrow(new RuntimeException("Unauthorized"));

            assertThrows(RuntimeException.class, () -> servlet.doPost(request, response));
        }
    }

    @Test
    @DisplayName("Train Service Exception")
    @Description("An exception is thrown by the train service during addTrain operation")
    @Severity(SeverityLevel.NORMAL)
    void testDoPost_ServiceException() {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            when(request.getParameter("trainno")).thenReturn("12345");
            when(request.getParameter("trainname")).thenReturn("Express");
            when(request.getParameter("fromstation")).thenReturn("StationA");
            when(request.getParameter("tostation")).thenReturn("StationB");
            when(request.getParameter("available")).thenReturn("100");
            when(request.getParameter("fare")).thenReturn("250.0");

            when(trainService.addTrain(any(TrainBean.class)))
                    .thenThrow(new RuntimeException("DB Error"));
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.ADMIN))
                    .thenAnswer(inv -> null);

            assertThrows(TrainException.class, () -> servlet.doPost(request, response));
        }
    }
}
