package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.dto.gomoku.GomokuRoomVO;
import com.fenglin.springboottest.service.GomokuRoomService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.fenglin.springboottest.interceptor.JwtInterceptor.ATTR_USER_ID;
import static com.fenglin.springboottest.interceptor.JwtInterceptor.ATTR_USER_NICKNAME;

/**
 * 五子棋联机的 REST 入口：邀请 / 接受 / 拒绝 / 取消 / 查询。
 *
 * <p>落子、重开、离开走 WebSocket（见 {@code GomokuWebSocketController}）。</p>
 */
@RestController
@RequestMapping("/api/gomoku")
public class GomokuController {

    private final GomokuRoomService roomService;

    public GomokuController(GomokuRoomService roomService) {
        this.roomService = roomService;
    }

    /** 邀请好友联机：body { friendId } */
    @PostMapping("/invite")
    public Result<Map<String, Object>> invite(@RequestAttribute(ATTR_USER_ID) Long userId,
                                              @RequestAttribute(ATTR_USER_NICKNAME) String nickname,
                                              @RequestBody Map<String, Object> body) {
        Long friendId = toLong(body == null ? null : body.get("friendId"));
        return Result.success(roomService.invite(userId, nickname, friendId));
    }

    /** 接受邀请：body { roomId } */
    @PostMapping("/accept")
    public Result<Map<String, Object>> accept(@RequestAttribute(ATTR_USER_ID) Long userId,
                                              @RequestAttribute(ATTR_USER_NICKNAME) String nickname,
                                              @RequestBody Map<String, Object> body) {
        String roomId = str(body == null ? null : body.get("roomId"));
        return Result.success(roomService.accept(userId, nickname, roomId));
    }

    /** 拒绝邀请：body { roomId } */
    @PostMapping("/decline")
    public Result<Void> decline(@RequestAttribute(ATTR_USER_ID) Long userId,
                                @RequestBody Map<String, Object> body) {
        String roomId = str(body == null ? null : body.get("roomId"));
        roomService.decline(userId, roomId);
        return Result.success();
    }

    /** 取消邀请：body { roomId } */
    @PostMapping("/cancel")
    public Result<Void> cancel(@RequestAttribute(ATTR_USER_ID) Long userId,
                               @RequestBody Map<String, Object> body) {
        String roomId = str(body == null ? null : body.get("roomId"));
        roomService.cancel(userId, roomId);
        return Result.success();
    }

    /** 查询当前用户待处理的邀请 */
    @GetMapping("/pending")
    public Result<Map<String, Object>> pending(@RequestAttribute(ATTR_USER_ID) Long userId) {
        return Result.success(roomService.pendingFor(userId));
    }

    /** 查询房间快照 */
    @GetMapping("/room/{roomId}")
    public Result<GomokuRoomVO> room(@PathVariable("roomId") String roomId) {
        return Result.success(roomService.getRoom(roomId));
    }

    // ---------- 内部工具 ----------

    private String str(Object o) {
        return o == null ? null : String.valueOf(o);
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
