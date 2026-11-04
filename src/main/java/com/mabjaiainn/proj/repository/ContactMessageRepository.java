package com.mabjaiainn.proj.repository;

import com.mabjaiainn.proj.entity.ContactMessage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {

    List<ContactMessage> findAllByOrderByCreatedAtDesc();

    List<ContactMessage> findByClient_ClientIdOrderByCreatedAtDesc(Long clientId);
}
