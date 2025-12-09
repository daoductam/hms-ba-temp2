package com.tamdao.chat_app.controller;

import com.tamdao.chat_app.model.ChatMessage;
import com.tamdao.chat_app.repository.ChatMessageRepository;
import com.tamdao.chat_app.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Controller
public class ChatController {

    @Autowired
    private UserService userService;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        log.info("bat dau them chat cho user {}", chatMessage.getSender());
        if (userService.userExists(chatMessage.getSender())) {{
            log.info("user ton tai");
            // luu tru username trong session
            Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
            if (sessionAttributes != null) {
                log.info("bat dau luu tru usename tron ss");
                sessionAttributes.put("username", chatMessage.getSender());
                log.info("da luu tru usename tron ss");

            }
            log.info("Set online");
            userService.setUserOnlineStatus(chatMessage.getSender(), true);
            log.info("set online thanh cong");
            log.info("User added Successfully {} with session ID {}",
                    chatMessage.getSender(),
                    headerAccessor.getSessionId());

            chatMessage.setTimestamp(LocalDateTime.now());
            if (chatMessage.getSender()==null) {
                chatMessage.setContent("");
            }
            chatMessageRepository.save(chatMessage);
        }}
        return chatMessage;
    }

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {

        if (userService.userExists(chatMessage.getSender())) {
            if (chatMessage.getTimestamp()==null) {
                chatMessage.setTimestamp(LocalDateTime.now());
            }

            if (chatMessage.getContent()==null) {
                chatMessage.setContent("");
            }

            return chatMessageRepository.save(chatMessage);
        }
        return null;
    }

    @MessageMapping("/chat.sendPrivateMessage")
    @SendToUser("/topic/private")
    public void sendPrivateMessage(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        log.info("Bat dau gui chat private {} + {}", chatMessage.getSender(), chatMessage.getRecipient());
        if (userService.userExists(chatMessage.getSender()) && userService.userExists(chatMessage.getRecipient())) {
            log.info("ca2 ton tai");
            if (chatMessage.getTimestamp()==null) {
                chatMessage.setTimestamp(LocalDateTime.now());
            }

            if (chatMessage.getContent()==null) {
                chatMessage.setContent("");
            }

            chatMessage.setType(ChatMessage.MessageType.PRIVATE_MESSAGE);

            ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
            log.info("Message saved successfully with Id {}", savedMessage.getId());

            try {
                String recipientDestination = "/user/"+chatMessage.getRecipient()+"/queue/private";
                log.info("Sending message to recipient destination {}", recipientDestination);
                messagingTemplate.convertAndSend(recipientDestination, savedMessage);

                String senderDestination = "/user/"+chatMessage.getSender()+"/queue/private";
                log.info("Sending message to sender destination {}", recipientDestination);
                messagingTemplate.convertAndSend(senderDestination, savedMessage);
            } catch (Exception e) {
                log.error("ERROR occured while sending the message {}", e.getMessage());
                e.printStackTrace();
            }
        } else {
            log.error("ERROR: Sender {} or recipient {} does not exist",
                    chatMessage.getSender(), chatMessage.getRecipient());
        }
    }
}
