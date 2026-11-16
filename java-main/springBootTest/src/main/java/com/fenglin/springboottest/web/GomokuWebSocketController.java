package com.fenglin.springboottest.web;

import com.fenglin.springboottest.common.BizException;
import com.fenglin.springboottest.dto.game.RoomStateMessage;
import com.fenglin.springboottest.service.GomokuRoomService;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

/**
 * 五子棋联机实时消息入口（WebSocket）。
 *
 * <p>前端连接 {@code /ws} 后，订阅 {@code /topic/gomoku/{roomId}} 接收落子 / 重开，
 * 订阅 {@code /user/queue/gomoku} 接收邀请 / 接受 / 拒绝 / 离开等点对点通知。</p>
 */
@Controller
public class GomokuWebSocketController {

    private final GomokuRoomService roomService;
    private final SimpMessagingTemplate messagingTemplate;

    public GomokuWebSocketController(GomokuRoomService roomService, SimpMessagingTemplate messagingTemplate) {
        this.roomService = roomService;
        this.messagingTemplate = messagingTemplate;
    }

    /** 落子：{ roomId, x, y } */
    @MessageMapping("/gomoku/move")
    public void move(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor accessor) {
        Long userId = userId(accessor);
        String roomId = str(payload == null ? null : payload.get("roomId"));
        Integer x = toInt(payload == null ? null : payload.get("x"));
        Integer y = toInt(payload == null ? null : payload.get("y"));
        roomService.move(userId, roomId, x, y);
    }

    /** 再来一局：{ roomId } */
    @MessageMapping("/gomoku/restart")
    public void restart(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor accessor) {
        Long userId = userId(accessor);
        String roomId = str(payload == null ? null : payload.get("roomId"));
        roomService.restart(userId, roomId);
    }

    /** 离开对局：{ roomId } */
    @MessageMapping("/gomoku/leave")
    public void leave(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor accessor) {
        Long userId = userId(accessor);
        String roomId = str(payload == null ? null : payload.get("roomId"));
        roomService.leave(userId, roomId);
    }

    /** 联机处理出错时，回发给当前用户（订阅 /user/queue/error） */
    @MessageExceptionHandler
    public void handleException(BizException e, SimpMessageHeaderAccessor accessor) {
        RoomStateMessage msg = new RoomStateMessage();
        msg.setType("ERROR");
        msg.setMessage(e.getMessage());
        if (accessor.getUser() != null) {
            messagingTemplate.convertAndSendToUser(accessor.getUser().getName(), "/queue/error", msg);
        }
    }

    // ---------- 内部工具 ----------

    private Long userId(SimpMessageHeaderAccessor accessor) {
        Object v = accessor.getSessionAttributes() == null ? null : accessor.getSessionAttributes().get("userId");
        if (v == null) throw new BizException(401, "未登录，无法联机");
        return (Long) v;
    }

    private String str(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private Integer toInt(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.intValue();
        try {
            return Integer.valueOf(String.valueOf(o));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
