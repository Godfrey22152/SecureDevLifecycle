package com.shashi.servlets;

import com.shashi.beans.TrainBean;
import com.shashi.beans.TrainException;
import com.shashi.service.TrainService;
import com.shashi.service.impl.TrainServiceImpl;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Epic("Train Management")
@Feature("Update Train Schedule")
class UpdateTrainScheduleTest {

    private UpdateTrainSchedule servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private TrainService trainService;
    private RequestDispatcher dispatcher;
    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws Exception {
        try (MockedConstruction<TrainServiceImpl> mocked = mockConstruction(TrainServiceImpl.class)) {
            servlet = new UpdateTrainSchedule();
            trainService = mocked.constructed().get(0);
        }

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        dispatcher = mock(RequestDispatcher.class);
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);

        when(response.getWriter()).thenReturn(printWriter);
        when(request.getRequestDispatcher("AdminUpdateTrain.html")).thenReturn(dispatcher);
    }

    private void setupValidParams() {
        when(request.getParameter("trainno")).thenReturn("12345");
        when(request.getParameter("trainname")).thenReturn("Express");
        when(request.getParameter("fromstation")).thenReturn("StationA");
        when(request.getParameter("tostation")).thenReturn("StationB");
        when(request.getParameter("available")).thenReturn("100");
        when(request.getParameter("fare")).thenReturn("250.0");
    }

    @Test
    @Story("Successful Train Schedule Update")
    @DisplayName("Successfully Update Train Schedule with Valid Data")
    @Description("Test verifies that valid train schedule updates are processed correctly and success message is displayed")
    @Severity(SeverityLevel.CRITICAL)
    void testDoPost_Success() throws Exception {
        setupValidParams();
        when(trainService.updateTrain(any(TrainBean.class))).thenReturn("SUCCESS");

        servlet.doPost(request, response);

        verify(response).setContentType("text/html");
        verify(dispatcher).include(request, response);
        assertTrue(stringWriter.toString().contains("Updated Successfully"));
    }

    @Test
    @Story("Train Schedule Update Failure")
    @DisplayName("Handle Update Failure from Service")
    @Description("Test ensures proper error handling when service layer returns update failure")
    @Severity(SeverityLevel.NORMAL)
    void testDoPost_UpdateFailure() throws Exception {
        setupValidParams();
        when(trainService.updateTrain(any(TrainBean.class))).thenReturn("FAILURE");

        servlet.doPost(request, response);

        assertTrue(stringWriter.toString().contains("Error in filling"));
    }

    @Test
    @Story("Invalid Input Handling")
    @DisplayName("Throw Exception on Invalid Train Number Format")
    @Description("Test validates proper exception handling when invalid numeric input is provided for train number")
    @Severity(SeverityLevel.BLOCKER)
    void testDoPost_InvalidNumberFormat() {
        when(request.getParameter("trainno")).thenReturn("invalid");

        assertThrows(TrainException.class, () -> servlet.doPost(request, response));
    }

    @Test
    @Story("Service Exception Handling")
    @DisplayName("Propagate Service Exceptions as TrainException")
    @Description("Test verifies that underlying service exceptions are properly wrapped and propagated")
    @Severity(SeverityLevel.CRITICAL)
    void testDoPost_ServiceException() {
        setupValidParams();
        when(trainService.updateTrain(any(TrainBean.class)))
            .thenThrow(new RuntimeException("DB Error"));

        assertThrows(TrainException.class, () -> servlet.doPost(request, response));
    }
}
