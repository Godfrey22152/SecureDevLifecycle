package com.shashi.servlets;

import com.shashi.beans.TrainException;
import com.shashi.constant.UserRole;
import com.shashi.utility.TrainUtil;
import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@Epic("User Interface Flow")
@Feature("Search Functionality Access")
@ExtendWith(MockitoExtension.class)
class UserSearchFwdTest {

    @Mock
    private HttpServletRequest request;
    
    @Mock
    private HttpServletResponse response;
    
    @Mock
    private RequestDispatcher requestDispatcher;

    private final UserSearchFwd servlet = new UserSearchFwd();

    @Test
    @Story("Authorization Validation")
    @DisplayName("Forward Authorized User to Search Page")
    @Description("Test verifies that authenticated CUSTOMER role users are forwarded to SearchTrains.html")
    @Severity(SeverityLevel.CRITICAL)
    void doGet_AuthorizedUser_ForwardsToSearchPage() throws Exception {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            when(request.getRequestDispatcher("SearchTrains.html")).thenReturn(requestDispatcher);

            servlet.doGet(request, response);

            verify(response).setContentType("text/html");
            verify(requestDispatcher).forward(request, response);
            utilMock.verify(() -> TrainUtil.validateUserAuthorization(request, UserRole.CUSTOMER));
        }
    }

    @Test
    @Story("Authorization Failure Handling")
    @DisplayName("Block Unauthorized Access to Search")
    @Description("Test ensures unauthorized users receive TrainException and cannot access search functionality")
    @Severity(SeverityLevel.BLOCKER)
    void doGet_UnauthorizedUser_ThrowsException() {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.CUSTOMER))
                   .thenThrow(new TrainException("Unauthorized"));

            assertThrows(TrainException.class, () -> servlet.doGet(request, response));
            verify(response).setContentType("text/html");
            verifyNoInteractions(requestDispatcher);
        }
    }
}
