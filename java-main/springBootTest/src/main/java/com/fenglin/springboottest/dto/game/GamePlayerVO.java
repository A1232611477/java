package com.fenglin.springboottest.dto.game;

import java.io.Serializable;

/**
 * 联机房间内一名玩家（真人）的运行时状态快照。
 *
 * <p>由服务端在广播 {@code ROOM_STATE} 时统一打包，客户端据此渲染其他队友的位置与状态。</p>
 */
public class GamePlayerVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户 id */
    private Long userId;

    /** 昵称 */
    private String nickname;

    /** 是否房主（房主负责开局） */
    private boolean host;

    /** 是否准备 */
    private boolean ready;

    /** 角色 id：priestess / mercenary / doctor / coordinator / seer */
    private String roleId;

    /** 玩家在求生者队伍里的槽位下标（0-3），-1 表示尚未分配 */
    private int slot;

    /** 世界坐标 x */
    private double x;
    /** 世界坐标 y */
    private double y;
    /** 生命值 */
    private int hp;
    /** 是否存活 */
    private boolean alive;
    /** 是否已逃脱 */
    private boolean escaped;
    /** 是否被挂椅 */
    private boolean chaired;

    public GamePlayerVO() {
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public boolean isHost() { return host; }
    public void setHost(boolean host) { this.host = host; }
    public boolean isReady() { return ready; }
    public void setReady(boolean ready) { this.ready = ready; }
    public String getRoleId() { return roleId; }
    public void setRoleId(String roleId) { this.roleId = roleId; }
    public int getSlot() { return slot; }
    public void setSlot(int slot) { this.slot = slot; }
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; }
    public boolean isAlive() { return alive; }
    public void setAlive(boolean alive) { this.alive = alive; }
    public boolean isEscaped() { return escaped; }
    public void setEscaped(boolean escaped) { this.escaped = escaped; }
    public boolean isChaired() { return chaired; }
    public void setChaired(boolean chaired) { this.chaired = chaired; }
}
