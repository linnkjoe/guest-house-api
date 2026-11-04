package com.mabjaiainn.proj.controller;

import com.mabjaiainn.proj.DTO.*;
import com.mabjaiainn.proj.exception.BusinessException;
import com.mabjaiainn.proj.entity.PaymentMethod;
import com.mabjaiainn.proj.service.CustomPackService;
import com.mabjaiainn.proj.service.PricingService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/** Estadias personalizadas: calculo de preco, historico do cliente e reserva a partir dele. */
@RestController
public class CustomPackController {

    private static final String OWNER_OR_STAFF =
            "hasAnyRole('ADMIN','GESTOR') or (hasRole('CLIENTE') and @guard.isClient(#clientId))";

    private final CustomPackService customPackService;
    private final PricingService pricingService;

    public CustomPackController(CustomPackService customPackService, PricingService pricingService) {
        this.customPackService = customPackService;
        this.pricingService = pricingService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','GESTOR','CLIENTE')")
    @PostMapping("/custom-packs/quote")
    public QuoteResponse quote(@Valid @RequestBody QuoteRequest request) throws BusinessException {
        return pricingService.quote(request);
    }

    @PreAuthorize(OWNER_OR_STAFF)
    @GetMapping("/clients/{clientId}/custom-packs")
    public List<CustomPackResponse> history(@PathVariable Long clientId) throws BusinessException {
        return customPackService.list(clientId);
    }

    @PreAuthorize(OWNER_OR_STAFF)
    @PostMapping("/clients/{clientId}/custom-packs")
    public ResponseEntity<CustomPackResponse> create(@PathVariable Long clientId,
            @Valid @RequestBody CustomPackRequest request) throws BusinessException {
        return ResponseEntity.status(HttpStatus.CREATED).body(customPackService.create(clientId, request));
    }

    @PreAuthorize(OWNER_OR_STAFF)
    @PutMapping("/clients/{clientId}/custom-packs/{customId}")
    public CustomPackResponse update(@PathVariable Long clientId, @PathVariable Long customId,
            @Valid @RequestBody CustomPackRequest request) throws BusinessException {
        return customPackService.update(clientId, customId, request);
    }

    @PreAuthorize(OWNER_OR_STAFF)
    @DeleteMapping("/clients/{clientId}/custom-packs/{customId}")
    public ResponseEntity<Void> delete(@PathVariable Long clientId, @PathVariable Long customId) {
        customPackService.delete(clientId, customId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize(OWNER_OR_STAFF)
    @PostMapping("/booking/{clientId}/custom/{customId}")
    public BookingResponseDTO book(@PathVariable Long clientId, @PathVariable Long customId,
            @Valid @RequestBody CustomBookingRequest request,
            Authentication authentication) throws BusinessException {
        boolean clientRequest = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_CLIENTE"));
        PaymentMethod paymentMethod = clientRequest ? request.paymentMethod() : null;
        if (clientRequest && paymentMethod == null) {
            throw new BusinessException("Payment method is required for client bookings");
        }
        return customPackService.book(clientId, customId, request.bookingEnter(), paymentMethod);
    }
}
