package com.mabjaiainn.proj.service;

import com.mabjaiainn.proj.DTO.ContactMessageRequest;
import com.mabjaiainn.proj.DTO.ContactReplyRequest;
import com.mabjaiainn.proj.entity.Client;
import com.mabjaiainn.proj.entity.ContactMessage;
import com.mabjaiainn.proj.entity.ContactMessageReply;
import com.mabjaiainn.proj.entity.Role;
import com.mabjaiainn.proj.exception.BusinessException;
import com.mabjaiainn.proj.repository.ClientRepository;
import com.mabjaiainn.proj.repository.ContactMessageRepository;
import com.mabjaiainn.proj.repository.ContactMessageReplyRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContactMessageServiceTest {

    @Mock
    private ContactMessageRepository messageRepository;

    @Mock
    private ContactMessageReplyRepository replyRepository;

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ContactMessageService messageService;

    @Test
    void createsMessageForAuthenticatedClient() {
        Client client = client();
        when(clientRepository.findById(7L)).thenReturn(Optional.of(client));
        when(messageRepository.save(any(ContactMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(replyRepository.findByMessage_MessageIdInOrderByCreatedAtAscReplyIdAsc(anyCollection()))
                .thenReturn(List.of());

        var response = messageService.create(7L,
                new ContactMessageRequest("  Reserva  ", "  Preciso de ajuda.  "), "joana@example.com");

        assertEquals(7L, response.clientId());
        assertEquals("Joana Silva", response.senderName());
        assertEquals("Reserva", response.subject());
        assertEquals("Preciso de ajuda.", response.message());
        assertEquals("CLIENTE", response.initialSenderRole());
    }

    @Test
    void allowsStaffToStartConversationWithClient() {
        when(clientRepository.findById(7L)).thenReturn(Optional.of(client()));
        when(messageRepository.save(any(ContactMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(replyRepository.findByMessage_MessageIdInOrderByCreatedAtAscReplyIdAsc(anyCollection()))
                .thenReturn(List.of());

        var response = messageService.createByStaff(7L,
                new ContactMessageRequest("Checkout", "A sua estadia termina em breve."),
                "manager@mbj.inn", Role.GESTOR);

        assertEquals("GESTOR", response.initialSenderRole());
        assertEquals("manager@mbj.inn", response.initialSenderUsername());
        assertEquals("Checkout", response.subject());
    }

    @Test
    void allowsStaffAndClientToExchangeMultipleRepliesInOneSubject() throws BusinessException {
        ContactMessage message = new ContactMessage();
        message.setClient(client());
        message.setSenderUsername("joana@example.com");
        message.setSenderRole(Role.CLIENTE);
        message.setSubject("Reserva");
        message.setMessageText("Preciso de ajuda.");
        when(messageRepository.findById(2L)).thenReturn(Optional.of(message));
        when(messageRepository.save(message)).thenReturn(message);
        List<ContactMessageReply> replies = new ArrayList<>();
        when(replyRepository.save(any(ContactMessageReply.class))).thenAnswer(invocation -> {
            ContactMessageReply reply = invocation.getArgument(0);
            replies.add(reply);
            return reply;
        });
        when(replyRepository.findByMessage_MessageIdInOrderByCreatedAtAscReplyIdAsc(anyCollection()))
                .thenAnswer(invocation -> List.copyOf(replies));

        var staffResponse = messageService.reply(2L, new ContactReplyRequest(" Estamos disponíveis. "),
                "manager", Role.GESTOR, null);
        var clientResponse = messageService.reply(2L, new ContactReplyRequest("Obrigado pela ajuda."),
                "joana@example.com", Role.CLIENTE, 7L);

        assertEquals(1, staffResponse.replies().size());
        assertEquals("GESTOR", staffResponse.replies().getFirst().senderRole());
        assertEquals(2, clientResponse.replies().size());
        assertEquals("CLIENTE", clientResponse.replies().getLast().senderRole());
        assertEquals("Obrigado pela ajuda.", clientResponse.reply());
        verify(messageRepository, org.mockito.Mockito.times(2)).save(message);
    }

    @Test
    void clientCannotReplyToAnotherClientsSubject() {
        ContactMessage message = new ContactMessage();
        message.setClient(client());
        message.setSubject("Reserva");
        message.setMessageText("Preciso de ajuda.");
        when(messageRepository.findById(2L)).thenReturn(Optional.of(message));

        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> messageService.reply(2L, new ContactReplyRequest("Resposta alheia"),
                        "another@example.com", Role.CLIENTE, 8L));
    }

    private Client client() {
        Client client = new Client();
        client.setClientId(7L);
        client.setClientFirstName("Joana");
        client.setClientLastName("Silva");
        client.setClientEmail("joana@example.com");
        return client;
    }
}
