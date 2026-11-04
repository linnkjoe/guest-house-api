package com.mabjaiainn.proj.DTO;

import java.time.LocalDateTime;

public record ContactMessageReplyResponse(
        Long replyId,
        String senderUsername,
        String senderRole,
        String message,
        LocalDateTime createdAt
) {
}
