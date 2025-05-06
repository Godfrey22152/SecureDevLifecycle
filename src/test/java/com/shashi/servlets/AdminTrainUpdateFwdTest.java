package com.shashi.servlets;

import io.qameta.allure.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@Epic("Admin Panel")
@Feature("Train Update Forwarding")
@Story("Admin forwards to train update page")
class AdminTrainUpdateFwdTest {

    @Test
    @DisplayName("GET request forwards to AdminUpdateTrain.html successfully")
    @Description("Ensures that doGet forwards the request to AdminUpdateTrain.html using RequestDispatcher.")
    @Severity(SeverityLevel.CRITICAL)
    void testDoGet_SuccessfulForward() throws Exception {
        // Create mocks
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        // Configure mocks
        when(request.getRequestDispatcher("AdminUpdateTrain.html")).thenReturn(dispatcher);

        // Create servlet instance
        AdminTrainUpdateFwd servlet = new AdminTrainUpdateFwd();

        // Execute test
        servlet.doGet(request, response);

        // Verify interactions
        verify(response).setContentType("text/html");

        ArgumentCaptor<String> dispatcherCaptor = ArgumentCaptor.forClass(String.class);
        verify(request).getRequestDispatcher(dispatcherCaptor.capture());
        assertEquals("AdminUpdateTrain.html", dispatcherCaptor.getValue());

        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("GET request sets correct content type")
    @Description("Checks that response content type is set to text/html on GET request.")
    @Severity(SeverityLevel.NORMAL)
    void testDoGet_VerifyContentType() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getRequestDispatcher(anyString())).thenReturn(mock(RequestDispatcher.class));

        AdminTrainUpdateFwd servlet = new AdminTrainUpdateFwd();
        servlet.doGet(request, response);

        verify(response).setContentType("text/html");
    }
}
