package com.mabjaiainn.proj.DTO;

import java.time.LocalDateTime;
import java.util.List;

public record ContactMessageResponse(
        Long messageId,
        Long clientId,
        String senderName,
        String senderEmail,
        String initialSenderUsername,
        String initialSenderRole,
        String subject,
        String message,
        LocalDateTime createdAt,
        String reply,
        String repliedBy,
        LocalDateTime repliedAt,
        List<ContactMessageReplyResponse> replies,
        LocalDateTime lastActivityAt,
        String lastActivityRole,
        Long lastActivityId
) {
}
