package com.fenglin.springboottest.service;

import com.fenglin.springboottest.common.BizException;
import com.fenglin.springboottest.dto.gomoku.GomokuRoomVO;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 五子棋「好友联机」房间服务（内存态，单机演示）。
 *
 * <p>约定：</p>
 * <ul>
 *   <li>房主 = 黑棋（color 1），受邀好友 = 白棋（color 2），黑棋先行。</li>
 *   <li>邀请 / 接受 / 拒绝 / 取消走 REST，实时落子 / 重开 / 离开走 WebSocket。</li>
 *   <li>服务端维护棋盘与回合（只做落子合法性校验与转发），胜负判定由两端客户端本地完成。</li>
 * </ul>
 */
@Service
public class GomokuRoomService {

    public static final int SIZE = 15;

    private static final SecureRandom RANDOM = new SecureRandom();

    private final SimpMessagingTemplate messagingTemplate;

    /** roomId -> 房间 */
    private final Map<String, GomokuRoom> rooms = new ConcurrentHashMap<>();

    public GomokuRoomService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /** 发起邀请：创建房间（房主=黑），点对点通知受邀好友 */
    public Map<String, Object> invite(Long hostId, String hostNickname, Long friendId) {
        if (friendId == null || friendId.equals(hostId)) {
            throw new BizException(400, "请选择要邀请的好友");
        }
        String roomId = genRoomId();
        GomokuRoom room = new GomokuRoom(roomId, hostId, hostNickname, friendId);
        rooms.put(roomId, room);

        Map<String, Object> invite = new HashMap<>();
        invite.put("type", "INVITE");
        invite.put("roomId", roomId);
        invite.put("fromUserId", hostId);
        invite.put("fromNickname", hostNickname);
        messagingTemplate.convertAndSendToUser(String.valueOf(friendId), "/queue/gomoku", invite);

        Map<String, Object> data = new HashMap<>();
        data.put("roomId", roomId);
        data.put("color", 1);
        return data;
    }

    /** 接受邀请：标记开局（受邀方=白），通知房主 */
    public Map<String, Object> accept(Long userId, String nickname, String roomId) {
        GomokuRoom room = requireRoom(roomId);
        if (!userId.equals(room.guestId)) {
            throw new BizException("你不是被邀请的玩家");
        }
        if (room.started) {
            return acceptedData(room);
        }
        room.started = true;
        room.guestNickname = nickname;

        Map<String, Object> msg = new HashMap<>();
        msg.put("type", "ACCEPTED");
        msg.put("roomId", roomId);
        msg.put("guestNickname", nickname);
        messagingTemplate.convertAndSendToUser(String.valueOf(room.hostId), "/queue/gomoku", msg);

        return acceptedData(room);
    }

    /** 拒绝邀请：解散房间，通知房主 */
    public void decline(Long userId, String roomId) {
        GomokuRoom room = rooms.get(roomId);
        if (room == null) return;
        if (!userId.equals(room.guestId)) return;
        rooms.remove(roomId);
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", "DECLINED");
        msg.put("roomId", roomId);
        messagingTemplate.convertAndSendToUser(String.valueOf(room.hostId), "/queue/gomoku", msg);
    }

    /** 房主取消邀请：解散房间，通知受邀好友 */
    public void cancel(Long userId, String roomId) {
        GomokuRoom room = rooms.get(roomId);
        if (room == null) return;
        if (!userId.equals(room.hostId)) return;
        rooms.remove(roomId);
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", "CANCELLED");
        msg.put("roomId", roomId);
        messagingTemplate.convertAndSendToUser(String.valueOf(room.guestId), "/queue/gomoku", msg);
    }

    /** 落子：校验合法性并广播给房间内两人 */
    public void move(Long userId, String roomId, Integer x, Integer y) {
        GomokuRoom room = requireRoom(roomId);
        if (!room.started) throw new BizException("对局尚未开始");
        if (x == null || y == null || x < 0 || x >= SIZE || y < 0 || y >= SIZE) {
            throw new BizException(400, "落子位置无效");
        }
        int color = colorOf(room, userId);
        if (color != room.nextColor) throw new BizException("还没轮到你落子");
        if (room.board[y][x] != 0) throw new BizException("该位置已有棋子");

        room.board[y][x] = color;
        room.moves.add(new int[]{x, y, color});
        room.nextColor = color == 1 ? 2 : 1;

        Map<String, Object> msg = new HashMap<>();
        msg.put("type", "MOVE");
        msg.put("roomId", roomId);
        msg.put("x", x);
        msg.put("y", y);
        msg.put("color", color);
        msg.put("nextColor", room.nextColor);
        messagingTemplate.convertAndSend("/topic/gomoku/" + roomId, msg);
    }

    /** 再来一局：清空棋盘，黑方先行，广播给两人 */
    public void restart(Long userId, String roomId) {
        GomokuRoom room = requireRoom(roomId);
        if (!userId.equals(room.hostId) && !userId.equals(room.guestId)) {
            throw new BizException("你不在该房间中");
        }
        room.board = new int[SIZE][SIZE];
        room.moves.clear();
        room.nextColor = 1;
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", "RESTART");
        msg.put("roomId", roomId);
        messagingTemplate.convertAndSend("/topic/gomoku/" + roomId, msg);
    }

    /** 离开对局：通知对方并解散房间 */
    public void leave(Long userId, String roomId) {
        GomokuRoom room = rooms.get(roomId);
        if (room == null) return;
        boolean isHost = userId.equals(room.hostId);
        boolean isGuest = userId.equals(room.guestId);
        if (!isHost && !isGuest) return;
        rooms.remove(roomId);

        Long peer = isHost ? room.guestId : room.hostId;
        if (peer != null) {
            Map<String, Object> msg = new HashMap<>();
            msg.put("type", "PEER_LEFT");
            msg.put("roomId", roomId);
            msg.put("message", (isHost ? room.hostNickname : room.guestNickname) + " 离开了对局");
            messagingTemplate.convertAndSendToUser(String.valueOf(peer), "/queue/gomoku", msg);
        }
    }

    /** 查询当前用户待处理（尚未接受）的邀请，没有返回 null */
    public Map<String, Object> pendingFor(Long userId) {
        for (GomokuRoom room : rooms.values()) {
            if (!room.started && userId.equals(room.guestId)) {
                Map<String, Object> data = new HashMap<>();
                data.put("roomId", room.roomId);
                data.put("fromUserId", room.hostId);
                data.put("fromNickname", room.hostNickname);
                return data;
            }
        }
        return null;
    }

    /** 查询房间快照（用于进房后初始化 / 同步） */
    public GomokuRoomVO getRoom(String roomId) {
        return requireRoom(roomId).toVO();
    }

    // ---------- 内部工具 ----------

    private GomokuRoom requireRoom(String roomId) {
        if (roomId == null) throw new BizException("房间号不能为空");
        GomokuRoom room = rooms.get(roomId);
        if (room == null) throw new BizException("房间不存在或已解散");
        return room;
    }

    private int colorOf(GomokuRoom room, Long userId) {
        if (userId.equals(room.hostId)) return 1;
        if (userId.equals(room.guestId)) return 2;
        throw new BizException("你不在该房间中");
    }

    private Map<String, Object> acceptedData(GomokuRoom room) {
        Map<String, Object> data = new HashMap<>();
        data.put("roomId", room.roomId);
        data.put("color", 2);
        data.put("hostNickname", room.hostNickname);
        return data;
    }

    private String genRoomId() {
        int guard = 0;
        String roomId;
        do {
            roomId = String.format("%06d", RANDOM.nextInt(1_000_000));
            if (++guard > 50) throw new BizException("创建房间失败，请稍后再试");
        } while (rooms.containsKey(roomId));
        return roomId;
    }

    /** 单个联机房间 */
    private static final class GomokuRoom {
        final String roomId;
        final Long hostId;
        final String hostNickname;
        final Long guestId;
        String guestNickname;
        boolean started;
        int nextColor = 1; // 1=黑=房主先行
        int[][] board = new int[SIZE][SIZE];
        final List<int[]> moves = new ArrayList<>();

        GomokuRoom(String roomId, Long hostId, String hostNickname, Long guestId) {
            this.roomId = roomId;
            this.hostId = hostId;
            this.hostNickname = hostNickname;
            this.guestId = guestId;
        }

        GomokuRoomVO toVO() {
            GomokuRoomVO vo = new GomokuRoomVO();
            vo.setRoomId(roomId);
            vo.setHostId(hostId);
            vo.setHostNickname(hostNickname);
            vo.setGuestId(guestId);
            vo.setGuestNickname(guestNickname);
            vo.setStarted(started);
            vo.setNextColor(nextColor);
            vo.setMoves(new ArrayList<>(moves));
            return vo;
        }
    }
}
