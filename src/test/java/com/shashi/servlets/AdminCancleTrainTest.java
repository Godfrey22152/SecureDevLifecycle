package com.shashi.servlets;

import com.shashi.beans.TrainException;
import com.shashi.constant.ResponseCode;
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

@Epic("Train Management")
@Feature("Train Deletion by Admin")
@Story("Admin can delete train records via AdminCancleTrain servlet")
class AdminCancleTrainTest {

    private AdminCancleTrain servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private RequestDispatcher dispatcher;
    private StringWriter stringWriter;
    private PrintWriter printWriter;
    private TrainService trainService;

    @BeforeEach
    void setUp() throws Exception {
        try (MockedConstruction<TrainServiceImpl> mocked = mockConstruction(TrainServiceImpl.class)) {
            servlet = new AdminCancleTrain();
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
    @DisplayName("Successful train deletion by admin")
    @Description("Verifies that a train is deleted successfully when a valid train number is provided.")
    @Severity(SeverityLevel.CRITICAL)
    void testDoPost_Success() throws Exception {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.ADMIN))
                   .thenAnswer(inv -> null);
            when(request.getParameter("trainno")).thenReturn("123");
            when(trainService.deleteTrainById("123"))
                .thenReturn(ResponseCode.SUCCESS.toString());

            servlet.doPost(request, response);

            verify(response).setContentType("text/html");
            verify(dispatcher).include(request, response);
            assertTrue(stringWriter.toString().contains("Deleted Successfully"));
        }
    }

    @Test
    @DisplayName("Train deletion failed - Train not found")
    @Description("Handles the case where the train number provided does not exist in the system.")
    @Severity(SeverityLevel.NORMAL)
    void testDoPost_TrainNotFound() throws Exception {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.ADMIN))
                   .thenAnswer(inv -> null);
            when(request.getParameter("trainno")).thenReturn("999");
            when(trainService.deleteTrainById("999"))
                .thenReturn("TRAIN_NOT_FOUND");

            servlet.doPost(request, response);

            verify(dispatcher).include(request, response);
            assertTrue(stringWriter.toString().contains("Not Available"));
        }
    }

    @Test
    @DisplayName("Unauthorized admin train deletion attempt")
    @Description("Simulates an unauthorized admin request to delete a train.")
    @Severity(SeverityLevel.CRITICAL)
    void testDoPost_AuthorizationFailure() {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.ADMIN))
                   .thenThrow(new RuntimeException("Unauthorized"));

            assertThrows(RuntimeException.class, () -> servlet.doPost(request, response));
        }
    }

    @Test
    @DisplayName("Exception thrown during train deletion")
    @Description("Tests if the servlet correctly handles unexpected exceptions like DB failures.")
    @Severity(SeverityLevel.BLOCKER)
    void testDoPost_ServiceException() {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.ADMIN))
                   .thenAnswer(inv -> null);
            when(request.getParameter("trainno")).thenReturn("error");
            when(trainService.deleteTrainById("error"))
                .thenThrow(new RuntimeException("DB Error"));

            assertThrows(TrainException.class, () -> servlet.doPost(request, response));
        }
    }
}
