package com.mabjaiainn.proj.controller;

import com.mabjaiainn.proj.DTO.ContactMessageRequest;
import com.mabjaiainn.proj.DTO.ContactMessageResponse;
import com.mabjaiainn.proj.DTO.ContactReplyRequest;
import com.mabjaiainn.proj.exception.BusinessException;
import com.mabjaiainn.proj.entity.Role;
import com.mabjaiainn.proj.service.ContactMessageService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ContactMessageController {

    private final ContactMessageService messageService;

    public ContactMessageController(ContactMessageService messageService) {
        this.messageService = messageService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
    @GetMapping("/messages")
    public List<ContactMessageResponse> listForStaff() {
        return messageService.listForStaff();
    }

    @PreAuthorize("hasRole('CLIENTE')")
    @GetMapping("/messages/mine")
    public List<ContactMessageResponse> listForClient(Authentication authentication) {
        return messageService.listForClient(clientId(authentication));
    }

    @PreAuthorize("hasRole('CLIENTE')")
    @PostMapping("/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public ContactMessageResponse create(@Valid @RequestBody ContactMessageRequest request,
            Authentication authentication) {
        return messageService.create(clientId(authentication), request, authentication.getName());
    }

    @PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
    @PostMapping("/messages/clients/{clientId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ContactMessageResponse createForClient(@PathVariable Long clientId,
            @Valid @RequestBody ContactMessageRequest request, Authentication authentication) {
        Role senderRole = authentication.getAuthorities().stream()
                .flatMap(authority -> java.util.Arrays.stream(Role.values())
                        .filter(role -> authority.getAuthority().equals("ROLE_" + role.name())))
                .filter(role -> role == Role.ADMIN || role == Role.GESTOR)
                .findFirst()
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Staff account required"));
        return messageService.createByStaff(clientId, request, authentication.getName(), senderRole);
    }

    @PreAuthorize("hasAnyRole('ADMIN','GESTOR','CLIENTE')")
    @PostMapping("/messages/{id}/replies")
    public ContactMessageResponse reply(@PathVariable Long id, @Valid @RequestBody ContactReplyRequest request,
            Authentication authentication) throws BusinessException {
        Role senderRole = authentication.getAuthorities().stream()
                .flatMap(authority -> java.util.Arrays.stream(Role.values())
                        .filter(role -> authority.getAuthority().equals("ROLE_" + role.name())))
                .findFirst()
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Conversation participant required"));
        Long clientId = senderRole == Role.CLIENTE ? clientId(authentication) : null;
        return messageService.reply(id, request, authentication.getName(), senderRole, clientId);
    }

    @PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
    @PutMapping("/messages/{id}/reply")
    public ContactMessageResponse replyUsingLegacyEndpoint(@PathVariable Long id,
            @Valid @RequestBody ContactReplyRequest request, Authentication authentication)
            throws BusinessException {
        Role senderRole = authentication.getAuthorities().stream()
                .flatMap(authority -> java.util.Arrays.stream(Role.values())
                        .filter(role -> authority.getAuthority().equals("ROLE_" + role.name())))
                .filter(role -> role == Role.ADMIN || role == Role.GESTOR)
                .findFirst()
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Staff account required"));
        return messageService.reply(id, request, authentication.getName(), senderRole, null);
    }

    private Long clientId(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            Object claim = jwtAuthentication.getToken().getClaim("clientId");
            if (claim instanceof Number clientId) return clientId.longValue();
        }
        throw new org.springframework.security.access.AccessDeniedException("Client account required");
    }
}
