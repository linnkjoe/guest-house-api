package com.mabjaiainn.proj.service;

import com.mabjaiainn.proj.DTO.CreateUserRequest;
import com.mabjaiainn.proj.DTO.CreateUserResponse;
import com.mabjaiainn.proj.DTO.ChangePasswordRequest;
import com.mabjaiainn.proj.DTO.UserResponseDTO;
import com.mabjaiainn.proj.entity.AppUser;
import com.mabjaiainn.proj.entity.Role;
import com.mabjaiainn.proj.exception.BusinessException;
import com.mabjaiainn.proj.repository.AppUserRepository;
import com.mabjaiainn.proj.security.TeamUsername;
import java.util.List;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponseDTO> listUsers() {
        return appUserRepository.findAll().stream().map(this::toDto).toList();
    }

    public CreateUserResponse createStaffUser(CreateUserRequest request) throws BusinessException {
        if (request.role() == Role.CLIENTE) {
            throw new BusinessException("Client accounts are created through the clients endpoint");
        }
        String username = request.username().trim().toLowerCase(Locale.ROOT);
        if (!TeamUsername.isValid(username)) {
            throw new BusinessException("O utilizador de Admin/Gestor deve terminar com @mbj.inn");
        }
        if (appUserRepository.existsByUsername(username)) {
            throw new BusinessException("Username already in use");
        }
        String temporaryPassword = null;
        String password;
        if (request.role() == Role.GESTOR) {
            temporaryPassword = TemporaryPasswordGenerator.generate();
            password = temporaryPassword;
        } else {
            if (request.password() == null || request.password().isBlank()) {
                throw new BusinessException("Password is required for administrator accounts");
            }
            if (!request.password().equals(request.confirmPassword())) {
                throw new BusinessException("Passwords do not match");
            }
            password = request.password();
        }
        String name = request.name() == null || request.name().isBlank() ? username : request.name().trim();
        AppUser user = new AppUser(username, passwordEncoder.encode(password), name, request.role(), null);
        user.setMustChangePassword(request.role() == Role.GESTOR);
        AppUser saved = appUserRepository.save(user);
        return new CreateUserResponse(saved.getUserId(), saved.getUsername(), saved.getDisplayName(),
                saved.getRole(), saved.isEnabled(), temporaryPassword);
    }

    @Transactional
    public UserResponseDTO setManagerEnabled(Long userId, boolean enabled) throws BusinessException {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));
        if (user.getRole() != Role.GESTOR) {
            throw new BusinessException("Only manager accounts can be deactivated");
        }
        user.setEnabled(enabled);
        return toDto(user);
    }

    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) throws BusinessException {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("User not found"));
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BusinessException("Current password is incorrect");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new BusinessException("New password must be different from the current password");
        }
        String newHash = passwordEncoder.encode(request.newPassword());
        user.setPasswordHash(newHash);
        user.setMustChangePassword(false);
        appUserRepository.save(user);
        if (user.getClient() != null) {
            user.getClient().setClientPassword(newHash);
        }
    }

    private UserResponseDTO toDto(AppUser user) {
        return new UserResponseDTO(user.getUserId(), user.getUsername(), user.getDisplayName(),
                user.getRole(), user.isEnabled());
    }

}
