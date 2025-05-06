package com.shashi.servlets;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.shashi.beans.TrainException;
import com.shashi.constant.UserRole;
import com.shashi.utility.TrainUtil;

import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

@Epic("Train Management Module")
@Feature("Add Train Forwarding Feature")
@Story("As an admin, I want to forward request to AddTrain page after authorization")
@Severity(SeverityLevel.CRITICAL)
@DisplayName("AddTrainFwd Servlet Test Cases")
class AddTrainFwdTest {

    private AddTrainFwd addTrainFwd;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        addTrainFwd = new AddTrainFwd();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        dispatcher = mock(RequestDispatcher.class);
    }

    @Test
    @Story("Authorized admin user should be able to access AddTrain page")
    @DisplayName("Test DoGet - Authorized Admin User")
    @Description("Verify that an authorized admin user is forwarded to AddTrains.html successfully.")
    @Severity(SeverityLevel.CRITICAL)
    void testDoGet_AuthorizedUser() throws Exception {
        try (MockedStatic<TrainUtil> mockedTrainUtil = Mockito.mockStatic(TrainUtil.class)) {
            mockedTrainUtil.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.ADMIN))
                    .thenAnswer(invocation -> null);

            when(request.getRequestDispatcher("AddTrains.html")).thenReturn(dispatcher);

            addTrainFwd.doGet(request, response);

            verify(response).setContentType("text/html");
            verify(dispatcher).forward(request, response);
        }
    }

    @Test
    @Story("Unauthorized user should be blocked from accessing AddTrain page")
    @DisplayName("Test DoGet - Unauthorized User Throws Exception")
    @Description("Verify that an unauthorized user is prevented from accessing AddTrain page by throwing TrainException.")
    @Severity(SeverityLevel.BLOCKER)
    void testDoGet_UnauthorizedUser() throws Exception {
        try (MockedStatic<TrainUtil> mockedTrainUtil = Mockito.mockStatic(TrainUtil.class)) {
            mockedTrainUtil.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.ADMIN))
                    .thenThrow(new TrainException("Unauthorized"));

            assertThrows(TrainException.class, () -> addTrainFwd.doGet(request, response));
        }
    }
}
