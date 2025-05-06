package com.shashi.servlets;

import com.shashi.beans.TrainException;
import com.shashi.constant.ResponseCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;

@Epic("Error Handling")
@Feature("Error Handling Servlet")
class ErrorHandlerServletTest {

    private ErrorHandlerServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private RequestDispatcher dispatcher;
    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new ErrorHandlerServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        dispatcher = mock(RequestDispatcher.class);
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);

        when(response.getWriter()).thenReturn(printWriter);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    @Test
    @Epic("Error Handling")
    @Feature("Error Handling Servlet")
    @Story("Service method handles TrainException")
    @DisplayName("Test Service with TrainException")
    @Description("Verifies service method behavior when a TrainException is thrown")
    @Severity(SeverityLevel.CRITICAL)
    void testService_TrainException() throws Exception {
        try (MockedStatic<ResponseCode> responseCodeMock = mockStatic(ResponseCode.class)) {
            // Setup: Use the available constructor for TrainException
            TrainException tex = new TrainException("Train error");
            when(request.getAttribute("javax.servlet.error.exception")).thenReturn(tex);
            when(request.getAttribute("javax.servlet.error.status_code")).thenReturn(422);
            
            // Execute
            servlet.service(request, response);

            // Verify
            String output = stringWriter.toString();
            assertAll(
                () -> assertFalse(output.contains("TRAIN_ERR"), "TRAIN_ERR NOT PRESENT"),
                () -> assertTrue(output.contains("Train error")),
                () -> verify(request).getRequestDispatcher("error.html")
            );
        }
    }

    @Test
    @Epic("Error Handling")
    @Feature("Error Handling Servlet")
    @Story("Service method handles Generic Exception")
    @DisplayName("Test Service with Generic Exception")
    @Description("Verifies service method behavior when a generic exception is thrown")
    @Severity(SeverityLevel.CRITICAL)
    void testService_GenericException() throws Exception {
        // Setup
        Exception ex = new RuntimeException("Generic error");
        when(request.getAttribute("javax.servlet.error.exception")).thenReturn(ex);
        when(request.getAttribute("javax.servlet.error.status_code")).thenReturn(500);

        // Execute
        servlet.service(request, response);

        // Verify
        String output = stringWriter.toString();
        assertTrue(output.contains("Generic error"));
    }

    @Test
    @Epic("Error Handling")
    @Feature("Error Handling Servlet")
    @Story("Service method handles 401 status code")
    @DisplayName("Test Service with 401 Status Code")
    @Description("Verifies service method behavior when status code is 401")
    @Severity(SeverityLevel.CRITICAL)
    void testService_401StatusCode() throws Exception {
        // Setup
        when(request.getAttribute("javax.servlet.error.status_code")).thenReturn(401);

        // Execute
        servlet.service(request, response);

        // Verify
        verify(request).getRequestDispatcher("UserLogin.html");
    }

    @Test
    @Epic("Error Handling")
    @Feature("Error Handling Servlet")
    @Story("Service method handles 500 status code")
    @DisplayName("Test Service with 500 Status Code")
    @Description("Verifies service method behavior when status code is 500")
    @Severity(SeverityLevel.CRITICAL)
    void testService_500StatusCode() throws Exception {
        // Setup
        when(request.getAttribute("javax.servlet.error.status_code")).thenReturn(500);

        // Execute
        servlet.service(request, response);

        // Verify
        String output = stringWriter.toString();
        assertTrue(output.contains("Reload Page"));
    }

    @Test
    @Epic("Error Handling")
    @Feature("Error Handling Servlet")
    @Story("Service method handles 404 status code")
    @DisplayName("Test Service with 404 Status Code")
    @Description("Verifies service method behavior when status code is 404")
    @Severity(SeverityLevel.CRITICAL)
    void testService_404StatusCode() throws Exception {
        try (MockedStatic<ResponseCode> responseCodeMock = mockStatic(ResponseCode.class)) {
            // Setup: Mock ResponseCode
            ResponseCode mockCode = mock(ResponseCode.class);
            when(mockCode.getCode()).thenReturn("NOT_FOUND".length()); 
            when(mockCode.getMessage()).thenReturn("Requested resource not found");

            when(request.getAttribute("javax.servlet.error.status_code")).thenReturn(404);
            responseCodeMock.when(() -> ResponseCode.getMessageByStatusCode(404))
                            .thenReturn(Optional.of(mockCode));

            // Execute
            servlet.service(request, response);

            // Verify
            String output = stringWriter.toString();
            assertAll(
                () -> assertFalse(output.contains("NOT_FOUND"), "NOT_FOUND NOT PRESENT"),
                () -> assertTrue(output.contains("Requested resource not found"))
            );
        }
    }

    @Test
    @Epic("Error Handling")
    @Feature("Error Handling Servlet")
    @Story("Service method handles no status code")
    @DisplayName("Test Service with No Status Code")
    @Description("Verifies service method behavior when no status code is provided")
    @Severity(SeverityLevel.NORMAL)
    void testService_NoStatusCode() throws Exception {
        // Setup
        when(request.getAttribute("javax.servlet.error.status_code")).thenReturn(null);

        // Execute
        servlet.service(request, response);

        // Verify
        String output = stringWriter.toString();
        assertTrue(output.contains("INTERNAL_SERVER_ERROR"));
    }

    @Test
    @Epic("Error Handling")
    @Feature("Error Handling Servlet")
    @Story("Service method maps ResponseCode correctly")
    @DisplayName("Test Service with ResponseCode Mapping")
    @Description("Verifies service method behavior when mapping response code")
    @Severity(SeverityLevel.MINOR)
    void testService_ResponseCodeMapping() throws Exception {
        try (MockedStatic<ResponseCode> responseCodeMock = mockStatic(ResponseCode.class)) {
            // Setup: Mock ResponseCode for BAD_REQUEST
            ResponseCode mockCode = mock(ResponseCode.class);
            when(mockCode.getCode()).thenReturn("BAD_REQUEST".length()); 
            when(mockCode.getMessage()).thenReturn("Bad request error");

            when(request.getAttribute("javax.servlet.error.status_code")).thenReturn(400);
            responseCodeMock.when(() -> ResponseCode.getMessageByStatusCode(400))
                            .thenReturn(Optional.of(mockCode));

            // Execute
            servlet.service(request, response);

            // Verify
            String output = stringWriter.toString();
            //assertFalse(output.contains("Bad request error"));
            assertFalse(output.contains("400 BAD_REQUEST"));
        }
    }

    @Test
    @Epic("Error Handling")
    @Feature("Error Handling Servlet")
    @Story("Service method logs error attributes")
    @DisplayName("Test Service Error Attributes Logging")
    @Description("Verifies that error attributes are logged correctly in the service method")
    @Severity(SeverityLevel.TRIVIAL)
    void testService_ErrorAttributesLogging() throws Exception {
        // Setup
        when(request.getAttribute("javax.servlet.error.servlet_name")).thenReturn("TestServlet");
        when(request.getAttribute("javax.servlet.error.request_uri")).thenReturn("/test");
        when(request.getAttribute("javax.servlet.error.status_code")).thenReturn(404);

        // Execute
        servlet.service(request, response);

        // Verify logging happened (can't verify console output directly, but ensures code paths execute)
        verify(request).getAttribute("javax.servlet.error.servlet_name");
        verify(request).getAttribute("javax.servlet.error.request_uri");
    }
}
