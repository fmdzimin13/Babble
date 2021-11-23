package com.babble.api.controller;

import com.babble.api.request.room.EmojiReq;
import com.babble.api.request.room.MessageReq;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@Slf4j
@RequiredArgsConstructor
@RestController
public class MessageController {
    private final SimpMessagingTemplate template;

    @MessageMapping("/message")
    public void sendMessage(@Payload MessageReq message) {
        System.out.println(">>>>> 전달 메시지 " + message);
        template.convertAndSend("/sub/message/" + message.getChatroomId(), message);
    }

    @MessageMapping("/emoji")
    public void sendEmoji(@Payload EmojiReq emojiReq) {
        System.out.println(">>>>> 전달 이모지 타입 " + emojiReq);
        template.convertAndSend("/sub/emoji/" + emojiReq.getRoomId(), emojiReq);
    }
}