package ps.backend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import ps.backend.dto.UserDTO;
import ps.backend.model.Role;
import ps.backend.service.UserService;
import ps.backend.controller.UserController.PasswordChangeRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private UserController userController;

    private UserDTO testUser;
    private UserDTO adminUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Set up security context mock
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Create test users
        testUser = new UserDTO();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(Role.CLIENT);
        testUser.setPhoneNumber("1234567890");

        adminUser = new UserDTO();
        adminUser.setId(2L);
        adminUser.setEmail("admin@example.com");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setRole(Role.ADMIN);
        adminUser.setPhoneNumber("0987654321");
    }

    @Test
    void getCurrentUser_shouldReturnUser_whenUserExists() {
        // Set up the authentication to return the test user's email
        when(authentication.getName()).thenReturn(testUser.getEmail());

        // Mock service to return the test user
        when(userService.getUserByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        // Call the controller method
        ResponseEntity<UserDTO> response = userController.getCurrentUser();

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testUser.getId(), response.getBody().getId());
        assertEquals(testUser.getEmail(), response.getBody().getEmail());

        // Verify the service was called correctly
        verify(userService).getUserByEmail(testUser.getEmail());
    }

    @Test
    void getCurrentUser_shouldReturnNotFound_whenUserDoesNotExist() {
        // Set up the authentication to return a non-existent email
        when(authentication.getName()).thenReturn("nonexistent@example.com");

        // Mock service to return empty
        when(userService.getUserByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Call the controller method
        ResponseEntity<UserDTO> response = userController.getCurrentUser();

        // Verify the response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());

        // Verify the service was called correctly
        verify(userService).getUserByEmail("nonexistent@example.com");
    }

    @Test
    void getUserById_shouldReturnUser_whenUserIsAdmin() {
        // Set up the authentication to return the admin user's email
        when(authentication.getName()).thenReturn(adminUser.getEmail());

        // Mock service to return users
        when(userService.getUserByEmail(adminUser.getEmail())).thenReturn(Optional.of(adminUser));
        when(userService.getUserById(testUser.getId())).thenReturn(Optional.of(testUser));

        // Call the controller method
        ResponseEntity<UserDTO> response = userController.getUserById(testUser.getId());

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testUser.getId(), response.getBody().getId());

        // Verify the services were called correctly
        verify(userService).getUserByEmail(adminUser.getEmail());
        verify(userService).getUserById(testUser.getId());
    }

    @Test
    void getUserById_shouldReturnUser_whenUserIsAccessingOwnProfile() {
        // Set up the authentication to return the test user's email
        when(authentication.getName()).thenReturn(testUser.getEmail());

        // Mock service to return the test user
        when(userService.getUserByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(userService.getUserById(testUser.getId())).thenReturn(Optional.of(testUser));

        // Call the controller method
        ResponseEntity<UserDTO> response = userController.getUserById(testUser.getId());

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testUser.getId(), response.getBody().getId());

        // Verify the services were called correctly
        verify(userService).getUserByEmail(testUser.getEmail());
        verify(userService).getUserById(testUser.getId());
    }

    @Test
    void getUserById_shouldReturnForbidden_whenUserIsNotAuthorized() {
        // Create another client user
        UserDTO otherUser = new UserDTO();
        otherUser.setId(3L);
        otherUser.setEmail("other@example.com");
        otherUser.setRole(Role.CLIENT);

        // Set up the authentication to return the test user's email
        when(authentication.getName()).thenReturn(testUser.getEmail());

        // Mock service to return the test user
        when(userService.getUserByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        // Call the controller method - trying to access another user's profile
        ResponseEntity<UserDTO> response = userController.getUserById(otherUser.getId());

        // Verify the response is forbidden
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        // Verify the service was called correctly
        verify(userService).getUserByEmail(testUser.getEmail());
        // getUserById should not be called since authorization check fails
        verify(userService, never()).getUserById(otherUser.getId());
    }

    @Test
    void getAllUsers_shouldReturnAllUsers() {
        // Create list of users
        List<UserDTO> users = new ArrayList<>();
        users.add(testUser);
        users.add(adminUser);

        // Mock service to return all users
        when(userService.getAllUsers()).thenReturn(users);

        // Call the controller method
        ResponseEntity<List<UserDTO>> response = userController.getAllUsers();

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertTrue(response.getBody().contains(testUser));
        assertTrue(response.getBody().contains(adminUser));

        // Verify the service was called correctly
        verify(userService).getAllUsers();
    }

    @Test
    void getUsersByRole_shouldReturnUsersWithRole() {
        // Create list of users with CLIENT role
        List<UserDTO> clientUsers = new ArrayList<>();
        clientUsers.add(testUser);

        // Mock service to return users by role
        when(userService.getUsersByRole(Role.CLIENT)).thenReturn(clientUsers);

        // Call the controller method
        ResponseEntity<List<UserDTO>> response = userController.getUsersByRole(Role.CLIENT);

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertTrue(response.getBody().contains(testUser));

        // Verify the service was called correctly
        verify(userService).getUsersByRole(Role.CLIENT);
    }

    @Test
    void updateUser_shouldUpdateUser_whenUserIsAdmin() {
        // Set up the authentication to return the admin user's email
        when(authentication.getName()).thenReturn(adminUser.getEmail());

        // Mock service to return users
        when(userService.getUserByEmail(adminUser.getEmail())).thenReturn(Optional.of(adminUser));

        // Create updated user DTO
        UserDTO updatedUser = new UserDTO();
        updatedUser.setFirstName("Updated");
        updatedUser.setLastName("Name");
        updatedUser.setRole(Role.ADMIN); // Role change should be allowed for admin

        // Mock service to return updated user
        when(userService.updateUser(testUser.getId(), updatedUser)).thenReturn(updatedUser);

        // Call the controller method
        ResponseEntity<UserDTO> response = userController.updateUser(testUser.getId(), updatedUser);

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated", response.getBody().getFirstName());
        assertEquals("Name", response.getBody().getLastName());
        assertEquals(Role.ADMIN, response.getBody().getRole());

        // Verify the services were called correctly
        verify(userService).getUserByEmail(adminUser.getEmail());
        verify(userService).updateUser(testUser.getId(), updatedUser);
    }

    @Test
    void updateUser_shouldUpdateUser_whenUserIsUpdatingOwnProfile() {
        // Set up the authentication to return the test user's email
        when(authentication.getName()).thenReturn(testUser.getEmail());

        // Mock service to return the test user
        when(userService.getUserByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        // Create updated user DTO
        UserDTO updatedUser = new UserDTO();
        updatedUser.setFirstName("Updated");
        updatedUser.setLastName("Name");
        updatedUser.setRole(Role.ADMIN); // Role change should be ignored for non-admin

        // Create expected result (with role set to null since regular users can't change roles)
        UserDTO expectedResult = new UserDTO();
        expectedResult.setFirstName("Updated");
        expectedResult.setLastName("Name");
        expectedResult.setRole(Role.CLIENT); // Original role maintained

        // Mock service to return expected result
        when(userService.updateUser(eq(testUser.getId()), any(UserDTO.class))).thenReturn(expectedResult);

        // Call the controller method
        ResponseEntity<UserDTO> response = userController.updateUser(testUser.getId(), updatedUser);

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated", response.getBody().getFirstName());
        assertEquals("Name", response.getBody().getLastName());
        assertEquals(Role.CLIENT, response.getBody().getRole());

        // Verify the service was called correctly
        verify(userService).getUserByEmail(testUser.getEmail());
        // Check that the role is set to null in the DTO passed to updateUser
        verify(userService).updateUser(eq(testUser.getId()), argThat(dto -> dto.getRole() == null));
    }

    @Test
    void updateUser_shouldReturnForbidden_whenUserIsNotAuthorized() {
        // Create another client user
        UserDTO otherUser = new UserDTO();
        otherUser.setId(3L);
        otherUser.setEmail("other@example.com");
        otherUser.setRole(Role.CLIENT);

        // Set up the authentication to return the test user's email
        when(authentication.getName()).thenReturn(testUser.getEmail());

        // Mock service to return the test user
        when(userService.getUserByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        // Create updated user DTO
        UserDTO updatedUser = new UserDTO();
        updatedUser.setFirstName("Updated");
        updatedUser.setLastName("Name");

        // Call the controller method - trying to update another user's profile
        ResponseEntity<UserDTO> response = userController.updateUser(otherUser.getId(), updatedUser);

        // Verify the response is forbidden
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        // Verify the service was called correctly
        verify(userService).getUserByEmail(testUser.getEmail());
        // updateUser should not be called since authorization check fails
        verify(userService, never()).updateUser(eq(otherUser.getId()), any(UserDTO.class));
    }

    @Test
    void changePassword_shouldChangePassword_whenCurrentPasswordIsCorrect() {
        // Set up the authentication to return the test user's email
        when(authentication.getName()).thenReturn(testUser.getEmail());

        // Mock service to return the test user
        when(userService.getUserByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        // Create password change request
        PasswordChangeRequest request = new PasswordChangeRequest();
        request.setCurrentPassword("currentPassword");
        request.setNewPassword("newPassword");

        // Mock service to return successful password change
        when(userService.changePassword(testUser.getId(), "currentPassword", "newPassword")).thenReturn(true);

        // Call the controller method
        ResponseEntity<?> response = userController.changePassword(testUser.getId(), request);

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Verify the services were called correctly
        verify(userService).getUserByEmail(testUser.getEmail());
        verify(userService).changePassword(testUser.getId(), "currentPassword", "newPassword");
    }

    @Test
    void changePassword_shouldReturnBadRequest_whenCurrentPasswordIsIncorrect() {
        // Set up the authentication to return the test user's email
        when(authentication.getName()).thenReturn(testUser.getEmail());

        // Mock service to return the test user
        when(userService.getUserByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        // Create password change request
        PasswordChangeRequest request = new PasswordChangeRequest();
        request.setCurrentPassword("wrongPassword");
        request.setNewPassword("newPassword");

        // Mock service to return failed password change
        when(userService.changePassword(testUser.getId(), "wrongPassword", "newPassword")).thenReturn(false);

        // Call the controller method
        ResponseEntity<?> response = userController.changePassword(testUser.getId(), request);

        // Verify the response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Current password is incorrect", response.getBody());

        // Verify the services were called correctly
        verify(userService).getUserByEmail(testUser.getEmail());
        verify(userService).changePassword(testUser.getId(), "wrongPassword", "newPassword");
    }

    @Test
    void changePassword_shouldReturnForbidden_whenUserIsNotAuthorized() {
        // Create another client user
        UserDTO otherUser = new UserDTO();
        otherUser.setId(3L);
        otherUser.setEmail("other@example.com");
        otherUser.setRole(Role.CLIENT);

        // Set up the authentication to return the test user's email
        when(authentication.getName()).thenReturn(testUser.getEmail());

        // Mock service to return the test user
        when(userService.getUserByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        // Create password change request
        PasswordChangeRequest request = new PasswordChangeRequest();
        request.setCurrentPassword("currentPassword");
        request.setNewPassword("newPassword");

        // Call the controller method - trying to change another user's password
        ResponseEntity<?> response = userController.changePassword(otherUser.getId(), request);

        // Verify the response is forbidden
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        // Verify the service was called correctly
        verify(userService).getUserByEmail(testUser.getEmail());
        // changePassword should not be called since authorization check fails
        verify(userService, never()).changePassword(eq(otherUser.getId()), anyString(), anyString());
    }

    @Test
    void deleteUser_shouldDeleteUser() {
        // Mock service to return successful deletion
        when(userService.deleteUser(testUser.getId())).thenReturn(true);

        // Call the controller method
        ResponseEntity<?> response = userController.deleteUser(testUser.getId());

        // Verify the response
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        // Verify the service was called correctly
        verify(userService).deleteUser(testUser.getId());
    }

    @Test
    void deleteUser_shouldReturnNotFound_whenUserDoesNotExist() {
        // Mock service to return failed deletion
        when(userService.deleteUser(999L)).thenReturn(false);

        // Call the controller method
        ResponseEntity<?> response = userController.deleteUser(999L);

        // Verify the response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        // Verify the service was called correctly
        verify(userService).deleteUser(999L);
    }
}