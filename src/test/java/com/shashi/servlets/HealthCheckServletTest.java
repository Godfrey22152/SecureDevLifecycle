package com.shashi.servlets;

import com.shashi.utility.DBUtil;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.mockito.MockedStatic;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.mockito.Mockito.*;

@Epic("Health Check")
@Feature("Database Status Endpoint")
class HealthCheckServletTest {

    private final HealthCheckServlet servlet = new HealthCheckServlet();
    private final HttpServletRequest request = mock(HttpServletRequest.class);
    private final HttpServletResponse response = mock(HttpServletResponse.class);

    @Test
    @DisplayName("Test GET request when database is UP")
    @Story("Database Health Check - Database is UP")
    @Description("This test verifies that the HealthCheckServlet returns the status 'UP' when the database is connected.")
    @Severity(SeverityLevel.CRITICAL)
    void testDoGet_DatabaseUp() throws Exception {
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            // Setup
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            when(response.getWriter()).thenReturn(printWriter);
            
            dbUtilMock.when(DBUtil::isDatabaseConnected).thenReturn(true);

            // Execute
            servlet.doGet(request, response);

            // Verify
            verify(response).setContentType("application/json");
            verify(response).setStatus(200);
            assertEquals("{\"status\":\"UP\"}", stringWriter.toString().trim());
        }
    }

    @Test
    @DisplayName("Test GET request when database is DOWN")
    @Story("Database Health Check - Database is DOWN")
    @Description("This test verifies that the HealthCheckServlet returns the status 'DOWN' when the database is not connected.")
    @Severity(SeverityLevel.CRITICAL)
    void testDoGet_DatabaseDown() throws Exception {
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            // Setup
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            when(response.getWriter()).thenReturn(printWriter);
            
            dbUtilMock.when(DBUtil::isDatabaseConnected).thenReturn(false);

            // Execute
            servlet.doGet(request, response);

            // Verify
            verify(response).setContentType("application/json");
            verify(response).setStatus(500);
            assertEquals("{\"status\":\"DOWN\"}", stringWriter.toString().trim());
        }
    }
}
