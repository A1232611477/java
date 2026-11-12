package com.fenglin.springboottest.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fenglin.springboottest.common.BizException;
import com.fenglin.springboottest.dto.FriendUserVO;
import com.fenglin.springboottest.dto.MessageVO;
import com.fenglin.springboottest.entity.Friend;
import com.fenglin.springboottest.entity.Message;
import com.fenglin.springboottest.entity.User;
import com.fenglin.springboottest.mapper.FriendMapper;
import com.fenglin.springboottest.mapper.MessageMapper;
import com.fenglin.springboottest.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 社交业务：好友关系（添加/接受/删除/列表）与私信（发送/会话/已读）
 *
 * <p>私信发送后通过 {@link SimpMessagingTemplate} 点对点实时推送给接收方
 * （订阅 {@code /user/queue/chat}），前端无需轮询即可收到新消息。</p>
 */
@Service
public class SocialService {

    @Autowired
    private FriendMapper friendMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // ---------- 好友 ----------

    /** 搜索用户：按昵称模糊匹配（也支持按 UID 精确匹配），排除自己，最多返回 20 条 */
    public List<FriendUserVO> searchUsers(Long userId, String keyword) {
        String kw = keyword == null ? "" : keyword.trim();
        if (kw.isEmpty()) {
            return new ArrayList<>();
        }
        List<User> users;
        // 纯数字视为按 UID 搜索
        if (kw.matches("\\d+")) {
            User byId = userMapper.selectById(Long.valueOf(kw));
            users = byId == null ? new ArrayList<>() : new ArrayList<>(List.of(byId));
        } else {
            users = userMapper.selectList(Wrappers.lambdaQuery(User.class)
                    .like(User::getNickname, kw)
                    .last("LIMIT 20"));
        }
        return users.stream()
                .filter(u -> u != null && !u.getId().equals(userId))
                .map(FriendUserVO::from)
                .collect(Collectors.toList());
    }

    /** 添加好友（或接受请求）：按昵称或 UID 发起，不存在/是自己则报错 */
    public FriendUserVO addFriend(Long userId, String nickname) {
        String name = nickname == null ? "" : nickname.trim();
        if (name.isEmpty()) {
            throw new BizException(400, "请输入要添加的昵称或 UID");
        }
        User target;
        if (name.matches("\\d+")) {
            target = userMapper.selectById(Long.valueOf(name));
        } else {
            target = userMapper.selectOne(
                    Wrappers.lambdaQuery(User.class).eq(User::getNickname, name));
        }
        if (target == null) {
            throw new BizException(404, "未找到该用户，请确认昵称或 UID 是否正确");
        }
        if (target.getId().equals(userId)) {
            throw new BizException(400, "不能添加自己为好友");
        }

        // 查询双方之间是否已有记录（任意方向）
        Friend existing = findBetween(userId, target.getId());
        if (existing != null) {
            if (existing.getStatus() != null && existing.getStatus() == 1) {
                throw new BizException(409, "你们已经是好友了");
            }
            // 存在待确认记录：若本次是「对方」来接受，则直接通过；否则提示已发送
            if (existing.getUserId().equals(userId)) {
                throw new BizException(409, "好友请求已发送，等待对方确认");
            }
            // 对方之前发过请求，现在我来添加 = 接受
            existing.setStatus(1);
            friendMapper.updateById(existing);
            // 反向也落一条，保证任意方向都能查到
            ensureReverse(userId, target.getId());
            return FriendUserVO.from(target);
        }

        // 全新请求：写入「我 → 对方」待确认
        Friend f = new Friend();
        f.setUserId(userId);
        f.setFriendId(target.getId());
        f.setStatus(0);
        friendMapper.insert(f);
        return FriendUserVO.from(target);
    }

    /** 接受好友请求 */
    public void acceptFriend(Long userId, Long friendId) {
        Friend f = findBetween(userId, friendId);
        if (f == null) {
            throw new BizException(404, "没有待处理的好友请求");
        }
        if (f.getStatus() != null && f.getStatus() == 1) {
            throw new BizException(409, "你们已经是好友了");
        }
        f.setStatus(1);
        friendMapper.updateById(f);
        ensureReverse(userId, friendId);
    }

    /** 拒绝好友请求 */
    public void rejectFriend(Long userId, Long friendId) {
        Friend f = findBetween(userId, friendId);
        if (f == null) {
            return;
        }
        friendMapper.deleteById(f.getId());
    }

    /** 删除好友（双向都删） */
    public void removeFriend(Long userId, Long friendId) {
        List<Friend> list = friendMapper.selectList(Wrappers.lambdaQuery(Friend.class)
                .and(w -> w
                        .and(x -> x.eq(Friend::getUserId, userId).eq(Friend::getFriendId, friendId))
                        .or(x -> x.eq(Friend::getUserId, friendId).eq(Friend::getFriendId, userId))));
        for (Friend f : list) {
            friendMapper.deleteById(f.getId());
        }
    }

    /** 好友列表（已接受）：附带对方用户信息 */
    public List<FriendUserVO> friendList(Long userId) {
        List<Friend> accepted = friendMapper.selectList(Wrappers.lambdaQuery(Friend.class)
                .eq(Friend::getUserId, userId)
                .eq(Friend::getStatus, 1));
        List<FriendUserVO> result = new ArrayList<>();
        for (Friend f : accepted) {
            User u = userMapper.selectById(f.getFriendId());
            if (u != null) {
                result.add(FriendUserVO.from(u));
            }
        }
        return result;
    }

    /** 待处理的好友请求（别人发给我的、status=0） */
    public List<FriendUserVO> friendRequests(Long userId) {
        List<Friend> pending = friendMapper.selectList(Wrappers.lambdaQuery(Friend.class)
                .eq(Friend::getFriendId, userId)
                .eq(Friend::getStatus, 0));
        List<FriendUserVO> result = new ArrayList<>();
        for (Friend f : pending) {
            User u = userMapper.selectById(f.getUserId());
            if (u != null) {
                result.add(FriendUserVO.from(u));
            }
        }
        return result;
    }

    // ---------- 私信 ----------

    /** 发送私信：落库 + 实时推送接收方 */
    public MessageVO sendMessage(Long senderId, String content, Long receiverId) {
        if (content == null || content.isBlank()) {
            throw new BizException(400, "消息内容不能为空");
        }
        if (content.length() > 500) {
            throw new BizException(400, "消息过长（最多 500 字）");
        }
        User receiver = userMapper.selectById(receiverId);
        if (receiver == null) {
            throw new BizException(404, "对方用户不存在");
        }
        User sender = userMapper.selectById(senderId);

        Message m = new Message();
        m.setSenderId(senderId);
        m.setReceiverId(receiverId);
        m.setContent(content.trim());
        m.setReadFlag(0);
        messageMapper.insert(m);

        MessageVO vo = MessageVO.from(m, sender == null ? "" : sender.getNickname());
        // 点对点推送给接收方（订阅 /user/queue/chat）
        messagingTemplate.convertAndSendToUser(String.valueOf(receiverId), "/queue/chat", vo);
        return vo;
    }

    /** 与某好友的聊天记录（双向，按时间正序），并顺带把「对方发给我的」标记已读 */
    public List<MessageVO> history(Long userId, Long peerId, int limit) {
        int sz = Math.min(Math.max(limit, 1), 200);
        List<Message> list = messageMapper.selectList(Wrappers.lambdaQuery(Message.class)
                .and(w -> w
                        .and(x -> x.eq(Message::getSenderId, userId).eq(Message::getReceiverId, peerId))
                        .or(x -> x.eq(Message::getSenderId, peerId).eq(Message::getReceiverId, userId)))
                .orderByAsc(Message::getId)
                .last("LIMIT " + sz));

        // 把「对方发给我的未读」标记已读
        List<Message> toMark = list.stream()
                .filter(m -> m.getReceiverId().equals(userId) && (m.getReadFlag() == null || m.getReadFlag() == 0))
                .collect(Collectors.toList());
        for (Message m : toMark) {
            m.setReadFlag(1);
            messageMapper.updateById(m);
        }

        // 组装昵称映射
        Map<Long, String> nicknameById = new HashMap<>();
        for (Message m : list) {
            if (!nicknameById.containsKey(m.getSenderId())) {
                User u = userMapper.selectById(m.getSenderId());
                nicknameById.put(m.getSenderId(), u == null ? "" : u.getNickname());
            }
        }
        return list.stream()
                .map(m -> MessageVO.from(m, nicknameById.getOrDefault(m.getSenderId(), "")))
                .collect(Collectors.toList());
    }

    /** 会话列表：与我有过消息往来的所有用户 + 最后一条消息 + 未读数 */
    public List<Map<String, Object>> conversations(Long userId) {
        List<Message> all = messageMapper.selectList(Wrappers.lambdaQuery(Message.class)
                .and(w -> w.eq(Message::getSenderId, userId).or().eq(Message::getReceiverId, userId))
                .orderByDesc(Message::getId));

        // 按「对方 id」归并，取最后一条
        Map<Long, Message> lastByPeer = new LinkedHashMapById();
        Map<Long, Integer> unreadByPeer = new HashMap<>();
        for (Message m : all) {
            Long peer = m.getSenderId().equals(userId) ? m.getReceiverId() : m.getSenderId();
            if (!lastByPeer.containsKey(peer)) {
                lastByPeer.put(peer, m);
            }
            if (m.getReceiverId().equals(userId) && (m.getReadFlag() == null || m.getReadFlag() == 0)) {
                unreadByPeer.merge(peer, 1, Integer::sum);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Long, Message> e : lastByPeer.entrySet()) {
            User u = userMapper.selectById(e.getKey());
            if (u == null) continue;
            Map<String, Object> item = new HashMap<>();
            item.put("peerId", e.getKey());
            item.put("nickname", u.getNickname());
            item.put("lastMessage", e.getValue().getContent());
            item.put("lastTime", e.getValue().getCreatedAt());
            item.put("unread", unreadByPeer.getOrDefault(e.getKey(), 0));
            result.add(item);
        }
        return result;
    }

    /** 未读私信总数 */
    public long unreadCount(Long userId) {
        return messageMapper.selectCount(Wrappers.lambdaQuery(Message.class)
                .eq(Message::getReceiverId, userId)
                .eq(Message::getReadFlag, 0));
    }

    // ---------- 内部工具 ----------

    /** 查询两人之间任意方向的一条记录 */
    private Friend findBetween(Long a, Long b) {
        List<Friend> list = friendMapper.selectList(Wrappers.lambdaQuery(Friend.class)
                .and(w -> w
                        .and(x -> x.eq(Friend::getUserId, a).eq(Friend::getFriendId, b))
                        .or(x -> x.eq(Friend::getUserId, b).eq(Friend::getFriendId, a))));
        return list.isEmpty() ? null : list.get(0);
    }

    /** 保证反向关系也存在（已接受），便于双向查询 */
    private void ensureReverse(Long a, Long b) {
        Long count = friendMapper.selectCount(Wrappers.lambdaQuery(Friend.class)
                .eq(Friend::getUserId, b).eq(Friend::getFriendId, a));
        if (count == null || count == 0) {
            Friend rev = new Friend();
            rev.setUserId(b);
            rev.setFriendId(a);
            rev.setStatus(1);
            friendMapper.insert(rev);
        }
    }

    /** 保序的去重 Map（LinkedHashMap 语义） */
    private static class LinkedHashMapById extends java.util.LinkedHashMap<Long, Message> {
        private static final long serialVersionUID = 1L;
    }
}
