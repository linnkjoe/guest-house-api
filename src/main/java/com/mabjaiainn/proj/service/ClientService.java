/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mabjaiainn.proj.service;

import com.mabjaiainn.proj.DTO.ClientResponseDTO;
import com.mabjaiainn.proj.DTO.CreateClientResponse;
import com.mabjaiainn.proj.entity.AppUser;
import com.mabjaiainn.proj.entity.Role;
import com.mabjaiainn.proj.exception.BusinessException;
import com.mabjaiainn.proj.exception.SourceNotFound;
import com.mabjaiainn.proj.entity.Client;
import com.mabjaiainn.proj.repository.AppUserRepository;
import com.mabjaiainn.proj.repository.ClientRepository;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.hateoas.EntityModel;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

/**
 *
 * @author linnkjoe
 */
@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public ClientService(ClientRepository clientRepository, AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder) {
        this.clientRepository = clientRepository;
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<ClientResponseDTO> retrieveAllClients() {
        List<Client> clients = clientRepository.findAll();
        if (clients.isEmpty()) {
            return List.of();
        }
        List<Long> clientIds = clients.stream().map(Client::getClientId).toList();
        Map<Long, AppUser> usersByClientId = appUserRepository.findAllByClient_ClientIdIn(clientIds).stream()
                .collect(Collectors.toMap(user -> user.getClient().getClientId(), Function.identity()));
        return clients.stream()
                .map(client -> toResponse(client, usersByClientId.get(client.getClientId())))
                .toList();
    }

    public EntityModel<Client> retrieveClientById(Long id) {
        Optional<Client> client = clientRepository.findById(id);

        if (client.isEmpty()) {
            throw new SourceNotFound("Client Not found");
        }
        EntityModel<Client> entityModel = EntityModel.of(client.get());
        return entityModel;

    }

    @Transactional
    public void setClientEnabled(Long id, boolean enabled) throws BusinessException {
        clientRepository.findById(id).orElseThrow(() -> new SourceNotFound("Client Not Found"));
        AppUser user = appUserRepository.findByClient_ClientId(id)
                .orElseThrow(() -> new BusinessException("Client account not found"));
        user.setEnabled(enabled);
        appUserRepository.save(user);
    }

    @Transactional
    public void deleteById(Long id) throws BusinessException {
        setClientEnabled(id, false);
    }

    @Transactional
    public Client registerClient(Client client) throws BusinessException {
        return persistClientAndAccount(client, client.getClientPassword(), false);
    }

    /**
     * Cria o cliente com uma credencial temporaria que tem de ser alterada no primeiro acesso.
     */
    @Transactional
    public CreateClientResponse createClientWithTemporaryPassword(Client client) throws BusinessException {
        String temporaryPassword = TemporaryPasswordGenerator.generate();
        Client savedClient = persistClientAndAccount(client, temporaryPassword, true);
        return new CreateClientResponse(savedClient.getClientId(),
                savedClient.getClientEmail().trim().toLowerCase(Locale.ROOT), temporaryPassword);
    }

    private Client persistClientAndAccount(Client client, String password, boolean mustChangePassword)
            throws BusinessException {
        String username = client.getClientEmail().trim().toLowerCase(Locale.ROOT);
        if (appUserRepository.existsByUsername(username)) {
            throw new BusinessException("Email already registered");
        }

        String hash = passwordEncoder.encode(password);
        client.setClientId(null);
        client.setClientPassword(hash);
        Client savedClient = clientRepository.save(client);

        String name = ((savedClient.getClientFirstName() == null ? "" : savedClient.getClientFirstName()) + " "
                + (savedClient.getClientLastName() == null ? "" : savedClient.getClientLastName())).trim();
        AppUser user = new AppUser(username, hash, name.isEmpty() ? username : name,
                Role.CLIENTE, savedClient);
        user.setMustChangePassword(mustChangePassword);
        appUserRepository.save(user);

        return savedClient;
    }

    private ClientResponseDTO toResponse(Client client, AppUser user) {
        return new ClientResponseDTO(client.getClientId(), client.getPhoneNumber(), client.getClientFirstName(),
                client.getClientLastName(), client.getClientEmail(), user != null && user.isEnabled());
    }
}
