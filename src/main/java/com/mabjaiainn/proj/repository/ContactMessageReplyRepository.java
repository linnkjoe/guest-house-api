package com.mabjaiainn.proj.repository;

import com.mabjaiainn.proj.entity.ContactMessageReply;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactMessageReplyRepository extends JpaRepository<ContactMessageReply, Long> {

    List<ContactMessageReply> findByMessage_MessageIdInOrderByCreatedAtAscReplyIdAsc(Collection<Long> messageIds);
}
