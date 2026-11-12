package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.dto.FriendUserVO;
import com.fenglin.springboottest.dto.MessageVO;
import com.fenglin.springboottest.service.SocialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static com.fenglin.springboottest.interceptor.JwtInterceptor.ATTR_USER_ID;

/**
 * 社交接口：好友（添加/接受/拒绝/删除/列表/请求）与私信（发送/历史/会话/未读）
 *
 * <p>全部需要登录令牌（走 {@code JwtInterceptor}）。私信另有 WebSocket 实时推送通道。</p>
 */
@RestController
@RequestMapping("/api/social")
public class SocialController {

    @Autowired
    private SocialService socialService;

    // ---------- 好友 ----------

    /** 搜索用户：GET /api/social/user/search?keyword=xxx（昵称模糊匹配，纯数字按 UID） */
    @GetMapping("/user/search")
    public Result<List<FriendUserVO>> searchUsers(@RequestAttribute(ATTR_USER_ID) Long userId,
                                                  @RequestParam(defaultValue = "") String keyword) {
        return Result.success(socialService.searchUsers(userId, keyword));
    }

    /** 添加好友（或接受请求）：POST /api/social/friend { nickname } */
    @PostMapping("/friend")
    public Result<FriendUserVO> addFriend(@RequestAttribute(ATTR_USER_ID) Long userId,
                                          @RequestBody Map<String, String> body) {
        return Result.success(socialService.addFriend(userId, body == null ? null : body.get("nickname")));
    }

    /** 好友列表：GET /api/social/friends */
    @GetMapping("/friends")
    public Result<List<FriendUserVO>> friends(@RequestAttribute(ATTR_USER_ID) Long userId) {
        return Result.success(socialService.friendList(userId));
    }

    /** 待处理请求：GET /api/social/friend/requests */
    @GetMapping("/friend/requests")
    public Result<List<FriendUserVO>> requests(@RequestAttribute(ATTR_USER_ID) Long userId) {
        return Result.success(socialService.friendRequests(userId));
    }

    /** 接受请求：POST /api/social/friend/{friendId}/accept */
    @PostMapping("/friend/{friendId}/accept")
    public Result<Void> accept(@RequestAttribute(ATTR_USER_ID) Long userId,
                               @PathVariable Long friendId) {
        socialService.acceptFriend(userId, friendId);
        return Result.success();
    }

    /** 拒绝请求：POST /api/social/friend/{friendId}/reject */
    @PostMapping("/friend/{friendId}/reject")
    public Result<Void> reject(@RequestAttribute(ATTR_USER_ID) Long userId,
                               @PathVariable Long friendId) {
        socialService.rejectFriend(userId, friendId);
        return Result.success();
    }

    /** 删除好友：DELETE /api/social/friend/{friendId} */
    @DeleteMapping("/friend/{friendId}")
    public Result<Void> removeFriend(@RequestAttribute(ATTR_USER_ID) Long userId,
                                     @PathVariable Long friendId) {
        socialService.removeFriend(userId, friendId);
        return Result.success();
    }

    // ---------- 私信 ----------

    /** 发送私信：POST /api/social/message { receiverId, content } */
    @PostMapping("/message")
    public Result<MessageVO> send(@RequestAttribute(ATTR_USER_ID) Long userId,
                                  @RequestBody Map<String, Object> body) {
        Long receiverId = body == null ? null : toLong(body.get("receiverId"));
        String content = body == null ? null : String.valueOf(body.get("content"));
        return Result.success(socialService.sendMessage(userId, content, receiverId));
    }

    /** 与某好友的聊天记录：GET /api/social/message/{peerId}?limit=50 */
    @GetMapping("/message/{peerId}")
    public Result<List<MessageVO>> history(@RequestAttribute(ATTR_USER_ID) Long userId,
                                           @PathVariable Long peerId,
                                           @RequestParam(defaultValue = "50") int limit) {
        return Result.success(socialService.history(userId, peerId, limit));
    }

    /** 会话列表：GET /api/social/conversations */
    @GetMapping("/conversations")
    public Result<List<Map<String, Object>>> conversations(@RequestAttribute(ATTR_USER_ID) Long userId) {
        return Result.success(socialService.conversations(userId));
    }

    /** 未读总数：GET /api/social/unread */
    @GetMapping("/unread")
    public Result<Long> unread(@RequestAttribute(ATTR_USER_ID) Long userId) {
        return Result.success(socialService.unreadCount(userId));
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
