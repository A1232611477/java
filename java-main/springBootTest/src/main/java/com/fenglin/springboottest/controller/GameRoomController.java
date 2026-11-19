package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.dto.game.GamePlayerVO;
import com.fenglin.springboottest.service.GameRoomService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fenglin.springboottest.interceptor.JwtInterceptor.ATTR_USER_ID;
import static com.fenglin.springboottest.interceptor.JwtInterceptor.ATTR_USER_NICKNAME;

/**
 * 联机房间的 REST 入口：创建 / 加入 / 查询房间。
 *
 * <p>创建与加入走 HTTP（复用 {@code JwtInterceptor} 鉴权），拿到房间号后，
 * 前端再用 WebSocket 订阅 {@code /topic/room/{roomId}} 做实时同步。
 * 这样避免了在 WebSocket 握手阶段做「点对点回发房间号」的复杂逻辑。</p>
 */
@RestController
@RequestMapping("/api/game")
public class GameRoomController {

    private final GameRoomService roomService;

    public GameRoomController(GameRoomService roomService) {
        this.roomService = roomService;
    }

    /** 创建房间，返回房间号 */
    @PostMapping("/create")
    public Result<Map<String, Object>> create(@RequestAttribute(ATTR_USER_ID) Long userId,
                                              @RequestAttribute(ATTR_USER_NICKNAME) String nickname) {
        String roomId = roomService.createRoom(userId, nickname);
        Map<String, Object> data = new HashMap<>();
        data.put("roomId", roomId);
        data.put("myUserId", userId);
        return Result.success(data);
    }

    /** 加入房间，返回房间号 */
    @PostMapping("/join")
    public Result<Map<String, Object>> join(@RequestAttribute(ATTR_USER_ID) Long userId,
                                            @RequestAttribute(ATTR_USER_NICKNAME) String nickname,
                                            @RequestParam("roomId") String roomId) {
        String joined = roomService.joinRoom(userId, nickname, roomId);
        Map<String, Object> data = new HashMap<>();
        data.put("roomId", joined);
        data.put("myUserId", userId);
        return Result.success(data);
    }

    /** 查询房间当前玩家列表 */
    @GetMapping("/room/{roomId}")
    public Result<List<GamePlayerVO>> room(@PathVariable("roomId") String roomId) {
        return Result.success(roomService.getPlayers(roomId));
    }

    // ---------- 好友邀请联机 ----------

    /** 邀请好友联机：body { friendId }，返回 { roomId, myUserId } */
    @PostMapping("/invite")
    public Result<Map<String, Object>> invite(@RequestAttribute(ATTR_USER_ID) Long userId,
                                              @RequestAttribute(ATTR_USER_NICKNAME) String nickname,
                                              @RequestBody Map<String, Object> body) {
        Long friendId = toLong(body == null ? null : body.get("friendId"));
        return Result.success(roomService.inviteFriend(userId, nickname, friendId));
    }

    /** 接受邀请：body { roomId }，返回 { roomId, myUserId } */
    @PostMapping("/accept")
    public Result<Map<String, Object>> accept(@RequestAttribute(ATTR_USER_ID) Long userId,
                                              @RequestAttribute(ATTR_USER_NICKNAME) String nickname,
                                              @RequestBody Map<String, Object> body) {
        String roomId = str(body == null ? null : body.get("roomId"));
        return Result.success(roomService.acceptInvite(userId, nickname, roomId));
    }

    /** 拒绝邀请：body { roomId } */
    @PostMapping("/decline")
    public Result<Void> decline(@RequestAttribute(ATTR_USER_ID) Long userId,
                                @RequestBody Map<String, Object> body) {
        String roomId = str(body == null ? null : body.get("roomId"));
        roomService.declineInvite(userId, roomId);
        return Result.success();
    }

    /** 取消邀请：body { roomId } */
    @PostMapping("/cancel")
    public Result<Void> cancel(@RequestAttribute(ATTR_USER_ID) Long userId,
                               @RequestBody Map<String, Object> body) {
        String roomId = str(body == null ? null : body.get("roomId"));
        roomService.cancelInvite(userId, roomId);
        return Result.success();
    }

    /** 查询当前用户待处理的邀请 */
    @GetMapping("/pending")
    public Result<Map<String, Object>> pending(@RequestAttribute(ATTR_USER_ID) Long userId) {
        return Result.success(roomService.pendingInvite(userId));
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
