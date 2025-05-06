package com.shashi.servlets;

import com.shashi.constant.UserRole;
import com.shashi.utility.TrainUtil;
import io.qameta.allure.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@Epic("Train Management System")
@Feature("Admin Train Operations")
@Story("Admin navigates to cancel train page")
class AdminCancleTrainFwdTest {

    @Test
    @DisplayName("Successful Forward to CancleTrain.html")
    @Description("Ensure that a valid admin request forwards to the CancleTrain.html page")
    @Severity(SeverityLevel.CRITICAL)
    void testDoGet_SuccessfulForward() throws Exception {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            HttpServletRequest request = mock(HttpServletRequest.class);
            HttpServletResponse response = mock(HttpServletResponse.class);
            RequestDispatcher dispatcher = mock(RequestDispatcher.class);

            when(request.getRequestDispatcher("CancleTrain.html")).thenReturn(dispatcher);

            AdminCancleTrainFwd servlet = new AdminCancleTrainFwd();
            servlet.doGet(request, response);

            verify(response).setContentType("text/html");
            utilMock.verify(() -> TrainUtil.validateUserAuthorization(request, UserRole.ADMIN));
            verify(dispatcher).forward(request, response);
        }
    }

    @Test
    @DisplayName("Authorization Failure during Forward")
    @Description("Ensure that unauthorized access throws an exception during forward to CancleTrain.html")
    @Severity(SeverityLevel.NORMAL)
    void testDoGet_AuthorizationFailure() {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            HttpServletRequest request = mock(HttpServletRequest.class);
            HttpServletResponse response = mock(HttpServletResponse.class);

            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.ADMIN))
                    .thenThrow(new RuntimeException("Unauthorized"));

            AdminCancleTrainFwd servlet = new AdminCancleTrainFwd();

            assertThrows(RuntimeException.class, () -> servlet.doGet(request, response));

            verify(response).setContentType("text/html");
        }
    }
}
