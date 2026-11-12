package com.fenglin.springboottest.web;

import com.fenglin.springboottest.common.BizException;
import com.fenglin.springboottest.dto.MessageVO;
import com.fenglin.springboottest.dto.game.RoomStateMessage;
import com.fenglin.springboottest.service.SocialService;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

/**
 * 私信实时消息入口（WebSocket）。
 *
 * <p>前端连接 {@code /ws} 后订阅 {@code /user/queue/chat}，即可收到别人发给自己的私信；
 * 发送消息通过 {@code /app/chat/send}，服务端落库后点对点推送给接收方。</p>
 *
 * <p>用户身份取自 {@link WebSocketAuthInterceptor} 写入 session 的 {@code userId}。</p>
 */
@Controller
public class ChatWebSocketController {

    private final SocialService socialService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatWebSocketController(SocialService socialService, SimpMessagingTemplate messagingTemplate) {
        this.socialService = socialService;
        this.messagingTemplate = messagingTemplate;
    }

    /** 发送私信：{ receiverId, content } */
    @MessageMapping("/chat/send")
    public void send(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor accessor) {
        Long senderId = userId(accessor);
        Long receiverId = toLong(payload == null ? null : payload.get("receiverId"));
        String content = payload == null ? null : String.valueOf(payload.get("content"));
        socialService.sendMessage(senderId, content, receiverId);
    }

    /** 私信处理出错时，回发给当前用户（订阅 /user/queue/error） */
    @MessageExceptionHandler
    public void handleException(BizException e, SimpMessageHeaderAccessor accessor) {
        RoomStateMessage msg = new RoomStateMessage();
        msg.setType("ERROR");
        msg.setMessage(e.getMessage());
        if (accessor.getUser() != null) {
            messagingTemplate.convertAndSendToUser(accessor.getUser().getName(), "/queue/error", msg);
        }
    }

    private Long userId(SimpMessageHeaderAccessor accessor) {
        Object v = accessor.getSessionAttributes() == null ? null : accessor.getSessionAttributes().get("userId");
        if (v == null) throw new BizException(401, "未登录，无法发送私信");
        return (Long) v;
    }

    private Long toLong(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.longValue();
        try {
            return Long.valueOf(String.valueOf(o));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
