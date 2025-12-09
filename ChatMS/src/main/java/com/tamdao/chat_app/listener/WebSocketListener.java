package com.tamdao.chat_app.listener;

import com.tamdao.chat_app.model.ChatMessage;
import com.tamdao.chat_app.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;

@Slf4j
@Component
public class WebSocketListener {

    @Autowired
    private UserService userService;

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        log.info("connected to websocket");
    }

    public void handleWebsocketDisconnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor  = StompHeaderAccessor.wrap(event.getMessage());
        String username = headerAccessor.getSessionAttributes().get("username").toString();
        if (username != null) {
            userService.setUserOnlineStatus(username, false);

            log.info("User {} disconnected from WebSocket", username);

            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setType(ChatMessage.MessageType.LEAVE);
            chatMessage.setSender(username);

            messagingTemplate.convertAndSend("/topic/public", chatMessage);
        }
    }
}
