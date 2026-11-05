package com.fenglin.springboottest.dto.game;

import java.io.Serializable;

/**
 * 客户端 → 服务端 的联机消息（STOMP 发到 {@code /app/game/*}）。
 *
 * <p>字段按需填充：加入房间时带 roomId；移动时带 x/y；开局/准备/选角时带对应字段。</p>
 */
public class GameMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String roomId;
    private String roleId;
    private Double x;
    private Double y;
    private Integer hp;
    private Boolean alive;
    private Boolean escaped;
    private Boolean chaired;
    /** 预留：玩家在本地完成的交互动作（修机/开门/救人等），服务端暂只做转发 */
    private String action;

    /** 房主广播的权威状态（AI 求生者 / 监管者 / 密码机 / 门 / 胜负） */
    private Object authority;

    public GameMessage() {
    }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    public String getRoleId() { return roleId; }
    public void setRoleId(String roleId) { this.roleId = roleId; }
    public Double getX() { return x; }
    public void setX(Double x) { this.x = x; }
    public Double getY() { return y; }
    public void setY(Double y) { this.y = y; }
    public Integer getHp() { return hp; }
    public void setHp(Integer hp) { this.hp = hp; }
    public Boolean getAlive() { return alive; }
    public void setAlive(Boolean alive) { this.alive = alive; }
    public Boolean getEscaped() { return escaped; }
    public void setEscaped(Boolean escaped) { this.escaped = escaped; }
    public Boolean getChaired() { return chaired; }
    public void setChaired(Boolean chaired) { this.chaired = chaired; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public Object getAuthority() { return authority; }
    public void setAuthority(Object authority) { this.authority = authority; }
}
