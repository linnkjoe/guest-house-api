package com.mabjaiainn.proj.security;

import com.mabjaiainn.proj.entity.AppUser;
import com.mabjaiainn.proj.entity.Client;
import com.mabjaiainn.proj.entity.Role;
import com.mabjaiainn.proj.repository.AppUserRepository;
import com.mabjaiainn.proj.repository.ClientRepository;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * No arranque:
 * 1) cria o primeiro ADMIN se ainda nao existir nenhum;
 * 2) cria conta de acesso para clientes ja existentes que ainda nao tenham uma,
 *    convertendo para hash as palavras-passe que estavam em texto simples.
 */
@Component
public class SecurityDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SecurityDataInitializer.class);

    private final AppUserRepository appUserRepository;
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String adminPassword;

    public SecurityDataInitializer(AppUserRepository appUserRepository,
            ClientRepository clientRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.security.bootstrap-admin.username:admin@mbj.inn}") String adminUsername,
            @Value("${app.security.bootstrap-admin.password:}") String adminPassword) {
        this.appUserRepository = appUserRepository;
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        createFirstAdmin();
        createAccountsForExistingClients();
    }

    private void createFirstAdmin() {
        if (appUserRepository.existsByRole(Role.ADMIN)) {
            return;
        }
        String username = TeamUsername.normalizeBootstrapUsername(adminUsername);
        String password = adminPassword;
        boolean generated = password == null || password.isBlank();
        if (generated) {
            byte[] bytes = new byte[12];
            new SecureRandom().nextBytes(bytes);
            password = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        }
        appUserRepository.save(new AppUser(username, passwordEncoder.encode(password),
                "Administrador", Role.ADMIN, null));
        if (generated) {
            log.warn("Primeiro ADMIN criado. utilizador='{}' palavra-passe='{}' (mostrada apenas agora; "
                    + "defina BOOTSTRAP_ADMIN_PASSWORD para escolher a sua)", username, password);
        } else {
            log.info("Primeiro ADMIN criado com o utilizador '{}'", username);
        }
    }

    private void createAccountsForExistingClients() {
        Set<Long> clientsWithAccounts = new HashSet<>(appUserRepository.findClientIdsWithAccounts());
        for (Client client : clientRepository.findAll()) {
            if (clientsWithAccounts.contains(client.getClientId())) {
                continue;
            }
            String email = client.getClientEmail();
            String stored = client.getClientPassword();
            if (email == null || email.isBlank() || stored == null || stored.isBlank()) {
                log.warn("Cliente {} sem e-mail ou palavra-passe: conta nao criada", client.getClientId());
                continue;
            }
            String username = email.trim().toLowerCase();
            if (appUserRepository.existsByUsername(username)) {
                log.warn("Cliente {}: o e-mail '{}' ja esta em uso por outra conta", client.getClientId(), username);
                continue;
            }
            try {
                String hash = stored.startsWith("$2") ? stored : passwordEncoder.encode(stored);
                if (!hash.equals(stored)) {
                    client.setClientPassword(hash);
                    clientRepository.save(client);
                }
                String name = ((client.getClientFirstName() == null ? "" : client.getClientFirstName()) + " "
                        + (client.getClientLastName() == null ? "" : client.getClientLastName())).trim();
                appUserRepository.save(new AppUser(username, hash, name.isEmpty() ? username : name,
                        Role.CLIENTE, client));
            } catch (RuntimeException ex) {
                log.warn("Nao foi possivel criar conta para o cliente {}: {}", client.getClientId(), ex.getMessage());
            }
        }
    }
}
