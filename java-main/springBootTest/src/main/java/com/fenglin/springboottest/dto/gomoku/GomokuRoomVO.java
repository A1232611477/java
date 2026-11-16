package com.fenglin.springboottest.dto.gomoku;

import java.util.ArrayList;
import java.util.List;

/**
 * 五子棋联机房间对外快照（用于 REST 返回）。
 *
 * <p>moves 为有序落子列表，每项是 [x, y, color]（color：1=黑=房主，2=白=受邀方）。</p>
 */
public class GomokuRoomVO {

    private String roomId;
    private Long hostId;
    private String hostNickname;
    private Long guestId;
    private String guestNickname;
    private boolean started;
    private int nextColor;
    private List<int[]> moves = new ArrayList<>();

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public Long getHostId() { return hostId; }
    public void setHostId(Long hostId) { this.hostId = hostId; }

    public String getHostNickname() { return hostNickname; }
    public void setHostNickname(String hostNickname) { this.hostNickname = hostNickname; }

    public Long getGuestId() { return guestId; }
    public void setGuestId(Long guestId) { this.guestId = guestId; }

    public String getGuestNickname() { return guestNickname; }
    public void setGuestNickname(String guestNickname) { this.guestNickname = guestNickname; }

    public boolean isStarted() { return started; }
    public void setStarted(boolean started) { this.started = started; }

    public int getNextColor() { return nextColor; }
    public void setNextColor(int nextColor) { this.nextColor = nextColor; }

    public List<int[]> getMoves() { return moves; }
    public void setMoves(List<int[]> moves) { this.moves = moves; }
}
