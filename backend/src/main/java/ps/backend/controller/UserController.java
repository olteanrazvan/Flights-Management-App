package ps.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import ps.backend.dto.UserDTO;
import ps.backend.model.Role;
import ps.backend.model.User;
import ps.backend.service.UserService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Get the current authenticated user
     *
     * @return The authenticated user
     */
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Optional<UserDTO> userOpt = userService.getUserByEmail(email);

        return userOpt
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get user by ID (admin or self only)
     *
     * @param id User ID
     * @return The user if found and authorized
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Optional<UserDTO> currentUserOpt = userService.getUserByEmail(email);

        if (currentUserOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        UserDTO currentUser = currentUserOpt.get();

        // Check if user is accessing their own profile or is an admin
        if (!currentUser.getId().equals(id) && currentUser.getRole() != Role.ADMIN) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        Optional<UserDTO> userOpt = userService.getUserById(id);

        return userOpt
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all users (admin only)
     *
     * @return List of all users
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Get users by role (admin only)
     *
     * @param role User role
     * @return List of users with the specified role
     */
    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getUsersByRole(@PathVariable Role role) {
        List<UserDTO> users = userService.getUsersByRole(role);
        return ResponseEntity.ok(users);
    }

    /**
     * Update user details (admin or self only)
     *
     * @param id User ID
     * @param userDTO DTO containing updated user details
     * @return The updated user
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Optional<UserDTO> currentUserOpt = userService.getUserByEmail(email);

        if (currentUserOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        UserDTO currentUser = currentUserOpt.get();

        // Check if user is updating their own profile or is an admin
        if (!currentUser.getId().equals(id) && currentUser.getRole() != Role.ADMIN) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        // Only allow role changes for admins
        if (currentUser.getRole() != Role.ADMIN) {
            userDTO.setRole(null); // Remove role from request to prevent changes
        }

        UserDTO updatedUser = userService.updateUser(id, userDTO);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Change user password (self only)
     *
     * @param id User ID
     * @param request Object containing current and new passwords
     * @return Success or failure response
     */
    @PostMapping("/{id}/change-password")
    public ResponseEntity<?> changePassword(
            @PathVariable Long id,
            @RequestBody PasswordChangeRequest request) {

        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Optional<UserDTO> currentUserOpt = userService.getUserByEmail(email);

        if (currentUserOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        UserDTO currentUser = currentUserOpt.get();

        // Only allow users to change their own password
        if (!currentUser.getId().equals(id)) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        boolean changed = userService.changePassword(id, request.getCurrentPassword(), request.getNewPassword());

        if (changed) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().body("Current password is incorrect");
        }
    }

    /**
     * Delete a user (admin only)
     *
     * @param id User ID
     * @return Success or failure response
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        boolean deleted = userService.deleteUser(id);

        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Simple DTO for password change requests
     */
    static class PasswordChangeRequest {
        private String currentPassword;
        private String newPassword;

        // Default constructor
        public PasswordChangeRequest() {}

        // Getters and setters
        public String getCurrentPassword() {
            return currentPassword;
        }

        public void setCurrentPassword(String currentPassword) {
            this.currentPassword = currentPassword;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }
}