package com.mabjaiainn.proj.service;

import com.mabjaiainn.proj.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class GoogleAuthServiceTest {

    @Test
    void buildsAuthorizationUrlWithConfiguredCallbackAndState() throws Exception {
        GoogleAuthService service = new GoogleAuthService(
                "web-client-id",
                "client-secret",
                "",
                "http://localhost:5173/backend/auth/google/callback",
                mock(ClientService.class),
                mock(AuthService.class),
                mock(AppUserRepository.class));

        String authorizationUrl = service.authorizationUrl("random-state", "random-nonce");

        assertTrue(authorizationUrl.startsWith("https://accounts.google.com/o/oauth2/v2/auth?"));
        assertTrue(authorizationUrl.contains("client_id=web-client-id"));
        assertTrue(authorizationUrl.contains("redirect_uri=http%3A%2F%2Flocalhost%3A5173%2Fbackend%2Fauth%2Fgoogle%2Fcallback"));
        assertTrue(authorizationUrl.contains("state=random-state"));
        assertTrue(authorizationUrl.contains("nonce=random-nonce"));
        assertTrue(authorizationUrl.contains("response_type=code"));
    }
}
