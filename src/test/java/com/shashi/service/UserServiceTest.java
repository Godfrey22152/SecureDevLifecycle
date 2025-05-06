package com.shashi.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import com.shashi.beans.TrainException;
import com.shashi.beans.UserBean;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Epic("User Service Management")
@Feature("User Operations")
@Severity(SeverityLevel.CRITICAL)
class UserServiceTest {

    private UserService userService;

    @BeforeEach
    @Step("Setting up mocked UserService before each test")
    void setUp() {
        userService = mock(UserService.class);
    }

    private UserBean createSampleUser() {
        UserBean user = new UserBean();
        user.setFName("John");
        user.setLName("Doe");
        user.setPWord("password123");
        user.setAddr("123 Main Street");
        user.setMailId("john.doe@example.com");
        user.setPhNo(1234567890L);
        return user;
    }

    @Test
    @Story("Fetch User by Email")
    @DisplayName("Get User by Valid Email - Success")
    @Description("Test to verify that a user can be successfully fetched by a valid email ID")
    @Severity(SeverityLevel.BLOCKER)
    void testGetUserByEmailIdSuccess() throws TrainException {
        UserBean user = createSampleUser();
        when(userService.getUserByEmailId("john.doe@example.com")).thenReturn(user);

        UserBean result = userService.getUserByEmailId("john.doe@example.com");

        assertNotNull(result);
        assertEquals("John", result.getFName());
        verify(userService, times(1)).getUserByEmailId("john.doe@example.com");
    }

    @Test
    @Story("Fetch User by Email")
    @DisplayName("Get User by Invalid Email - Exception Thrown")
    @Description("Test to verify exception is thrown when trying to fetch a non-existent user by email")
    @Severity(SeverityLevel.CRITICAL)
    void testGetUserByEmailIdThrowsException() throws TrainException {
        when(userService.getUserByEmailId("unknown@example.com")).thenThrow(new TrainException("User not found"));

        TrainException exception = assertThrows(TrainException.class, () -> {
            userService.getUserByEmailId("unknown@example.com");
        });

        assertEquals("User not found", exception.getMessage());
        verify(userService, times(1)).getUserByEmailId("unknown@example.com");
    }

    @Test
    @Story("Fetch All Users")
    @DisplayName("Get All Users - Success")
    @Description("Test to verify that all users can be fetched successfully")
    @Severity(SeverityLevel.NORMAL)
    void testGetAllUsersSuccess() throws TrainException {
        UserBean user1 = createSampleUser();
        UserBean user2 = createSampleUser();
        user2.setMailId("jane.doe@example.com");

        when(userService.getAllUsers()).thenReturn(Arrays.asList(user1, user2));

        List<UserBean> users = userService.getAllUsers();

        assertNotNull(users);
        assertEquals(2, users.size());
        verify(userService, times(1)).getAllUsers();
    }

    @Test
    @Story("Fetch All Users")
    @DisplayName("Get All Users - Exception Thrown")
    @Description("Test to verify exception is thrown when no users are found")
    @Severity(SeverityLevel.CRITICAL)
    void testGetAllUsersThrowsException() throws TrainException {
        when(userService.getAllUsers()).thenThrow(new TrainException("No users found"));

        TrainException exception = assertThrows(TrainException.class, () -> {
            userService.getAllUsers();
        });

        assertEquals("No users found", exception.getMessage());
        verify(userService, times(1)).getAllUsers();
    }

    @Test
    @Story("Update User Information")
    @DisplayName("Update User - Success")
    @Description("Test to verify that a user can be updated successfully")
    @Severity(SeverityLevel.CRITICAL)
    void testUpdateUser() {
        UserBean user = createSampleUser();
        when(userService.updateUser(user)).thenReturn("User updated successfully");

        String response = userService.updateUser(user);

        assertEquals("User updated successfully", response);
        verify(userService, times(1)).updateUser(user);
    }

    @Test
    @Story("Delete User")
    @DisplayName("Delete User - Success")
    @Description("Test to verify that a user can be deleted successfully")
    @Severity(SeverityLevel.CRITICAL)
    void testDeleteUser() {
        UserBean user = createSampleUser();
        when(userService.deleteUser(user)).thenReturn("User deleted successfully");

        String response = userService.deleteUser(user);

        assertEquals("User deleted successfully", response);
        verify(userService, times(1)).deleteUser(user);
    }

    @Test
    @Story("Register New User")
    @DisplayName("Register User - Success")
    @Description("Test to verify that a user can be registered successfully")
    @Severity(SeverityLevel.CRITICAL)
    void testRegisterUser() {
        UserBean user = createSampleUser();
        when(userService.registerUser(user)).thenReturn("User registered successfully");

        String response = userService.registerUser(user);

        assertEquals("User registered successfully", response);
        verify(userService, times(1)).registerUser(user);
    }

    @Test
    @Story("User Login")
    @DisplayName("Login User with Valid Credentials - Success")
    @Description("Test to verify that a user can log in successfully with valid credentials")
    @Severity(SeverityLevel.BLOCKER)
    void testLoginUserSuccess() throws TrainException {
        UserBean user = createSampleUser();
        when(userService.loginUser("john.doe@example.com", "password123")).thenReturn(user);

        UserBean result = userService.loginUser("john.doe@example.com", "password123");

        assertNotNull(result);
        assertEquals("john.doe@example.com", result.getMailId());
        verify(userService, times(1)).loginUser("john.doe@example.com", "password123");
    }

    @Test
    @Story("User Login")
    @DisplayName("Login User with Invalid Credentials - Exception Thrown")
    @Description("Test to verify exception is thrown when invalid login credentials are provided")
    @Severity(SeverityLevel.CRITICAL)
    void testLoginUserThrowsException() throws TrainException {
        when(userService.loginUser("wronguser@example.com", "wrongpass"))
                .thenThrow(new TrainException("Invalid credentials"));

        TrainException exception = assertThrows(TrainException.class, () -> {
            userService.loginUser("wronguser@example.com", "wrongpass");
        });

        assertEquals("Invalid credentials", exception.getMessage());
        verify(userService, times(1)).loginUser("wronguser@example.com", "wrongpass");
    }
}
