package com.mabjaiainn.proj.controller;

import com.mabjaiainn.proj.DTO.CreateUserRequest;
import com.mabjaiainn.proj.DTO.CreateUserResponse;
import com.mabjaiainn.proj.DTO.ChangePasswordRequest;
import com.mabjaiainn.proj.DTO.LoginRequest;
import com.mabjaiainn.proj.DTO.LoginResponse;
import com.mabjaiainn.proj.DTO.SetUserEnabledRequest;
import com.mabjaiainn.proj.DTO.UserResponseDTO;
import com.mabjaiainn.proj.exception.BusinessException;
import com.mabjaiainn.proj.service.UserService;
import com.mabjaiainn.proj.service.AuthService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<UserResponseDTO> retrieveUsers() {
        return userService.listUsers();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CreateUserResponse> createStaffUser(@Valid @RequestBody CreateUserRequest request)
            throws BusinessException {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createStaffUser(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/enabled")
    public UserResponseDTO setManagerEnabled(@PathVariable Long id,
            @Valid @RequestBody SetUserEnabledRequest request) throws BusinessException {
        return userService.setManagerEnabled(id, request.enabled());
    }

    @PutMapping("/me/password")
    public ResponseEntity<LoginResponse> changePassword(Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) throws BusinessException {
        userService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok(authService.login(
                new LoginRequest(authentication.getName(), request.newPassword())));
    }
}
