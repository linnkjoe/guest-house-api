package com.mabjaiainn.proj.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "contact_message")
public class ContactMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "contact_message_seq")
    @SequenceGenerator(name = "contact_message_seq", sequenceName = "CONTACT_MESSAGE_SEQ", allocationSize = 1)
    private Long messageId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(name = "sender_username", nullable = false, length = 100)
    private String senderUsername;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_role", nullable = false, length = 20)
    private Role senderRole;

    @Column(name = "subject", nullable = false, length = 120)
    private String subject;

    @Column(name = "message_text", nullable = false, length = 2000)
    private String messageText;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "reply_text", length = 2000)
    private String replyText;

    @Column(name = "replied_by", length = 100)
    private String repliedBy;

    @Column(name = "replied_at")
    private LocalDateTime repliedAt;

    public Long getMessageId() { return messageId; }
    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
    public String getSenderUsername() { return senderUsername; }
    public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }
    public Role getSenderRole() { return senderRole; }
    public void setSenderRole(Role senderRole) { this.senderRole = senderRole; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getMessageText() { return messageText; }
    public void setMessageText(String messageText) { this.messageText = messageText; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getReplyText() { return replyText; }
    public void setReplyText(String replyText) { this.replyText = replyText; }
    public String getRepliedBy() { return repliedBy; }
    public void setRepliedBy(String repliedBy) { this.repliedBy = repliedBy; }
    public LocalDateTime getRepliedAt() { return repliedAt; }
    public void setRepliedAt(LocalDateTime repliedAt) { this.repliedAt = repliedAt; }
}
