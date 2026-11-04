package com.mabjaiainn.proj.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "contact_message_reply")
public class ContactMessageReply {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "contact_message_reply_seq")
    @SequenceGenerator(name = "contact_message_reply_seq", sequenceName = "CONTACT_MESSAGE_REPLY_SEQ", allocationSize = 1)
    private Long replyId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "message_id", nullable = false)
    private ContactMessage message;

    @Column(name = "sender_username", nullable = false, length = 100)
    private String senderUsername;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_role", nullable = false, length = 20)
    private Role senderRole;

    @Column(name = "reply_text", nullable = false, length = 2000)
    private String replyText;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Long getReplyId() { return replyId; }
    public ContactMessage getMessage() { return message; }
    public void setMessage(ContactMessage message) { this.message = message; }
    public String getSenderUsername() { return senderUsername; }
    public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }
    public Role getSenderRole() { return senderRole; }
    public void setSenderRole(Role senderRole) { this.senderRole = senderRole; }
    public String getReplyText() { return replyText; }
    public void setReplyText(String replyText) { this.replyText = replyText; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
