package com.mabjaiainn.proj.service;

import com.mabjaiainn.proj.entity.AppUser;
import com.mabjaiainn.proj.entity.Client;
import com.mabjaiainn.proj.entity.Role;
import com.mabjaiainn.proj.repository.AppUserRepository;
import com.mabjaiainn.proj.repository.ClientRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ClientService clientService;

    @Test
    void createsClientWithTemporaryPasswordAndMandatoryFirstChange() throws Exception {
        when(appUserRepository.existsByUsername("client@example.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-hash");
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> {
            Client client = invocation.getArgument(0);
            client.setClientId(9L);
            return client;
        });
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Client client = new Client(123456789, null, "Client", "Test", "Client@example.com");

        var created = clientService.createClientWithTemporaryPassword(client);

        assertEquals(9L, created.clientId());
        assertEquals("client@example.com", created.username());
        assertNotNull(created.temporaryPassword());
        assertTrue(created.temporaryPassword().length() >= 8);
        assertEquals("encoded-hash", client.getClientPassword());
        ArgumentCaptor<AppUser> savedUser = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserRepository).save(savedUser.capture());
        assertEquals(Role.CLIENTE, savedUser.getValue().getRole());
        assertTrue(savedUser.getValue().isMustChangePassword());
    }

    @Test
    void deletingClientDisablesAccountWithoutDeletingClientHistory() throws Exception {
        Client client = new Client();
        AppUser user = new AppUser("client@example.com", "hash", "Client", Role.CLIENTE, client);
        when(clientRepository.findById(9L)).thenReturn(Optional.of(client));
        when(appUserRepository.findByClient_ClientId(9L)).thenReturn(Optional.of(user));

        clientService.deleteById(9L);

        assertFalse(user.isEnabled());
        verify(appUserRepository).save(user);
        verify(clientRepository, never()).deleteById(9L);
    }

    @Test
    void publicRegistrationKeepsThePasswordChosenByTheClient() throws Exception {
        when(appUserRepository.existsByUsername("client@example.com")).thenReturn(false);
        when(passwordEncoder.encode("ChosenPassword1!")).thenReturn("chosen-hash");
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Client client = new Client(123456789, "ChosenPassword1!", "Client", "Test", "client@example.com");

        clientService.registerClient(client);

        assertEquals("chosen-hash", client.getClientPassword());
        ArgumentCaptor<AppUser> savedUser = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserRepository).save(savedUser.capture());
        assertFalse(savedUser.getValue().isMustChangePassword());
    }
}
