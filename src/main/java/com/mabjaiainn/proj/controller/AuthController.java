package com.mabjaiainn.proj.controller;

import com.mabjaiainn.proj.DTO.EmailAvailability;
import com.mabjaiainn.proj.DTO.RegisterRequest;
import com.mabjaiainn.proj.entity.Client;
import com.mabjaiainn.proj.exception.BusinessException;
import com.mabjaiainn.proj.repository.AppUserRepository;
import com.mabjaiainn.proj.service.ClientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mabjaiainn.proj.DTO.LoginRequest;
import com.mabjaiainn.proj.DTO.LoginResponse;
import com.mabjaiainn.proj.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final ClientService clientService;
    private final AppUserRepository appUserRepository;

    public AuthController(AuthService authService, ClientService clientService,
            AppUserRepository appUserRepository) {
        this.authService = authService;
        this.clientService = clientService;
        this.appUserRepository = appUserRepository;
    }

    // Publico (liberado em SpringSecurityConfiguration): qualquer pessoa pode tentar autenticar-se.
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    // Publico: auto cadastro de um cliente novo. Cria o cliente + conta e entra logo.
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request)
            throws BusinessException {
        if (!request.clientPassword().equals(request.confirmClientPassword())) {
            throw new BusinessException("Passwords do not match");
        }
        Client client = new Client(request.phoneNumber(), request.clientPassword(),
                request.clientFirstName().trim(), request.clientLastName().trim(), request.clientEmail().trim());
        clientService.registerClient(client);
        LoginResponse response = authService.login(new LoginRequest(request.clientEmail(), request.clientPassword()));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Publico: permite validar o e-mail ao sair do campo, antes de enviar o formulario.
    @GetMapping("/email-available")
    public EmailAvailability emailAvailable(@RequestParam String email) {
        return new EmailAvailability(!appUserRepository.existsByUsername(email.trim().toLowerCase()));
    }
}
