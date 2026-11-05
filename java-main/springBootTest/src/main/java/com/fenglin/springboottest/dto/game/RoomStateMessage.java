package com.fenglin.springboottest.dto.game;

import java.io.Serializable;
import java.util.List;

/**
 * 房间状态广播体：客户端订阅 {@code /topic/room/{roomId}} 收到。
 *
 * <p>type 取值：</p>
 * <ul>
 *   <li>{@code ROOM_STATE}  —— 大厅/对局中的完整房间状态（玩家列表、对局是否开始）</li>
 *   <li>{@code JOINED}       —— 有人加入房间</li>
 *   <li>{@code LEFT}         —— 有人离开房间</li>
 *   <li>{@code STARTED}      —— 房主开局，对局开始</li>
 *   <li>{@code ERROR}        —— 服务端主动返回的错误提示</li>
 * </ul>
 */
public class RoomStateMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String type;
    private String roomId;
    private boolean started;
    private List<GamePlayerVO> players;
    private String message;
    /** 房主用户 id（客户端判断自己能否开局） */
    private Long hostId;

    /** 房主广播的权威状态（非房主端据此渲染 AI/监管者/密码机等） */
    private Object authority;

    public RoomStateMessage() {
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    public boolean isStarted() { return started; }
    public void setStarted(boolean started) { this.started = started; }
    public List<GamePlayerVO> getPlayers() { return players; }
    public void setPlayers(List<GamePlayerVO> players) { this.players = players; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Long getHostId() { return hostId; }
    public void setHostId(Long hostId) { this.hostId = hostId; }
    public Object getAuthority() { return authority; }
    public void setAuthority(Object authority) { this.authority = authority; }
}
