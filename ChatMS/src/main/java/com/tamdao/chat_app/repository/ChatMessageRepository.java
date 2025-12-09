package com.tamdao.chat_app.repository;

import com.tamdao.chat_app.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("SELECT cm from ChatMessage cm WHERE cm.type = 'PRIVATE_MESSAGE' AND " +
            "((cm.sender = :user1 AND cm.recipient = :user2) OR (cm.sender = :user2 AND cm.recipient = :user1)) " +
            "ORDER BY cm.timestamp ASC")
    List<ChatMessage> findPrivateMessagesBetweenTwoUsers(@Param("user1") String user1, @Param("user2") String user2);
}
