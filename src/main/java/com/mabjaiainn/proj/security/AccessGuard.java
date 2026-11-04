package com.mabjaiainn.proj.security;

import com.mabjaiainn.proj.repository.BookingRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Regras de propriedade para o perfil CLIENTE, usadas nos @PreAuthorize como
 * @guard.isClient(#id) e @guard.ownsBooking(#id).
 * O clientId vem da claim assinada do token, nunca do pedido.
 */
@Component("guard")
public class AccessGuard {

    private final BookingRepository bookingRepository;

    public AccessGuard(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    /** O utilizador autenticado e o cliente com este id. */
    public boolean isClient(Long clientId) {
        Long own = currentClientId();
        return own != null && own.equals(clientId);
    }

    /** A reserva pertence ao cliente autenticado. */
    public boolean ownsBooking(Long bookingId) {
        Long own = currentClientId();
        return own != null
                && bookingId != null
                && bookingRepository.existsByBookingIdAndClient_ClientId(bookingId, own);
    }

    private Long currentClientId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            Object claim = jwtAuthentication.getToken().getClaim("clientId");
            if (claim instanceof Number number) {
                return number.longValue();
            }
        }
        return null;
    }
}
