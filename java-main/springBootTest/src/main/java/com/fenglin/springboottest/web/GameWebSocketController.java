package com.fenglin.springboottest.web;

import com.fenglin.springboottest.common.BizException;
import com.fenglin.springboottest.dto.game.GameMessage;
import com.fenglin.springboottest.dto.game.RoomStateMessage;
import com.fenglin.springboottest.service.GameRoomService;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * 联机消息入口（房间内实时消息）。
 *
 * <p>创建/加入房间走 REST（见 {@code GameRoomController}），拿到房间号后
 * 前端订阅 {@code /topic/room/{roomId}}，再通过这里发送房间内消息。</p>
 *
 * <p>用户身份取自 {@link WebSocketAuthInterceptor} 写入 session 的 {@code userId}/{@code nickname}。</p>
 */
@Controller
public class GameWebSocketController {

    private final GameRoomService roomService;
    private final SimpMessagingTemplate messagingTemplate;

    public GameWebSocketController(GameRoomService roomService, SimpMessagingTemplate messagingTemplate) {
        this.roomService = roomService;
        this.messagingTemplate = messagingTemplate;
    }

    /** 房间内消息处理出错时，回发给当前用户的错误提示（订阅 /user/queue/error） */
    @MessageExceptionHandler
    public void handleException(BizException e, SimpMessageHeaderAccessor accessor) {
        RoomStateMessage msg = new RoomStateMessage();
        msg.setType("ERROR");
        msg.setMessage(e.getMessage());
        if (accessor.getUser() != null) {
            messagingTemplate.convertAndSendToUser(accessor.getUser().getName(), "/queue/error", msg);
        }
    }

    /** 离开房间 */
    @MessageMapping("/game/leave")
    public void leave(@Payload GameMessage msg, SimpMessageHeaderAccessor accessor) {
        roomService.leaveRoom(userId(accessor), msg.getRoomId());
    }

    /** 选角 */
    @MessageMapping("/game/role")
    public void role(@Payload GameMessage msg, SimpMessageHeaderAccessor accessor) {
        roomService.selectRole(userId(accessor), msg.getRoomId(), msg.getRoleId());
    }

    /** 准备/取消准备 */
    @MessageMapping("/game/ready")
    public void ready(@Payload GameMessage msg, SimpMessageHeaderAccessor accessor) {
        roomService.toggleReady(userId(accessor), msg.getRoomId());
    }

    /** 房主开局 */
    @MessageMapping("/game/start")
    public void start(@Payload GameMessage msg, SimpMessageHeaderAccessor accessor) {
        roomService.startGame(userId(accessor), msg.getRoomId());
    }

    /** 玩家移动/状态上报 */
    @MessageMapping("/game/move")
    public void move(@Payload GameMessage msg, SimpMessageHeaderAccessor accessor) {
        roomService.updateState(userId(accessor), msg.getRoomId(), msg);
    }

    /** 玩家动作事件（修机/开门/救人等）转发 */
    @MessageMapping("/game/action")
    public void action(@Payload GameMessage msg, SimpMessageHeaderAccessor accessor) {
        roomService.broadcastAction(userId(accessor), msg.getRoomId(), msg);
    }

    /** 房主广播权威状态（AI 求生者 / 监管者 / 密码机 / 门 / 胜负） */
    @MessageMapping("/game/host")
    public void host(@Payload GameMessage msg, SimpMessageHeaderAccessor accessor) {
        roomService.broadcastAuthority(userId(accessor), msg);
    }

    // ---------- 内部工具 ----------

    private Long userId(SimpMessageHeaderAccessor accessor) {
        Object v = accessor.getSessionAttributes() == null ? null : accessor.getSessionAttributes().get("userId");
        if (v == null) throw new BizException(401, "未登录，无法联机");
        return (Long) v;
    }
}
