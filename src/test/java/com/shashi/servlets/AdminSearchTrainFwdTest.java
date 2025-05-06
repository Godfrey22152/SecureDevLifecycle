package com.shashi.servlets;

import com.shashi.constant.UserRole;
import com.shashi.utility.TrainUtil;
import io.qameta.allure.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Epic("Admin Train Management")
@Feature("Search Train Forward Servlet")
class AdminSearchTrainFwdTest {

    @Test
    @Story("Admin accesses train search page successfully")
    @DisplayName("Admin Authorization Success - Forwards to Search Page")
    @Description("Ensures admin is authorized and request is forwarded to search train page.")
    @Severity(SeverityLevel.CRITICAL)
    void testDoGet_SuccessfulAuthorization() throws ServletException, IOException {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            HttpServletRequest request = mock(HttpServletRequest.class);
            HttpServletResponse response = mock(HttpServletResponse.class);
            RequestDispatcher dispatcher = mock(RequestDispatcher.class);

            when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

            AdminSearchTrainFwd servlet = new AdminSearchTrainFwd();

            servlet.doGet(request, response);

            verify(response).setContentType("text/html");
            utilMock.verify(() -> TrainUtil.validateUserAuthorization(request, UserRole.ADMIN));
            verify(dispatcher).forward(request, response);
        }
    }

    @Test
    @Story("Unauthorized user attempts to access train search")
    @DisplayName("Admin Authorization Failure - Throws RuntimeException")
    @Description("Verifies that unauthorized access throws an exception and denies forwarding.")
    @Severity(SeverityLevel.NORMAL)
    void testDoGet_AuthorizationFailure() {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            HttpServletRequest request = mock(HttpServletRequest.class);
            HttpServletResponse response = mock(HttpServletResponse.class);

            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.ADMIN))
                    .thenThrow(new RuntimeException("Unauthorized"));

            AdminSearchTrainFwd servlet = new AdminSearchTrainFwd();

            assertThrows(RuntimeException.class, () -> servlet.doGet(request, response));

            verify(response).setContentType("text/html");
        }
    }
}
