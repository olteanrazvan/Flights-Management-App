package ps.backend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ps.backend.dto.AuthRequestDTO;
import ps.backend.dto.AuthResponseDTO;
import ps.backend.dto.RegisterRequestDTO;
import ps.backend.model.Role;
import ps.backend.service.AuthService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for the AuthController
 */
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private RegisterRequestDTO registerRequest;
    private AuthRequestDTO authRequest;
    private AuthResponseDTO authResponse;
    private AuthController.RefreshTokenRequest refreshTokenRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create test register request
        registerRequest = new RegisterRequestDTO();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("Test");
        registerRequest.setLastName("User");
        registerRequest.setPhoneNumber("1234567890");

        // Create test auth request
        authRequest = new AuthRequestDTO();
        authRequest.setEmail("test@example.com");
        authRequest.setPassword("password123");

        // Create test refresh token request
        refreshTokenRequest = new AuthController.RefreshTokenRequest();
        refreshTokenRequest.setRefreshToken("refresh-token");

        // Create test auth response
        authResponse = new AuthResponseDTO();
        authResponse.setToken("jwt-token");
        authResponse.setRefreshToken("refresh-token");
        authResponse.setUserId(1L);
        authResponse.setEmail("test@example.com");
        authResponse.setFirstName("Test");
        authResponse.setLastName("User");
        authResponse.setRole(Role.CLIENT);
    }

    /**
     * Test for the register endpoint
     * This test verifies that the register method correctly handles a valid registration request
     */
    @Test
    void register_shouldRegisterUser() {
        // Mock service to return auth response
        when(authService.register(registerRequest)).thenReturn(authResponse);

        // Call the controller method
        ResponseEntity<AuthResponseDTO> response = authController.register(registerRequest);

        // Verify the response
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(authResponse.getToken(), response.getBody().getToken());
        assertEquals(authResponse.getEmail(), response.getBody().getEmail());

        // Verify the service was called correctly
        verify(authService).register(registerRequest);
    }

    /**
     * Test for the authenticate endpoint
     * This test verifies that the authenticate method correctly handles a valid authentication request
     */
    @Test
    void authenticate_shouldAuthenticateUser() {
        // Mock service to return auth response
        when(authService.authenticate(authRequest)).thenReturn(authResponse);

        // Call the controller method
        ResponseEntity<AuthResponseDTO> response = authController.authenticate(authRequest);

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(authResponse.getToken(), response.getBody().getToken());
        assertEquals(authResponse.getEmail(), response.getBody().getEmail());

        // Verify the service was called correctly
        verify(authService).authenticate(authRequest);
    }

    /**
     * Test for the refresh token endpoint
     * This test verifies that the refreshToken method correctly handles a valid refresh token request
     */
    @Test
    void refreshToken_shouldRefreshToken() {
        // Mock service to return auth response
        when(authService.refreshToken(refreshTokenRequest.getRefreshToken())).thenReturn(authResponse);

        // Call the controller method
        ResponseEntity<AuthResponseDTO> response = authController.refreshToken(refreshTokenRequest);

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(authResponse.getToken(), response.getBody().getToken());
        assertEquals(authResponse.getRefreshToken(), response.getBody().getRefreshToken());

        // Verify the service was called correctly
        verify(authService).refreshToken(refreshTokenRequest.getRefreshToken());
    }

    /**
     * Test for handling registration with an existing email
     * This test verifies that the register method correctly handles a registration request with an existing email
     */
    @Test
    void register_shouldReturn400_whenEmailAlreadyExists() {
        // Mock service to throw exception
        when(authService.register(registerRequest)).thenThrow(new RuntimeException("Email already in use"));

        // Call the controller method
        ResponseEntity<AuthResponseDTO> response = authController.register(registerRequest);

        // Verify the response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());

        // Verify the service was called correctly
        verify(authService).register(registerRequest);
    }

    /**
     * Test for handling invalid login credentials
     * This test verifies that the authenticate method correctly handles invalid credentials
     */
    @Test
    void authenticate_shouldReturn401_whenCredentialsAreInvalid() {
        // Mock service to throw exception
        when(authService.authenticate(authRequest)).thenThrow(new RuntimeException("Invalid credentials"));

        // Call the controller method
        ResponseEntity<AuthResponseDTO> response = authController.authenticate(authRequest);

        // Verify the response
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());

        // Verify the service was called correctly
        verify(authService).authenticate(authRequest);
    }

    /**
     * Test for handling invalid refresh token
     * This test verifies that the refreshToken method correctly handles an invalid refresh token
     */
    @Test
    void refreshToken_shouldReturn401_whenRefreshTokenIsInvalid() {
        // Mock service to throw exception
        when(authService.refreshToken(refreshTokenRequest.getRefreshToken())).thenThrow(new RuntimeException("Invalid refresh token"));

        // Call the controller method
        ResponseEntity<AuthResponseDTO> response = authController.refreshToken(refreshTokenRequest);

        // Verify the response
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());

        // Verify the service was called correctly
        verify(authService).refreshToken(refreshTokenRequest.getRefreshToken());
    }
}