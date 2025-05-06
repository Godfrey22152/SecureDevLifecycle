package com.shashi.servlets;

import com.shashi.beans.TrainBean;
import com.shashi.beans.TrainException;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Epic("Admin Module")
@Feature("Train Management")
@Story("View all trains as Admin via AdminViewTrainFwd servlet")
class AdminViewTrainFwdTest {

    private AdminViewTrainFwd servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private RequestDispatcher dispatcher;
    private StringWriter stringWriter;
    private PrintWriter printWriter;
    private TrainService trainService;

    @BeforeEach
    void setUp() throws Exception {
        try (MockedConstruction<TrainServiceImpl> mocked = mockConstruction(TrainServiceImpl.class)) {
            servlet = new AdminViewTrainFwd();
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
    @DisplayName("Admin views list of available trains")
    @Description("Verify doGet returns HTML content containing train details when trains exist")
    @Severity(SeverityLevel.CRITICAL)
    void testDoGet_WithTrains() throws Exception {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            List<TrainBean> trains = Arrays.asList(
                createTrain(123L, "Express", "StationA", "StationB", 50, 100.0),
                createTrain(456L, "FastTrack", "StationC", "StationD", 30, 150.0)
            );

            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.ADMIN))
                   .thenAnswer(inv -> null);
            when(trainService.getAllTrains()).thenReturn(trains);

            servlet.doGet(request, response);

            verify(response).setContentType("text/html");
            verify(request).getRequestDispatcher("ViewTrains.html");

            String output = stringWriter.toString();
            assertAll(
                () -> assertTrue(output.contains("Running Trains")),
                () -> assertTrue(output.contains("Express")),
                () -> assertTrue(output.contains("FastTrack")),
                () -> assertTrue(output.contains("&#8358;100.0")),
                () -> assertTrue(output.contains("adminupdatetrain?trainnumber=123"))
            );
        }
    }

    @Test
    @DisplayName("Admin views empty train list")
    @Description("Verify doGet returns message indicating no trains are running when train list is empty")
    @Severity(SeverityLevel.NORMAL)
    void testDoGet_NoTrains() throws Exception {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.ADMIN))
                   .thenAnswer(inv -> null);
            when(trainService.getAllTrains()).thenReturn(Collections.emptyList());

            servlet.doGet(request, response);

            String output = stringWriter.toString();
            assertTrue(output.contains("No Running Trains"));
        }
    }

    @Test
    @DisplayName("Unauthorized admin access")
    @Description("Verify doGet throws RuntimeException when admin is not authorized")
    @Severity(SeverityLevel.CRITICAL)
    void testDoGet_AuthorizationFailure() {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.ADMIN))
                   .thenThrow(new RuntimeException("Unauthorized"));

            assertThrows(RuntimeException.class, () -> servlet.doGet(request, response));
        }
    }

    @Test
    @DisplayName("Database exception while fetching train list")
    @Description("Verify doGet throws TrainException when trainService throws an exception")
    @Severity(SeverityLevel.CRITICAL)
    void testDoGet_ServiceException() throws Exception {
        try (MockedStatic<TrainUtil> utilMock = mockStatic(TrainUtil.class)) {
            utilMock.when(() -> TrainUtil.validateUserAuthorization(request, UserRole.ADMIN))
                .thenAnswer(inv -> null);

            when(trainService.getAllTrains()).thenThrow(new TrainException("DB Error"));

            assertThrows(TrainException.class, () -> servlet.doGet(request, response));
        }
    }

    private TrainBean createTrain(long number, String name, String from, String to, int seats, double fare) {
        TrainBean train = new TrainBean();
        train.setTr_no(number);
        train.setTr_name(name);
        train.setFrom_stn(from);
        train.setTo_stn(to);
        train.setSeats(seats);
        train.setFare(fare);
        return train;
    }
}
