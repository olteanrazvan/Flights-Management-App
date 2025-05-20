package ps.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ps.backend.dto.AuthRequestDTO;
import ps.backend.dto.AuthResponseDTO;
import ps.backend.dto.RegisterRequestDTO;
import ps.backend.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Register a new user
     *
     * @param request Registration request with user details
     * @return Authentication response with JWT tokens
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@RequestBody RegisterRequestDTO request) {
        try {
            AuthResponseDTO response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            // Handle cases where registration fails (e.g., email already exists)
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Authenticate a user
     *
     * @param request Authentication request with email and password
     * @return Authentication response with JWT tokens
     */
    @PostMapping("/authenticate")
    public ResponseEntity<AuthResponseDTO> authenticate(@RequestBody AuthRequestDTO request) {
        try {
            AuthResponseDTO response = authService.authenticate(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // Handle cases where authentication fails (e.g., invalid credentials)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Refresh an authentication token
     *
     * @param request Object containing refresh token
     * @return Authentication response with new JWT tokens
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponseDTO> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            AuthResponseDTO response = authService.refreshToken(request.getRefreshToken());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // Handle cases where token refresh fails (e.g., invalid refresh token)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Simple DTO for refresh token requests
     */
    static class RefreshTokenRequest {
        private String refreshToken;

        // Default constructor
        public RefreshTokenRequest() {}

        // Getters and setters
        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }
    }
}