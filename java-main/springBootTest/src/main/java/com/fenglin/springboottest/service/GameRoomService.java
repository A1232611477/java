package com.fenglin.springboottest.service;

import com.fenglin.springboottest.common.BizException;
import com.fenglin.springboottest.dto.game.GameMessage;
import com.fenglin.springboottest.dto.game.GamePlayerVO;
import com.fenglin.springboottest.dto.game.RoomStateMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 小游戏「好友联机」房间服务（内存态，单机演示）。
 *
 * <p>约定：</p>
 * <ul>
 *   <li>房间号：6 位数字，创建时随机生成，撞号重试。</li>
 *   <li>每局最多 4 名真人（对应 4 名求生者），不足部分由前端 AI 补齐。</li>
 *   <li>房主开局后进入对局，玩家通过 {@code /app/game/move} 上报坐标，服务端广播给同房其他玩家。</li>
 *   <li>监管者（杰克）始终由 AI 控制，服务端只做玩家位置/状态转发，不做权威判定。</li>
 * </ul>
 *
 * <p>这是「真人队友协作」方案的实现：多个真人各自控制一名求生者，其余位置 AI 补齐，
 * 共同对抗 AI 监管者。真正的权威同步/胜负判定仍需前端本地执行，服务端负责房间与转发。</p>
 */
@Service
public class GameRoomService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int MAX_PLAYERS = 4;

    private final SimpMessagingTemplate messagingTemplate;

    /** roomId -> 房间内玩家（线程安全） */
    private final Map<String, Map<Long, GamePlayerVO>> rooms = new ConcurrentHashMap<>();

    /** roomId -> 房主最近一次广播的权威状态 */
    private final Map<String, Object> authority = new ConcurrentHashMap<>();

    public GameRoomService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /** 创建房间，返回房间号；创建者自动成为房主 */
    public String createRoom(Long userId, String nickname) {
        String roomId;
        int guard = 0;
        do {
            roomId = genRoomId();
            if (++guard > 50) {
                throw new BizException("创建房间失败，请稍后再试");
            }
        } while (rooms.containsKey(roomId));

        Map<Long, GamePlayerVO> players = new ConcurrentHashMap<>();
        players.put(userId, makePlayer(userId, nickname, true));
        rooms.put(roomId, players);
        return roomId;
    }

    /** 加入房间；返回房间号 */
    public String joinRoom(Long userId, String nickname, String roomId) {
        if (roomId == null || !roomId.matches("\\d{6}")) {
            throw new BizException("房间号格式错误（应为 6 位数字）");
        }
        Map<Long, GamePlayerVO> players = rooms.get(roomId);
        if (players == null) {
            throw new BizException("房间不存在，请检查房间号");
        }
        if (players.containsKey(userId)) {
            return roomId; // 已在房间中，幂等
        }
        if (players.size() >= MAX_PLAYERS) {
            throw new BizException("房间已满（最多 4 人）");
        }
        players.put(userId, makePlayer(userId, nickname, false));
        broadcast(roomId, joinedMessage(roomId, userId, nickname, players));
        return roomId;
    }

    /** 离开房间；房间空了就销毁 */
    public void leaveRoom(Long userId, String roomId) {
        if (roomId == null) return;
        Map<Long, GamePlayerVO> players = rooms.get(roomId);
        if (players == null) return;
        players.remove(userId);
        if (players.isEmpty()) {
            rooms.remove(roomId);
            return;
        }
        // 房主离开则转移给剩余最早加入的人（简化：任取一人）
        if (players.values().stream().noneMatch(GamePlayerVO::isHost)) {
            GamePlayerVO first = players.values().iterator().next();
            first.setHost(true);
        }
        broadcast(roomId, stateMessage(roomId, "LEFT", players, userId + " 离开了房间"));
    }

    /** 玩家选角 */
    public void selectRole(Long userId, String roomId, String roleId) {
        Map<Long, GamePlayerVO> players = requirePlayers(roomId);
        GamePlayerVO p = players.get(userId);
        if (p == null) throw new BizException("你不在该房间中");
        p.setRoleId(roleId);
        broadcast(roomId, stateMessage(roomId, "ROOM_STATE", players, null));
    }

    /** 玩家准备/取消准备 */
    public void toggleReady(Long userId, String roomId) {
        Map<Long, GamePlayerVO> players = requirePlayers(roomId);
        GamePlayerVO p = players.get(userId);
        if (p == null) throw new BizException("你不在该房间中");
        p.setReady(!p.isReady());
        broadcast(roomId, stateMessage(roomId, "ROOM_STATE", players, null));
    }

    /** 房主开局：分配槽位，广播 STARTED */
    public void startGame(Long userId, String roomId) {
        Map<Long, GamePlayerVO> players = requirePlayers(roomId);
        GamePlayerVO host = players.get(userId);
        if (host == null || !host.isHost()) {
            throw new BizException("只有房主可以开始游戏");
        }
        if (players.size() < 2) {
            throw new BizException("至少需要 2 名玩家才能开始联机");
        }
        int slot = 0;
        for (GamePlayerVO p : players.values()) {
            p.setSlot(slot++);
            p.setAlive(true);
            p.setHp(2);
            p.setEscaped(false);
            p.setChaired(false);
        }
        RoomStateMessage msg = stateMessage(roomId, "STARTED", players, null);
        msg.setStarted(true);
        broadcast(roomId, msg);
    }

    /** 玩家位置/状态上报：转发给同房其他玩家 */
    public void updateState(Long userId, String roomId, GameMessage m) {
        Map<Long, GamePlayerVO> players = rooms.get(roomId);
        if (players == null) return;
        GamePlayerVO p = players.get(userId);
        if (p == null) return;
        if (m.getX() != null) p.setX(m.getX());
        if (m.getY() != null) p.setY(m.getY());
        if (m.getHp() != null) p.setHp(m.getHp());
        if (m.getAlive() != null) p.setAlive(m.getAlive());
        if (m.getEscaped() != null) p.setEscaped(m.getEscaped());
        if (m.getChaired() != null) p.setChaired(m.getChaired());

        // 广播全量状态给房间内所有玩家（含上报者自己，保证一致）
        broadcast(roomId, stateMessage(roomId, "ROOM_STATE", players, null));
    }

    /** 房主广播权威状态：记录并转发给同房所有玩家 */
    public void broadcastAuthority(Long userId, GameMessage m) {
        String roomId = m.getRoomId();
        if (roomId == null) return;
        Map<Long, GamePlayerVO> players = rooms.get(roomId);
        if (players == null) return;
        GamePlayerVO host = players.get(userId);
        if (host == null || !host.isHost()) return; // 仅房主可广播权威状态
        authority.put(roomId, m.getAuthority());
        RoomStateMessage msg = stateMessage(roomId, "ROOM_STATE", players, null);
        msg.setAuthority(m.getAuthority());
        broadcast(roomId, msg);
    }

    /** 广播动作事件（修机/开门/救人等），服务端只转发 */
    public void broadcastAction(Long userId, String roomId, GameMessage m) {
        Map<Long, GamePlayerVO> players = rooms.get(roomId);
        if (players == null) return;
        GamePlayerVO p = players.get(userId);
        if (p == null) return;
        RoomStateMessage msg = new RoomStateMessage();
        msg.setType("ACTION");
        msg.setRoomId(roomId);
        msg.setMessage(p.getNickname() + " " + (m.getAction() == null ? "" : m.getAction()));
        msg.setHostId(hostId(players));
        broadcast(roomId, msg);
    }

    /** 获取房间当前玩家列表（用于客户端进房后初始化） */
    public List<GamePlayerVO> getPlayers(String roomId) {
        Map<Long, GamePlayerVO> players = rooms.get(roomId);
        return players == null ? new ArrayList<>() : new ArrayList<>(players.values());
    }

    // ---------- 内部工具 ----------

    private Map<Long, GamePlayerVO> requirePlayers(String roomId) {
        Map<Long, GamePlayerVO> players = rooms.get(roomId);
        if (players == null) throw new BizException("房间不存在或已解散");
        return players;
    }

    private GamePlayerVO makePlayer(Long userId, String nickname, boolean host) {
        GamePlayerVO p = new GamePlayerVO();
        p.setUserId(userId);
        p.setNickname(nickname);
        p.setHost(host);
        p.setReady(false);
        p.setSlot(-1);
        p.setAlive(true);
        p.setHp(2);
        p.setEscaped(false);
        p.setChaired(false);
        return p;
    }

    private Long hostId(Map<Long, GamePlayerVO> players) {
        for (GamePlayerVO p : players.values()) if (p.isHost()) return p.getUserId();
        return players.values().iterator().hasNext() ? players.values().iterator().next().getUserId() : null;
    }

    private RoomStateMessage stateMessage(String roomId, String type, Map<Long, GamePlayerVO> players, String message) {
        RoomStateMessage msg = new RoomStateMessage();
        msg.setType(type);
        msg.setRoomId(roomId);
        msg.setPlayers(new ArrayList<>(players.values()));
        msg.setHostId(hostId(players));
        msg.setMessage(message);
        msg.setStarted(players.values().stream().allMatch(p -> p.getSlot() >= 0) && !players.isEmpty());
        msg.setAuthority(authority.get(roomId));
        return msg;
    }

    private RoomStateMessage joinedMessage(String roomId, Long userId, String nickname, Map<Long, GamePlayerVO> players) {
        RoomStateMessage msg = stateMessage(roomId, "JOINED", players, nickname + " 加入了房间");
        return msg;
    }

    private void broadcast(String roomId, RoomStateMessage msg) {
        messagingTemplate.convertAndSend("/topic/room/" + roomId, msg);
    }

    private String genRoomId() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }
}
