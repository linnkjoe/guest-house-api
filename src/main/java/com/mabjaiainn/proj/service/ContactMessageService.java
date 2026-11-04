package com.mabjaiainn.proj.service;

import com.mabjaiainn.proj.DTO.ContactMessageRequest;
import com.mabjaiainn.proj.DTO.ContactMessageReplyResponse;
import com.mabjaiainn.proj.DTO.ContactMessageResponse;
import com.mabjaiainn.proj.DTO.ContactReplyRequest;
import com.mabjaiainn.proj.entity.Client;
import com.mabjaiainn.proj.entity.ContactMessage;
import com.mabjaiainn.proj.entity.ContactMessageReply;
import com.mabjaiainn.proj.entity.Role;
import com.mabjaiainn.proj.exception.BusinessException;
import com.mabjaiainn.proj.exception.SourceNotFound;
import com.mabjaiainn.proj.repository.ClientRepository;
import com.mabjaiainn.proj.repository.ContactMessageRepository;
import com.mabjaiainn.proj.repository.ContactMessageReplyRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContactMessageService {

    private final ContactMessageRepository messageRepository;
    private final ContactMessageReplyRepository replyRepository;
    private final ClientRepository clientRepository;

    public ContactMessageService(ContactMessageRepository messageRepository,
            ContactMessageReplyRepository replyRepository, ClientRepository clientRepository) {
        this.messageRepository = messageRepository;
        this.replyRepository = replyRepository;
        this.clientRepository = clientRepository;
    }

    @Transactional(readOnly = true)
    public List<ContactMessageResponse> listForStaff() {
        return toResponses(messageRepository.findAllByOrderByCreatedAtDesc());
    }

    @Transactional(readOnly = true)
    public List<ContactMessageResponse> listForClient(Long clientId) {
        return toResponses(messageRepository.findByClient_ClientIdOrderByCreatedAtDesc(clientId));
    }

    @Transactional
    public ContactMessageResponse create(Long clientId, ContactMessageRequest request, String username) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new SourceNotFound("Client not found"));
        ContactMessage message = new ContactMessage();
        message.setClient(client);
        message.setSenderUsername(username);
        message.setSenderRole(Role.CLIENTE);
        message.setSubject(request.subject().trim());
        message.setMessageText(request.message().trim());
        message.setCreatedAt(LocalDateTime.now());
        return toResponses(List.of(messageRepository.save(message))).getFirst();
    }

    @Transactional
    public ContactMessageResponse createByStaff(Long clientId, ContactMessageRequest request,
            String username, Role senderRole) {
        if (senderRole != Role.ADMIN && senderRole != Role.GESTOR) {
            throw new org.springframework.security.access.AccessDeniedException("Staff account required");
        }
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new SourceNotFound("Client not found"));
        ContactMessage message = new ContactMessage();
        message.setClient(client);
        message.setSenderUsername(username);
        message.setSenderRole(senderRole);
        message.setSubject(request.subject().trim());
        message.setMessageText(request.message().trim());
        message.setCreatedAt(LocalDateTime.now());
        return toResponses(List.of(messageRepository.save(message))).getFirst();
    }

    @Transactional
    public ContactMessageResponse reply(Long messageId, ContactReplyRequest request, String username,
            Role senderRole, Long clientId)
            throws BusinessException {
        ContactMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new SourceNotFound("Contact message not found"));
        if (senderRole == Role.CLIENTE
                && !message.getClient().getClientId().equals(clientId)) {
            throw new org.springframework.security.access.AccessDeniedException("Client does not own this conversation");
        }
        if (senderRole != Role.CLIENTE && senderRole != Role.ADMIN && senderRole != Role.GESTOR) {
            throw new org.springframework.security.access.AccessDeniedException("Unsupported conversation participant");
        }
        LocalDateTime now = LocalDateTime.now();
        ContactMessageReply reply = new ContactMessageReply();
        reply.setMessage(message);
        reply.setSenderUsername(username);
        reply.setSenderRole(senderRole);
        reply.setReplyText(request.reply().trim());
        reply.setCreatedAt(now);
        replyRepository.save(reply);

        message.setReplyText(reply.getReplyText());
        message.setRepliedBy(username);
        message.setRepliedAt(now);
        messageRepository.save(message);
        return toResponses(List.of(message)).getFirst();
    }

    private List<ContactMessageResponse> toResponses(List<ContactMessage> messages) {
        if (messages.isEmpty()) return List.of();
        List<Long> messageIds = messages.stream().map(ContactMessage::getMessageId).toList();
        Map<Long, List<ContactMessageReply>> repliesByMessage = new HashMap<>();
        for (ContactMessageReply reply : replyRepository
                .findByMessage_MessageIdInOrderByCreatedAtAscReplyIdAsc(messageIds)) {
            repliesByMessage.computeIfAbsent(reply.getMessage().getMessageId(), ignored -> new ArrayList<>()).add(reply);
        }
        return messages.stream()
                .map(message -> toResponse(message, repliesByMessage.getOrDefault(message.getMessageId(), List.of())))
                .sorted(Comparator.comparing(ContactMessageResponse::lastActivityAt).reversed())
                .toList();
    }

    private ContactMessageResponse toResponse(ContactMessage message, List<ContactMessageReply> replies) {
        Client client = message.getClient();
        ContactMessageReply latestReply = replies.isEmpty() ? null : replies.getLast();
        return new ContactMessageResponse(
                message.getMessageId(),
                client.getClientId(),
                (client.getClientFirstName() + " " + client.getClientLastName()).trim(),
                client.getClientEmail(),
                message.getSenderUsername(),
                message.getSenderRole().name(),
                message.getSubject(),
                message.getMessageText(),
                message.getCreatedAt(),
                latestReply == null ? message.getReplyText() : latestReply.getReplyText(),
                latestReply == null ? message.getRepliedBy() : latestReply.getSenderUsername(),
                latestReply == null ? message.getRepliedAt() : latestReply.getCreatedAt(),
                replies.stream().map(reply -> new ContactMessageReplyResponse(reply.getReplyId(),
                        reply.getSenderUsername(), reply.getSenderRole().name(), reply.getReplyText(),
                        reply.getCreatedAt())).toList(),
                latestReply == null ? message.getCreatedAt() : latestReply.getCreatedAt(),
                latestReply == null ? message.getSenderRole().name() : latestReply.getSenderRole().name(),
                latestReply == null ? message.getMessageId() : latestReply.getReplyId());
    }
}
