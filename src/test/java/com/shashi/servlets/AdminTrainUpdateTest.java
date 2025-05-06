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
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Epic("Admin Train Management")
@Feature("Update Train Servlet")
class AdminTrainUpdateTest {

    private AdminTrainUpdate servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private RequestDispatcher dispatcher;
    private TrainService mockTrainService;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws Exception {
        try (MockedConstruction<TrainServiceImpl> mocked = mockConstruction(TrainServiceImpl.class)) {
            servlet = new AdminTrainUpdate();
            mockTrainService = mocked.constructed().get(0);
        }

        Field serviceField = AdminTrainUpdate.class.getDeclaredField("trainService");
        serviceField.setAccessible(true);
        serviceField.set(servlet, mockTrainService);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        dispatcher = mock(RequestDispatcher.class);
        responseWriter = new StringWriter();

        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    @Test
    @Story("Train Found and Updated")
    @DisplayName("Train Found - Forward to AdminHome with Update Form")
    @Description("Checks that when a valid train is found, the update form is rendered correctly.")
    @Severity(SeverityLevel.CRITICAL)
    void testDoPost_TrainFound() throws Exception {
        TrainBean train = new TrainBean();
        train.setTr_no(123L);
        train.setTr_name("Express");
        train.setFrom_stn("Lagos");
        train.setTo_stn("Abuja");
        train.setSeats(50);
        train.setFare(1000.0);

        when(request.getParameter("trainnumber")).thenReturn("123");
        when(mockTrainService.getTrainById("123")).thenReturn(train);
        when(request.getRequestDispatcher("AdminHome.html")).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(response).setContentType("text/html");
        verify(dispatcher).include(request, response);

        String output = responseWriter.toString();
        assertTrue(output.contains("Train Schedule Update"));
        assertTrue(output.contains("Update Train Schedule"));
    }

    @Test
    @Story("Train Not Found")
    @DisplayName("Train Not Found - Show Error on AdminUpdateTrain Page")
    @Description("Verifies that if the train number is not found, a message is shown to the admin.")
    @Severity(SeverityLevel.NORMAL)
    void testDoPost_TrainNotFound() throws Exception {
        when(request.getParameter("trainnumber")).thenReturn("999");
        when(mockTrainService.getTrainById("999")).thenReturn(null);
        when(request.getRequestDispatcher("AdminUpdateTrain.html")).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(response).setContentType("text/html");
        verify(dispatcher).include(request, response);
        assertTrue(responseWriter.toString().contains("Train Not Available"));
    }

    @Test
    @Story("TrainService throws Exception")
    @DisplayName("Train Exception Thrown - Database Error")
    @Description("Checks that TrainException is thrown properly when a database error occurs.")
    @Severity(SeverityLevel.BLOCKER)
    void testDoPost_ThrowsTrainException() throws Exception {
        when(request.getParameter("trainnumber")).thenReturn("error");
        when(mockTrainService.getTrainById("error"))
            .thenThrow(new TrainException(422, "TEST_FAIL", "DB error"));

        TrainException thrown = assertThrows(TrainException.class, () -> {
            servlet.doPost(request, response);
        });

        assertEquals("DB error", thrown.getMessage());
        assertTrue(thrown.getErrorCode().endsWith("_FAILED"));
        assertEquals(422, thrown.getStatusCode());
    }

    @Test
    @Story("GET Request Delegated to POST")
    @DisplayName("Delegates doGet to doPost - Same Validation Flow")
    @Description("Verifies that doGet delegates properly to doPost for consistent behavior.")
    @Severity(SeverityLevel.MINOR)
    void testDoGet_DelegatesToDoPost() throws Exception {
        when(request.getParameter("trainnumber")).thenReturn("test");
        when(mockTrainService.getTrainById("test")).thenReturn(null);
        when(request.getRequestDispatcher("AdminUpdateTrain.html")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(response).setContentType("text/html");
        verify(dispatcher).include(request, response);
    }
}
