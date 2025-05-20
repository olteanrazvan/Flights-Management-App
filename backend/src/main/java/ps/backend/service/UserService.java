package ps.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ps.backend.dto.UserDTO;
import ps.backend.model.Role;
import ps.backend.model.Ticket;
import ps.backend.model.User;
import ps.backend.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Get user by ID
     *
     * @param id User ID
     * @return User if found
     */
    public Optional<UserDTO> getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::convertToDTO);
    }

    /**
     * Get user by email
     *
     * @param email User email
     * @return User if found
     */
    public Optional<UserDTO> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::convertToDTO);
    }

    /**
     * Get all users
     *
     * @return List of all users
     */
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get users by role
     *
     * @param role User role
     * @return List of users with the specified role
     */
    public List<UserDTO> getUsersByRole(Role role) {
        return userRepository.findByRole(role).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Update user details
     *
     * @param id ID of the user to update
     * @param userDTO DTO containing updated user details
     * @return Updated user
     */
    @Transactional
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Update user details
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setPhoneNumber(userDTO.getPhoneNumber());

        // Only admins can change roles
        if (userDTO.getRole() != null) {
            user.setRole(userDTO.getRole());
        }

        // Save updated user
        user = userRepository.save(user);

        return convertToDTO(user);
    }

    /**
     * Change user password
     *
     * @param id User ID
     * @param currentPassword Current password
     * @param newPassword New password
     * @return true if password was changed successfully, false otherwise
     */
    @Transactional
    public boolean changePassword(Long id, String currentPassword, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return false;
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return true;
    }

    /**
     * Delete a user
     *
     * @param id User ID
     * @return true if user was deleted successfully, false otherwise
     */
    @Transactional
    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Convert User entity to UserDTO
     *
     * @param user User entity
     * @return UserDTO
     */
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setRole(user.getRole());

        dto.setTicketIds(user.getTickets().stream()
                .map(Ticket::getId)
                .collect(Collectors.toList()));

        return dto;
    }
}