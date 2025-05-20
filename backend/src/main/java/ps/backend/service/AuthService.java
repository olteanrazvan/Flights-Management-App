package ps.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import ps.backend.dto.AuthRequestDTO;
import ps.backend.dto.AuthResponseDTO;
import ps.backend.dto.RegisterRequestDTO;
import ps.backend.model.Role;
import ps.backend.model.User;
import ps.backend.repository.UserRepository;
import ps.backend.security.JwtService;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Register a new user
     *
     * @param request Registration request containing user details
     * @return Authentication response with JWT tokens
     */
    public AuthResponseDTO register(RegisterRequestDTO request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        // Create new user
        User user = new User(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getFirstName(),
                request.getLastName(),
                request.getPhoneNumber(),
                Role.CLIENT // Default role for new registrations
        );

        // Save user
        user = userRepository.save(user);

        // Generate tokens
        String jwtToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Create authentication response
        return new AuthResponseDTO(
                jwtToken,
                refreshToken,
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole()
        );
    }

    /**
     * Authenticate a user
     *
     * @param request Authentication request containing email and password
     * @return Authentication response with JWT tokens
     */
    public AuthResponseDTO authenticate(AuthRequestDTO request) {
        // Authenticate user
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate tokens
        String jwtToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Create authentication response
        return new AuthResponseDTO(
                jwtToken,
                refreshToken,
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole()
        );
    }

    /**
     * Refresh a JWT token
     *
     * @param refreshToken The refresh token
     * @return Authentication response with new JWT tokens
     */
    public AuthResponseDTO refreshToken(String refreshToken) {
        // Extract username from refresh token
        String userEmail = jwtService.extractUsername(refreshToken);

        if (userEmail != null) {
            // Find user by email
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Validate refresh token
            if (jwtService.isTokenValid(refreshToken, user)) {
                // Generate new tokens
                String jwtToken = jwtService.generateToken(user);
                String newRefreshToken = jwtService.generateRefreshToken(user);

                // Create authentication response
                return new AuthResponseDTO(
                        jwtToken,
                        newRefreshToken,
                        user.getId(),
                        user.getEmail(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getRole()
                );
            }
        }

        throw new RuntimeException("Invalid refresh token");
    }
}