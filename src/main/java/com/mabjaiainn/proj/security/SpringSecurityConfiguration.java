package com.mabjaiainn.proj.security;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.springframework.http.HttpMethod;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import static org.springframework.security.config.Customizer.withDefaults;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import com.mabjaiainn.proj.repository.AppUserRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Autenticacao: login em POST /auth/login devolve um JWT (Bearer).
 * Autorizacao: cada endpoint declara os perfis permitidos com @PreAuthorize
 * nos controllers (ADMIN, GESTOR, CLIENTE). Aqui so se garante que tudo,
 * exceto login e Swagger, exige um token valido.
 */
@Configuration
@EnableMethodSecurity
public class SpringSecurityConfiguration {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AppUserRepository appUserRepository) throws Exception {

        http.cors(withDefaults());
        http.csrf(csrf -> csrf.disable());
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/login", "/auth/register", "/auth/email-available", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/packs/*/image", "/about", "/about/image").permitAll()
                .anyRequest().access((authentication, context) -> {
                    var currentAuthentication = authentication.get();
                    if (currentAuthentication == null || !currentAuthentication.isAuthenticated()
                            || currentAuthentication instanceof AnonymousAuthenticationToken) {
                        return new AuthorizationDecision(false);
                    }
                    if (!(currentAuthentication instanceof JwtAuthenticationToken jwtAuthentication)) {
                        return new AuthorizationDecision(true);
                    }
                    var user = appUserRepository.findByUsername(jwtAuthentication.getName()).orElse(null);
                    if (user == null || !user.isEnabled()) {
                        return new AuthorizationDecision(false);
                    }
                    boolean passwordChangeRequired = user.isMustChangePassword();
                    boolean passwordEndpoint = context.getRequest().getMethod().equals("PUT")
                            && context.getRequest().getServletPath().equals("/users/me/password");
                    return new AuthorizationDecision(!passwordChangeRequired || passwordEndpoint);
                })
        );

        http.oauth2ResourceServer(oauth -> oauth
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                .authenticationEntryPoint((request, response, ex)
                        -> writeJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                                "Autenticacao necessaria ou sessao expirada"))
                .accessDeniedHandler((request, response, ex)
                        -> writeJson(response, HttpServletResponse.SC_FORBIDDEN,
                                "Sem permissao para esta acao"))
        );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Le os perfis da claim "roles" do token e converte-os em ROLE_ADMIN, ROLE_GESTOR, ROLE_CLIENTE. */
    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName("roles");
        authorities.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins:http://localhost:5173}") String allowedOrigins) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private static void writeJson(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"timestamp\":\"" + LocalDateTime.now()
                + "\",\"message\":\"" + message + "\"}");
    }
}
